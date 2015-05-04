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
      
      if(annualStats.size() > 0) {
         System.out.println();
         Average avgPnl = new Average();
         Average avgPnlPct = new Average();
         Average avgDD = new Average();
         Average avgDDPct = new Average();
         for(int ii = 0; ii < annualStats.size(); ++ii) {
            String yearStr = annualStats.getTimestamp(ii).format(DateTimeFormatter.ofPattern("yyyy"));
            String pnlStr = String.format("$%,d", Math.round(annualStats.get(ii, 0)));
            String pnlPctStr = String.format("%.2f%%", annualStats.get(ii, 1)*100.0);
            String endEqStr = String.format("$%,d", Math.round(annualStats.get(ii, 2)));
            String ddStr = String.format("$%,d", Math.round(annualStats.get(ii, 3)));
            String ddPctStr = String.format("%.2f%%", annualStats.get(ii, 4)*100.0);
            System.out.println(yearStr + " PnL: " + pnlStr + ", PnL Pct: " + pnlPctStr +
                  ", End Equity: " + endEqStr + ", MaxDD: " + ddStr +
                  ", Pct MaxDD: " + ddPctStr);
            avgPnl.add(annualStats.get(ii, 0));
            avgPnlPct.add(annualStats.get(ii, 1));
            avgDD.add(Math.abs(annualStats.get(ii,3)));
            avgDDPct.add(Math.abs(annualStats.get(ii, 4)));
         }
         
         System.out.println();
         
         String pnlStr = String.format("$%,d", Math.round(avgPnl.get()));
         String pnlPctStr = String.format("%.2f%%", avgPnlPct.get()*100.0);
         String ddStr = String.format("$%,d", Math.round(avgDD.get()));
         String ddPctStr = String.format("%.2f%%", avgDDPct.get()*100.0);
         double gainToPain = avgPnl.get() / avgDD.get();
         System.out.println("Avg PnL: " + pnlStr + ", Pct Avg PnL: " + pnlPctStr + 
               ", Avg DD: " + ddStr + ", Pct Avg DD: " + ddPctStr + 
               ", Gain to Pain: " + String.format("%.4f", gainToPain));
      } else {
         System.out.println();
      }
      
      // Global statistics
      LocalDateTime maxDateTime = LocalDateTime.MIN;
      double maxEndEq = Double.MIN_VALUE;
      
      Series equity = strategy.getEquity();
      for(int ii = 0; ii < equity.size(); ++ii) {
         if(equity.get(ii) > maxEndEq) {
            maxEndEq = equity.get(ii);
            maxDateTime = equity.getTimestamp(ii);
         }
      }
      
      double lastDD = maxEndEq - equity.get(equity.size()-1);
      double lastDDPct = lastDD/maxEndEq*100;
      
      System.out.println(
            "\n" +
            "Total equity peak [" + maxDateTime.format(DateTimeFormatter.ISO_DATE) + "]: " + String.format("$%,d", Math.round(maxEndEq)) +
            "\n" +
            String.format("Current Drawdown: $%,d [%.2f%%]", Math.round(lastDD), lastDDPct));
      
      if(equity.size() > 2) {
         int ii = equity.size() - 1;
         int jj = ii - 1;
         
         for(; jj >= 0 && equity.getTimestamp(jj).getYear() == equity.getTimestamp(ii).getYear(); --jj) {
            
         }
         
         if(equity.getTimestamp(jj).getYear() != equity.getTimestamp(ii).getYear()) {
            ++jj;
            maxDateTime = equity.getTimestamp(jj);
            maxEndEq = equity.get(jj);
            for(++jj; jj < equity.size(); ++jj) {
               if(equity.get(jj) > maxEndEq) {
                  maxEndEq = equity.get(jj);
                  maxDateTime = equity.getTimestamp(jj);
               }
            }
            
            lastDD = maxEndEq - equity.get(equity.size()-1);
            lastDDPct = lastDD/maxEndEq*100;
            
            System.out.println(
                  "\n" +
                  Integer.toString(equity.getTimestamp(ii).getYear()) + " equity peak [" + 
                  maxDateTime.format(DateTimeFormatter.ISO_DATE) + "]: " + String.format("$%,d", Math.round(maxEndEq)) +
                  "\n" +
                  String.format("Current Drawdown: $%,d [%.2f%%]", Math.round(lastDD), lastDDPct));
         }
      }
      
      System.out.println();
      
      TradeSummary summary = strategy.getSummary("TOTAL", "All");
      System.out.println("Avg Trade PnL: " + String.format("$%,d", Math.round(summary.averageTradePnl)) +
                         ", Max DD: " + String.format("$%,d", Math.round(summary.maxDrawdown)) +
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
