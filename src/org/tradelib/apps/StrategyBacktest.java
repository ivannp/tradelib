package org.tradelib.apps;

import java.io.IOException;
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
      strategy.writeExecutionsAndTrades();
      elapsedTime = System.nanoTime() - start;
      System.out.println("writing to the database took " + String.format("%.2f secs",(double)elapsedTime/1e9));
      
      System.out.println();
      
      // Compute total statistics
      strategy.totalTradeStats();

      // Short performance statistics for the report
      
      // Annual statistics
      TimeSeries<Double> annualStats = strategy.getAnnualStats();
      
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
      
      // Global statistics
      TradeSummary summary = strategy.getSummary("TOTAL", "All");
      System.out.println("Avg Trade PnL: " + String.format("%,.2f", summary.averageTradePnl) +
                         ", Max DD: " + String.format("%,.2f", summary.maxDrawdown) +
                         ", Num Trades: " + Long.toString(summary.numTrades));
   }
}
