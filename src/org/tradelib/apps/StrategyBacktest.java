package org.tradelib.apps;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.tradelib.core.Average;
import org.tradelib.core.Context;
import org.tradelib.core.HistoricalDataFeed;
import org.tradelib.core.HistoricalReplay;
import org.tradelib.core.MySQLDataFeed;
import org.tradelib.core.Series;
import org.tradelib.core.Strategy;
import org.tradelib.core.TimeSeries;
import org.tradelib.core.TradeSummary;

public class StrategyBacktest {

   public static void run(Strategy strategy) throws Exception {
      // Setup the logging
      Logger rootLogger = Logger.getLogger(""); 
      FileHandler logHandler = new FileHandler(BacktestCfg.instance().getProperty("diag.out", "diag.out"), true); 
      logHandler.setFormatter(new SimpleFormatter()); 
      logHandler.setLevel(Level.INFO); 
      // rootLogger.removeHandler(rootLogger.getHandlers()[0]); 
      rootLogger.setLevel(Level.WARNING); 
      rootLogger.addHandler(logHandler);
      
      // Setup Hibernate
      // Configuration configuration = new Configuration();
      // StandardServiceRegistryBuilder builder = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties());
      // SessionFactory factory = configuration.buildSessionFactory(builder.build());
      
      Context context = new Context();
      context.dbUrl = BacktestCfg.instance().getProperty("db.url");
      
      HistoricalDataFeed hdf = new MySQLDataFeed(context);
      hdf.configure(BacktestCfg.instance().getProperty("datafeed.config", "config/datafeed.properties"));
      context.historicalDataFeed = hdf;
      
      HistoricalReplay hr = new HistoricalReplay(context);
      context.broker = hr;
      
      strategy.initialize(context);
      strategy.cleanupDb();
      
      long start = System.nanoTime();
      strategy.start();
      long elapsedTime = System.nanoTime() - start;
      System.out.println("backtest took " + String.format("%.2f secs",(double)elapsedTime/1e9));
      
      start = System.nanoTime();
      strategy.updateEndEquity();
      strategy.writeExecutionsAndTrades();
      strategy.writeEquity();
      elapsedTime = System.nanoTime() - start;
      System.out.println("writing to the database took " + String.format("%.2f secs",(double)elapsedTime/1e9));
      
      System.out.println();
      
      // Compute total statistics
      strategy.totalTradeStats();

      // Short performance statistics for the report
      
      // Annual statistics
      Series annualStats = strategy.getAnnualStats();
      Series annualStats2 = strategy.getAnnualStats2();
      
      if(annualStats.size() > 0) {
         Average avgPnl = new Average();
         Average avgDrawdown = new Average();
         for(int ii = 0; ii < annualStats.size(); ++ii) {
            String syear = annualStats.getTimestamp(ii).format(DateTimeFormatter.ofPattern("yyyy"));
            String spnl = String.format("%,.2f", annualStats.get(ii, 0));
            String smd = String.format("%,.2f", annualStats.get(ii, 1));
            System.out.println(syear + " PnL: " + spnl + ", MaxDrawdown: " + smd);
            avgPnl.add(annualStats.get(ii, 0));
            avgDrawdown.add(Math.abs(annualStats.get(ii, 1)));
         }
         
         System.out.println();
         
         String spnl = String.format("%,.2f", avgPnl.get());
         String smd = String.format("%,.2f", avgDrawdown.get());
         double gainToPain = avgPnl.get() / avgDrawdown.get();
         System.out.println("Avg PnL: " + spnl + ", Avg DD: " + smd + ", Gain to Pain: " + String.format("%.4f", gainToPain));
      } else {
         System.out.println();
      }
      
      if(annualStats2.size() > 0) {
         System.out.println();
         Average avgPnl = new Average();
         Average avgPnlPct = new Average();
         Average avgDD = new Average();
         Average avgDDPct = new Average();
         for(int ii = 0; ii < annualStats2.size(); ++ii) {
            String yearStr = annualStats2.getTimestamp(ii).format(DateTimeFormatter.ofPattern("yyyy"));
            String pnlStr = String.format("%,d", Math.round(annualStats2.get(ii, 0)));
            String pnlPctStr = String.format("%.2f%%", annualStats2.get(ii, 1)*100.0);
            String endEqStr = String.format("%,d", Math.round(annualStats2.get(ii, 2)));
            String ddStr = String.format("%,.2f", annualStats2.get(ii, 3));
            String ddPctStr = String.format("%.2f%%", annualStats2.get(ii, 4)*100.0);
            System.out.println(yearStr + " PnL: " + pnlStr + ", PnL Pct: " + pnlPctStr +
                  ", End Equity: " + endEqStr + ", MaxDD: " + ddStr +
                  ", Pct MaxDD: " + ddPctStr);
            avgPnl.add(annualStats2.get(ii, 0));
            avgPnlPct.add(annualStats2.get(ii, 1));
            avgDD.add(Math.abs(annualStats2.get(ii,3)));
            avgDDPct.add(Math.abs(annualStats2.get(ii, 4)));
         }
         
         System.out.println();
         
         String pnlStr = String.format("%,d", Math.round(avgPnl.get()));
         String pnlPctStr = String.format("%.2f%%", avgPnlPct.get()*100.0);
         String ddStr = String.format("%,d", Math.round(avgDD.get()));
         String ddPctStr = String.format("%.2f%%", avgDDPct.get()*100.0);
         double gainToPain = avgPnl.get() / avgDD.get();
         System.out.println("Avg PnL: " + pnlStr + ", Pct Avg PnL: " + pnlPctStr + 
               ", Avg DD: " + ddStr + ", Pct Avg DD: " + ddPctStr + 
               ", Gain to Pain: " + String.format("%.4f", gainToPain));
      } else {
         System.out.println();
      }
      
      // Global statistics
      LocalDateTime maxEquityDateTime = LocalDateTime.MIN;
      double maxEquity = Double.MIN_VALUE;
      double equity = 0.0;
      
      Series pnl = strategy.getPnl();
      for(int ii = 0; ii < pnl.size(); ++ii) {
         equity += pnl.get(ii);
         if(equity > maxEquity) {
            maxEquity = equity;
            maxEquityDateTime = pnl.getTimestamp(ii);
         }
      }
      
      double lastDrawdown = maxEquity - equity;
      
      System.out.println(
            "\n" +
            "Total equity peak (" + maxEquityDateTime.format(DateTimeFormatter.ISO_DATE) + "): " + String.format("%,.2f", maxEquity) +
            "\n" +
            "Current Drawdown: " + String.format("%,.2f", lastDrawdown));
      
      if(pnl.size() > 2) {
         int ii = pnl.size() - 1;
         int jj = ii - 1;
         
         for(; jj >= 0 && pnl.getTimestamp(jj).getYear() == pnl.getTimestamp(ii).getYear(); --jj) {
            
         }
         
         if(pnl.getTimestamp(jj).getYear() != pnl.getTimestamp(ii).getYear()) {
            ++jj;
            maxEquityDateTime = pnl.getTimestamp(jj);
            equity = pnl.get(jj);
            maxEquity = pnl.get(jj);
            for(++jj; jj < pnl.size(); ++jj) {
               equity += pnl.get(jj);
               if(equity > maxEquity) {
                  maxEquity = equity;
                  maxEquityDateTime = pnl.getTimestamp(jj);
               }
            }
            
            lastDrawdown = maxEquity - equity;
            
            System.out.println(
                  "\n" +
                  Integer.toString(pnl.getTimestamp(ii).getYear()) + " equity peak (" + 
                  maxEquityDateTime.format(DateTimeFormatter.ISO_DATE) + "): " + String.format("%,.2f", maxEquity) +
                  "\n" +
                  "Current Drawdown: " + String.format("%,.2f", lastDrawdown));
         }
      }
      
      System.out.println();
      
      TradeSummary summary = strategy.getSummary("TOTAL", "All");
      System.out.println("Avg Trade PnL: " + String.format("%,.2f", summary.averageTradePnl) +
                         ", Max DD: " + String.format("%,.2f", summary.maxDrawdown) +
                         ", Num Trades: " + Long.toString(summary.numTrades));
   }
   
   private class PerformanceDetails {
      public LocalDateTime maxEquityDateTime;
      public double maxEquity;
      public double lastDrawdown;
      
      public void build(TimeSeries<Double> pnl) {
         maxEquityDateTime = LocalDateTime.MIN;
         maxEquity = Double.MIN_VALUE;
         
         double equity = 0.0;
         
         for(int ii = 0; ii < pnl.size(); ++ii) {
            equity += pnl.get(ii);
            if(equity > maxEquity) {
               maxEquity = equity;
               maxEquityDateTime = pnl.getTimestamp(ii);
            }
         }
         
         lastDrawdown = maxEquity - equity;
      }
   }
}
