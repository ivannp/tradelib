package org.tradelib.apps;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.TreeMap;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;

import org.tradelib.core.Average;
import org.tradelib.core.Context;
import org.tradelib.core.HistoricalDataFeed;
import org.tradelib.core.HistoricalReplay;
import org.tradelib.core.MySQLDataFeed;
import org.tradelib.core.Series;
import org.tradelib.core.Strategy;
import org.tradelib.core.TimeSeries;
import org.tradelib.core.TradeSummary;
import org.tradelib.misc.StrategyText;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class StrategyBacktest {

   public static void run(Strategy strategy) throws Exception {
      // Setup the logging
      System.setProperty(
               "java.util.logging.SimpleFormatter.format",
               "%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS: %4$s: %5$s%n%6$s%n");
      LogManager.getLogManager().reset();
      Logger rootLogger = Logger.getLogger("");
      if(Boolean.parseBoolean(BacktestCfg.instance().getProperty("file.log", "true"))) {
         FileHandler logHandler = new FileHandler("diag.out", 8*1024*1024, 2, true);
         logHandler.setFormatter(new SimpleFormatter()); 
         logHandler.setLevel(Level.FINEST); 
         rootLogger.addHandler(logHandler);
      }
      
      if(Boolean.parseBoolean(BacktestCfg.instance().getProperty("console.log", "true"))) {
         ConsoleHandler consoleHandler = new ConsoleHandler();
         consoleHandler.setFormatter(new SimpleFormatter());
         consoleHandler.setLevel(Level.INFO);
         rootLogger.addHandler(consoleHandler);
      }
       
      rootLogger.setLevel(Level.INFO); 
      
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
      
      // Write the strategy totals to the database
      strategy.totalTradeStats();
      
      // Write the strategy report to the database and obtain the JSON
      // for writing it to the console.
      JsonObject report = strategy.writeStrategyReport();
      
      JsonArray asa = report.getAsJsonArray("annual_stats");
      
      // If emails are being send out
      String signalText = StrategyText.build(
               context.dbUrl,
               strategy.getName(),
               strategy.getLastTimestamp().toLocalDate(),
               "\t");
      
      String message = "";
      
      if(asa.size() > 0) {
         // Sort the array
         TreeMap<Integer, Integer> map = new TreeMap<Integer, Integer>();
         for(int ii = 0; ii < asa.size(); ++ii) {
            int year = asa.get(ii).getAsJsonObject().get("year").getAsInt();
            map.put(year, ii);
         }

         for(int id : map.values()) {
            JsonObject jo = asa.get(id).getAsJsonObject();
            String yearStr = String.valueOf(jo.get("year").getAsInt());
            String pnlStr = String.format("$%,d", jo.get("pnl").getAsInt());
            String pnlPctStr = String.format("%.2f%%", jo.get("pnl_pct").getAsDouble());
            String endEqStr = String.format("$%,d", jo.get("end_equity").getAsInt());
            String ddStr = String.format("$%,d", jo.get("maxdd").getAsInt());
            String ddPctStr = String.format("%.2f%%", jo.get("maxdd_pct").getAsDouble());
            String str = yearStr + " PnL: " + pnlStr + ", PnL Pct: " + pnlPctStr +
                  ", End Equity: " + endEqStr + ", MaxDD: " + ddStr +
                  ", Pct MaxDD: " + ddPctStr;
            message += str + "\n";
         }
         
         String pnlStr = String.format("$%,d", report.get("pnl").getAsInt());
         String pnlPctStr = String.format("%.2f%%", report.get("pnl_pct").getAsDouble());
         String ddStr = String.format("$%,d", report.get("avgdd").getAsInt());
         String ddPctStr = String.format("%.2f%%", report.get("avgdd_pct").getAsDouble());
         String gainToPainStr = String.format("%.4f", report.get("gain_to_pain").getAsDouble());
         String str = "\nAvg PnL: " + pnlStr + ", Pct Avg PnL: " + pnlPctStr + 
               ", Avg DD: " + ddStr + ", Pct Avg DD: " + ddPctStr + 
               ", Gain to Pain: " + gainToPainStr;
         message += str + "\n";
      } else {
         message += "\n";
      }
      
      // Global statistics
      JsonObject jo = report.getAsJsonObject("total_peak");
      String dateStr = jo.get("date").getAsString();
      int maxEndEq = jo.get("equity").getAsInt();
      jo = report.getAsJsonObject("total_maxdd");
      double cash = jo.get("cash").getAsDouble();
      double pct = jo.get("pct").getAsDouble();
      message += 
               "\n" +
               "Total equity peak [" + dateStr + "]: " + String.format("$%,d", maxEndEq) +
               "\n" +
               String.format("Current Drawdown: $%,d [%.2f%%]", Math.round(cash), pct) +
               "\n";
      
      if(report.has("latest_peak") && report.has("latest_maxdd")) {
         jo = report.getAsJsonObject("latest_peak");
         LocalDate ld = LocalDate.parse(jo.get("date").getAsString(), DateTimeFormatter.ISO_DATE);
         maxEndEq = jo.get("equity").getAsInt();
         jo = report.getAsJsonObject("latest_maxdd");
         cash = jo.get("cash").getAsDouble();
         pct = jo.get("pct").getAsDouble();
         message += 
               "\n" +
               Integer.toString(ld.getYear()) + " equity peak [" + 
               ld.format(DateTimeFormatter.ISO_DATE) + "]: " + String.format("$%,d", maxEndEq) +
               "\n" +
               String.format("Current Drawdown: $%,d [%.2f%%]", Math.round(cash), pct) +
               "\n";
      }
      
      message += "\n" +
                 "Avg Trade PnL: " + String.format("$%,d", Math.round(report.get("avg_trade_pnl").getAsDouble())) +
                 ", Max DD: " + String.format("$%,d", Math.round(report.get("maxdd").getAsDouble())) +
                 ", Max DD Pct: " + String.format("%.2f%%", report.get("maxdd_pct").getAsDouble()) +
                 ", Num Trades: " + Integer.toString(report.get("num_trades").getAsInt());
      
      System.out.println(message);
      
      if(Boolean.parseBoolean(BacktestCfg.instance().getProperty("email.enabled", "false"))) {
         Properties props = new Properties();
         props.put("mail.smtp.auth", "true");
         props.put("mail.smtp.starttls.enable", "true");
         props.put("mail.smtp.host", "smtp.sendgrid.net");
         props.put("mail.smtp.port", "587");
         
         String user = BacktestCfg.instance().getProperty("email.user");
         String pass = BacktestCfg.instance().getProperty("email.pass");
         
         Session session = Session.getInstance(
                           props,
                           new javax.mail.Authenticator() {
                              protected PasswordAuthentication getPasswordAuthentication() {
                                 return new PasswordAuthentication(user, pass);
                              }
                           });
         
         MimeMessage msg = new MimeMessage(session);
         try {
            msg.setFrom(new InternetAddress(BacktestCfg.instance().getProperty("email.from")));
            msg.addRecipients(RecipientType.TO, BacktestCfg.instance().getProperty("email.recipients"));
            msg.setSubject(strategy.getName() + " Report [" + strategy.getLastTimestamp().format(DateTimeFormatter.ISO_LOCAL_DATE) + "]");
            msg.setText("Positions & Signals\n" + signalText + "\n\nStatistics\n" + message);
            Transport.send(msg);
         } catch (Exception ee) {
            Logger.getLogger("").warning(ee.getMessage());
         }
      }
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
