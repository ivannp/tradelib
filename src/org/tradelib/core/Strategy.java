package org.tradelib.core;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public abstract class Strategy implements IBrokerListener {

   protected String name;
   protected long dbId = -1;
   protected String dbUrl;
   protected IBroker broker;
   protected BarHierarchy barData = new BarHierarchy();
   protected List<Execution> executions = new ArrayList<Execution>();
   
   private LocalDateTime tradingStart = LocalDateTime.of(1990, 1, 1, 0, 0);

   // protected Portfolio portfolio = new Portfolio();
   protected Account account = new Account();
   
   protected Connection connection = null;
   
   protected LocalDateTime lastTimestamp = LocalDateTime.MIN;
   
   public LocalDateTime getLastTimestamp() {
      return lastTimestamp;
   }
   
   protected void connectIfNecessary() throws SQLException {
      if(connection == null) {
         connection = DriverManager.getConnection(dbUrl);
         connection.setAutoCommit(false);
      }
   }
   
   public void initialize(Context context) throws Exception {
      // Cache some context
      this.broker = context.broker;
      this.dbUrl = context.dbUrl;
      this.broker.addBrokerListener(this);
   }
   
   protected void setName(String name) { this.name = name; }
   
   protected void setDbUrl(String dbUrl) { this.dbUrl = dbUrl; }
   
   protected IBroker getBroker() { return broker; }
   
   protected void subscribe(String symbol) throws Exception {
      barData.addSymbol(symbol);
      getBroker().subscribe(symbol);
   }
   
   public void cleanupDb() throws SQLException {
      if(dbUrl == null || name == null) return;
      
      connectIfNecessary();
      
      // Load the strategy unique id from the "strategies" table
      getDbId();

      String query = "DELETE FROM executions WHERE strategy_id=?"; 
      PreparedStatement stmt = connection.prepareStatement(query);
      stmt.setLong(1, dbId);
      stmt.executeUpdate();
      stmt.close();
      
      query = "DELETE FROM trades WHERE strategy_id=?"; 
      stmt = connection.prepareStatement(query);
      stmt.setLong(1, dbId);
      stmt.executeUpdate();
      stmt.close();
      
      query = "DELETE FROM pnls WHERE strategy_id=?"; 
      stmt = connection.prepareStatement(query);
      stmt.setLong(1, dbId);
      stmt.executeUpdate();
      stmt.close();
      
      query = "DELETE FROM trade_summaries WHERE strategy_id=?"; 
      stmt = connection.prepareStatement(query);
      stmt.setLong(1, dbId);
      stmt.executeUpdate();
      stmt.close();
      
//      query = "DELETE FROM strategy_positions WHERE strategy_id=?"; 
//      stmt = connection.prepareStatement(query);
//      stmt.setLong(1, dbId);
//      stmt.executeUpdate();
//      stmt.close();
      
      query = "DELETE FROM end_equity WHERE strategy_id=?"; 
      stmt = connection.prepareStatement(query);
      stmt.setLong(1, dbId);
      stmt.executeUpdate();
      stmt.close();
      
      connection.commit();
   }
   
   public void getDbId() throws SQLException {
      if(dbId >= 0) return;
      
      connectIfNecessary();

      // Get the strategy id
      String query = "SELECT id FROM strategies where name=?";
      PreparedStatement stmt = connection.prepareStatement(query);
      stmt.setString(1, name);
      ResultSet rs = stmt.executeQuery();
      if(!rs.next()) {
         // The name doesn't exist, insert it
         rs.close();
         stmt.close();
         stmt = connection.prepareStatement("INSERT INTO strategies(name) values (?)");
         stmt.setString(1, name);
         // Ignore errors, some other process may insert the strategy.
         try {
            stmt.executeUpdate();
         } catch(Exception e) {
         }
         stmt.close();
         // Repeat the query
         stmt = connection.prepareStatement(query);
         stmt.setString(1, name);
         rs = stmt.executeQuery();
         rs.next();
      }
      
      dbId = rs.getLong(1);
      rs.close();
      stmt.close();
   }
   
   public void writeExecutions() throws SQLException {
      
      connectIfNecessary();

      getDbId();
      
      String query = "INSERT INTO executions(symbol,strategy_id,ts,price,quantity,signal_name) VALUES (?,?,?,?,?,?)";
      PreparedStatement stmt = connection.prepareStatement(query);
      for(Execution execution : executions) {
         stmt.setString(1, execution.getSymbol());
         stmt.setLong(2, dbId);
         stmt.setTimestamp(3, Timestamp.valueOf(execution.getDateTime()));
         stmt.setDouble(4, execution.getPrice());
         stmt.setLong(5, execution.getQuantity());
         stmt.setString(6, execution.getSignal());
         // stmt.executeUpdate();
         stmt.addBatch();
      }
      stmt.executeBatch();
      connection.commit();
   }
   
   public void writeExecutionsAndTrades() throws Exception {
      writeExecutions();
      writeTrades();
   }
   
   public void writeTrades() throws Exception {
      for(String symbol : account.getPortfolioSymbols()) {
         writeTrades(broker.getInstrument(symbol));
      }
   }
   
   public void writeTrades(Instrument instrument) throws Exception {
      BarHistory history = barData.getHistory(instrument.getSymbol(), Duration.ofDays(1));
      Series pnl = account.getPnlSeries(instrument);
      if(pnl.size() == 0) return;
      
      connectIfNecessary();
      
      getDbId();

      long start = System.nanoTime();
      
      // Write the PnL
      String query = " INSERT INTO pnls(strategy_id,symbol,ts,pnl) VALUE (?,?,?,?) " +
                     " ON DUPLICATE KEY UPDATE pnl=?";
      PreparedStatement stmt = connection.prepareStatement(query);
      stmt.setLong(1, dbId);
      stmt.setString(2, instrument.getSymbol());
      for(int ii = 0; ii < pnl.size(); ++ii) {
         stmt.setTimestamp(3, Timestamp.valueOf(pnl.getTimestamp(ii)));
         stmt.setDouble(4, pnl.get(ii));
         stmt.setDouble(5, pnl.get(ii));
         stmt.addBatch();
      }
      stmt.executeBatch();
      connection.commit();
      stmt.close();
      
      long elapsedTime = System.nanoTime() - start;
      // System.out.println("pnls insert took " + String.format("%.4f secs",(double)elapsedTime/1e9));
      
      TradingResults tr = account.getPortfolioTradingResults(instrument);
      // TradingResults trold = portfolio.getTradingResults(instrument);
      // Write the trade statistics
      if(tr.stats.size() > 0) {
         
         query = " INSERT INTO trades(strategy_id,symbol,start,end,initial_position, " +
                 "       max_position,num_transactions,pnl,pct_pnl,tick_pnl,fees) " +
                 " VALUES(?,?,?,?,?,?,?,?,?,?,?)";
         stmt = connection.prepareStatement(query);
         stmt.setLong(1, dbId);
         stmt.setString(2, instrument.getSymbol());
         start = System.nanoTime();
         int ii = 0;
         for(Trade tradeStats : tr.stats) {
            stmt.setTimestamp(3, Timestamp.valueOf(tradeStats.start));
            stmt.setTimestamp(4, Timestamp.valueOf(tradeStats.end));
            stmt.setLong(5, tradeStats.initialPosition);
            stmt.setLong(6, tradeStats.maxPosition);
            stmt.setLong(7, tradeStats.numTransactions);
            // System.out.println(tradeStats.pnl.toString());
            stmt.setDouble(8, tradeStats.pnl);
            stmt.setDouble(9, tradeStats.pctPnl);
            stmt.setDouble(10, tradeStats.tickPnl);
            stmt.setDouble(11, tradeStats.fees);
//            Trade oldTradeStats = trold.stats.get(ii);
//            if(tradeStats.pnl != oldTradeStats.pnl ||
//               tradeStats.numTransactions != oldTradeStats.numTransactions ||
//               tradeStats.initialPosition != oldTradeStats.initialPosition) {
//               
//               throw new Exception("Alternative trade statitics don't match!");
//            }
            stmt.executeUpdate();
            
            ++ii;
         }
         connection.commit();
         stmt.close();
         
         elapsedTime = System.nanoTime() - start;
         // System.out.println("trades insert took " + String.format("%.4f secs",(double)elapsedTime/1e9));
      }
      
      writeTradeSummary(instrument, "All", tr.all);
      writeTradeSummary(instrument, "Long", tr.longs);
      writeTradeSummary(instrument, "Short", tr.shorts);
      
      connection.commit();
   }
   
   protected void writeTradeSummary(String symbol, String type, TradeSummary tradeSummary) throws SQLException {
      
      connectIfNecessary();
      
      if(tradeSummary.numTrades > 0) {
         String query = " INSERT INTO trade_summaries (strategy_id,symbol,type,num_trades,gross_profits, " +
                        "      gross_losses,profit_factor,average_daily_pnl,daily_pnl_stddev,sharpe_ratio, " +
                        "      average_trade_pnl,trade_pnl_stddev,pct_positive,pct_negative,max_win,max_loss, " +
                        "      average_win,average_loss,average_win_loss,equity_min,equity_max,max_drawdown) " +
                        " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
         PreparedStatement stmt = connection.prepareStatement(query);
         stmt.setLong(1, dbId);
         stmt.setString(2, symbol);
         stmt.setString(3, type);
         stmt.setLong(4, tradeSummary.numTrades);
         setDoubleParam(stmt, 5, tradeSummary.grossProfits);
         setDoubleParam(stmt, 6, tradeSummary.grossLosses);
         setDoubleParam(stmt, 7, tradeSummary.profitFactor);
         setDoubleParam(stmt, 8, tradeSummary.averageDailyPnl);
         setDoubleParam(stmt, 9, tradeSummary.dailyPnlStdDev);
         setDoubleParam(stmt, 10, tradeSummary.sharpeRatio);
         setDoubleParam(stmt, 11, tradeSummary.averageTradePnl);
         setDoubleParam(stmt, 12, tradeSummary.tradePnlStdDev);
         setDoubleParam(stmt, 13, tradeSummary.pctPositive);
         setDoubleParam(stmt, 14, tradeSummary.pctNegative);
         setDoubleParam(stmt, 15, tradeSummary.maxWin);
         setDoubleParam(stmt, 16, tradeSummary.maxLoss);
         setDoubleParam(stmt, 17, tradeSummary.averageWin);
         setDoubleParam(stmt, 18, tradeSummary.averageLoss);
         setDoubleParam(stmt, 19, tradeSummary.averageWinLoss);
         setDoubleParam(stmt, 20, tradeSummary.equityMin);
         setDoubleParam(stmt, 21, tradeSummary.equityMax);
         setDoubleParam(stmt, 22, tradeSummary.maxDrawdown);
         stmt.executeUpdate();
         connection.commit();
      }
   }
   
   public void writeEquity() throws SQLException {
      connectIfNecessary();
      
      // Accumulate using the last value for each day (the end equity)
      Series eq = getAccount().getEquity().toDaily((Double x, Double y) -> y);
      
      String query = " INSERT INTO end_equity(strategy_id,ts,equity) VALUE (?,?,?) " +
                     " ON DUPLICATE KEY UPDATE equity=?";
      PreparedStatement stmt = connection.prepareStatement(query);
      stmt.setLong(1, dbId);
      for(int ii = 0; ii < eq.size(); ++ii) {
         stmt.setTimestamp(2, Timestamp.valueOf(eq.getTimestamp(ii)));
         stmt.setDouble(3, eq.get(ii));
         stmt.setDouble(4, eq.get(ii));
         stmt.addBatch();
      }
      stmt.executeBatch();
      connection.commit();
      stmt.close();
   }
   
   private void setDoubleParam(PreparedStatement stmt, int index, double value) throws SQLException {
      if(!Double.isNaN(value)) {
         stmt.setDouble(index, value);
      } else {
         stmt.setNull(index, Types.DOUBLE);
      }
   }
   
   protected void writeTradeSummary(Instrument instrument, String type, TradeSummary tradeSummary) throws SQLException {
      writeTradeSummary(instrument.getSymbol(), type, tradeSummary);
   }
   
   protected void onBarOpen(BarHistory history, Bar bar) throws Exception {}
   protected void onBarClose(BarHistory history, Bar bar) throws Exception {}
   protected void onBarClosed(BarHistory history, Bar bar) throws Exception {}
   protected void onOrderNotification(OrderNotification on) throws Exception {}
   
   public void barOpenHandler(Bar bar) throws Exception {
      BarHistory history = barData.getHistory(bar);
      // null means the strategy is not interested in this symbol
      if(history != null) {
         onBarOpen(history, bar);
      }
   }

   public void barCloseHandler(Bar bar) throws Exception {
      BarHistory history = barData.getHistory(bar);
      // null means the strategy is not interested in this symbol
      if(history != null) {
         history.add(bar);
         onBarClose(history, bar);
      }
   }

   public void barClosedHandler(Bar bar) throws Exception {
      BarHistory history = barData.getHistory(bar);
      // null means the strategy is not interested in this symbol
      if(history != null) {
         onBarClosed(history, bar);
      }
      
      if(bar.getDateTime().isAfter(lastTimestamp)) {
         lastTimestamp = bar.getDateTime();
      }
   }

   public void orderExecutedHandler(OrderNotification on) throws Exception {
      executions.add(on.execution);
      // portfolio.addTransaction(on.execution);
      getAccount().addTransaction(on.execution);
      onOrderNotification(on);
   }
   
   /**
    * @brief Get basic statistics to evaluate performance.
    * 
    * Computations are based off the equity curve.
    * 
    * For a quick strategy evaluation, I currently use the approach from
    * "Building Reliable Trading Systems", by Keith Fitschen.
    *  
    *    * PnL
    *    * PnL as percentage
    *    * End Equity
    *    * Cash Max Drawdown
    *    * Percentage Max Drawdown
    *    
    * @return A time series with the afford-mentioned columns
    * 
    * @throws SQLException 
    */
   public Series getAnnualStats() throws Exception {
      Series result = new Series(5);
      
      connectIfNecessary();
      
      String query = "SELECT ts,equity FROM end_equity WHERE strategy_id=" +
                     Long.toString(dbId) + " ORDER BY ts ASC";
      Statement stmt = connection.createStatement();
      ResultSet rs = stmt.executeQuery(query);
   
      double equity = Double.NaN;
      double startEquity = Double.NaN;
      double maxEquity = Double.NaN;
      double minEquity = Double.NaN;
      double maxDD = Double.NaN;
      double maxDDPct = 0.0;
   
      LocalDateTime last = null;
      
      if(rs.next()) {
         last = rs.getTimestamp(1).toLocalDateTime();
         equity = rs.getDouble(2);
         maxEquity = equity;
         minEquity = equity;
         startEquity = equity;
         maxDD = 0.0;
      }
      
      while(rs.next()) {
         // Kick off the statistics at the first different equity
         if(result.size() == 0 && rs.getDouble(2) == equity) {
            last = rs.getTimestamp(1).toLocalDateTime();
            continue;
         }
         
         LocalDateTime ldt = rs.getTimestamp(1).toLocalDateTime(); 
         if(ldt.getYear() == last.getYear()) {
            // Same year, update the counters
            equity = rs.getDouble(2);
            maxEquity = Math.max(maxEquity, equity);
            minEquity = Math.min(minEquity, equity);
            maxDD = Math.min(maxDD, equity - maxEquity);
            maxDDPct = Math.min(maxDDPct, equity/maxEquity - 1.0);
         } else {
            // Starting a new year. Summarize statistics and reset the counters.
            double pnl = equity - startEquity;
            double pnlPct = equity/startEquity - 1.0;
            result.append(last, pnl, pnlPct, equity, maxDD, maxDDPct);
            
            startEquity = equity;
            equity = rs.getDouble(2);
            
            maxEquity = equity;
            minEquity = equity;
            
            maxDD = 0.0;
            maxDDPct = 0.0;
            
            last = ldt;
         }
      }
      
      // Add the last year
      if(!Double.isNaN(equity)) {
         double pnl = equity - startEquity;
         double pnlPct = equity/startEquity - 1.0;
         result.append(last, pnl, pnlPct, equity, maxDD, maxDDPct);
      }
      
      connection.commit();
      
      return result;
   }

   /**
    * @brief Get basic statistics used to evaluate performance.
    * 
    * For a quick strategy evaluation, I currently use the approach from
    * "Building Reliable Trading Systems", by Keith Fitschen. 
    *    * PnL
    *    * Drawdown
    *    
    * @return A time series with two columns - PnL and MaxDrawdown
    * @throws SQLException 
    */
   public Series getAnnualStatsOld() throws SQLException {
      Series annualStats = new Series(2);

      connectIfNecessary();
      
      getDbId();
      
      String query = "SELECT ts,pnl FROM pnls WHERE strategy_id=" + Long.toString(dbId) +
               " AND symbol = 'TOTAL' ORDER BY ts ASC";
      Statement stmt = connection.createStatement();
      ResultSet rs = stmt.executeQuery(query);
      
      double equity = 0.0;
      double maxEquity = Double.MIN_VALUE;
      double minEquity = Double.MAX_VALUE;
      double maxDrawdown = Double.MAX_VALUE;
      
      LocalDateTime last = LocalDateTime.of(0, 1, 1, 0, 0);
      
      while(rs.next()) {
         LocalDateTime ldt = rs.getTimestamp(1).toLocalDateTime();
         double pnl = rs.getDouble(2);
         
         // Kick off the statistics at the first positive PnL
         if(annualStats.size() == 0 && pnl == 0.0) continue;
         
         if(ldt.getYear() == last.getYear()) {
            // Same year, update the counters
            equity += pnl;
            maxEquity = Math.max(maxEquity, equity);
            minEquity = Math.min(minEquity, equity);
            maxDrawdown = Math.min(maxDrawdown, equity - maxEquity);
         } else {
            // Starting a new year. Summarize statistics and reset the counters.
            if(maxDrawdown != Double.MAX_VALUE) {
               annualStats.append(last, equity, maxDrawdown);
            }
            equity = pnl;
            maxEquity = equity;
            minEquity = equity;
            maxDrawdown = equity;
            last = ldt;
         }
      }
      
      // Add the last year
      if(maxDrawdown != Double.MAX_VALUE) {
         annualStats.append(last, equity, maxDrawdown);
      }
      
      connection.commit();
      
      return annualStats;
   }
   
   public Series getPnl() throws Exception {
      return getPnl("TOTAL");
   }
   
   public Series getPnl(String symbol) throws Exception {
      Series pnl = new Series(2);

      connectIfNecessary();
      
      getDbId();
      
      String query = "SELECT ts,pnl FROM pnls WHERE strategy_id=" + Long.toString(dbId) +
               " AND symbol = '" + symbol +"' ORDER BY ts ASC";
      Statement stmt = connection.createStatement();
      ResultSet rs = stmt.executeQuery(query);
      
      double equity = 0.0;
      double maxEquity = Double.MIN_VALUE;
      double minEquity = Double.MAX_VALUE;
      double maxDrawdown = Double.MAX_VALUE;
      
      LocalDateTime last = LocalDateTime.of(0, 1, 1, 0, 0);
      
      while(rs.next()) {
         pnl.append(rs.getTimestamp(1).toLocalDateTime(), rs.getDouble(2));
      }
      
      connection.commit();
      
      return pnl;
   }
   
   public Series getEquity() {
      return getAccount().getEquity();
   }
  
   public TimeSeries<BigDecimal> getAnnualPnl(String symbols) throws SQLException {
      TimeSeries<BigDecimal> pnl = new TimeSeries<BigDecimal>(1);

      connectIfNecessary();
      
      getDbId();
      
      // Build the query
      String query = "SELECT ts,pnl FROM pnls WHERE strategy_id=" + Long.toString(dbId) + " AND ";
      String inList = "";
      if(!symbols.isEmpty()) {
         String [] strs = symbols.split("\\s*,\\s*");
         for(String str : strs) {
            if(!inList.isEmpty()) inList = inList + ",\"" + str + "\"";
            else inList = "\"" + str + "\"";
         }
         query += " symbol in (" + inList + ") ";
      } else {
         // Exclude the totals
         query += " symbol <> 'TOTAL' ";
      }
      
      query += " ORDER BY ts";

      Statement stmt = connection.createStatement();
      ResultSet rs = stmt.executeQuery(query);
      while(rs.next()) {
         if(pnl.size() != 0) {
            LocalDateTime ldt = rs.getTimestamp(1).toLocalDateTime();
            BigDecimal pp = rs.getBigDecimal(2);
            if(ldt.getYear() != pnl.getTimestamp(pnl.size()-1).getYear()) {
               // Entered a new year
               pnl.add(LocalDateTime.of(ldt.getYear(), 1, 1, 0, 0), pp);
            } else if(pp.compareTo(BigDecimal.ZERO) != 0){
               // Same year
               int id = pnl.size() - 1;
               BigDecimal prevPnl = pnl.get(id);
               pnl.set(id, prevPnl.add(pp));
            }
         } else {
            BigDecimal pp = rs.getBigDecimal(2);
            
            // Start adding PnL after the first non-zero value
            if(pp.compareTo(BigDecimal.ZERO) == 0) continue;
            
            LocalDateTime ldt = rs.getTimestamp(1).toLocalDateTime();
            pnl.add(LocalDateTime.of(ldt.getYear(), 1, 1, 0, 0), pp);
         }
      }
      
      connection.commit();
      
      return pnl;
   }
   
   class TradeTotalsBuilder
   {
      private long numTrades;

      private double grossProfits;
      private double grossLosses;

      private double mean;
      private double variance;

      private AverageAndVariance dailyPnlStats;
      private AverageAndVariance pnlStats;

      private long nonZero;
      private long positive;
      private long negative;

      private double maxWin;
      private double maxLoss;

      private Average averageWinTrade;
      private Average averageLossTrade;

      private TimeSeries<Double> pnl;
      private int pnlId;

      private double previousEquity;
      private double minEquity;
      private double maxEquity;
      private double maxDrawdown;

      TradeTotalsBuilder() {
         numTrades = 0;
         
         grossProfits = 0.0;
         grossLosses = 0.0;
         
         nonZero = 0;
         positive = 0;
         negative = 0;
         
         maxWin = 0.0;
         maxLoss = 0.0;
         
         averageWinTrade = new Average();
         averageLossTrade = new Average();
         
         minEquity = 0.0;
         maxEquity = 0.0;
         previousEquity = 0.0;
         maxDrawdown = 0.0;
         
         pnl = new TimeSeries<Double>();
         
         dailyPnlStats = new AverageAndVariance();
         pnlStats = new AverageAndVariance();
      }

      void add(long position, double pnl)
      {
         ++numTrades;
         if(pnl < 0.0) {
            ++nonZero;
            ++negative;
            averageLossTrade.add(pnl);
            grossLosses += pnl;
         }
         else if(pnl > 0.0) {
            ++nonZero;
            ++positive;
            averageWinTrade.add(pnl);
            grossProfits += pnl;
         }

         pnlStats.add(pnl);

         maxWin = Math.max(maxWin, pnl);
         maxLoss = Math.min(maxLoss, pnl);
      }

      TradeSummary summarize() {
         TradeSummary summary = new TradeSummary();
         
         summary.numTrades = numTrades;

         if(numTrades > 0) {
            summary.grossLosses = grossLosses;
            summary.grossProfits = grossProfits;
            summary.profitFactor = grossLosses != 0.0 ?
                                       Math.abs(grossProfits/grossLosses) :
                                       Math.abs(grossProfits);

            summary.averageTradePnl = pnlStats.getAverage();
            summary.tradePnlStdDev = pnlStats.getStdDev();
            summary.pctNegative = numTrades > 0 ?
                                    (double)negative/numTrades*100.0 :
                                    0.0;
            summary.pctPositive = numTrades > 0 ?
                                    (double)positive/numTrades*100.0 :
                                    0.0;

            summary.maxLoss = maxLoss;
            summary.maxWin = maxWin;
            summary.averageLoss = averageLossTrade.get();
            summary.averageWin = averageWinTrade.get();
            summary.averageWinLoss = summary.averageLoss != 0.0 ?
                                       summary.averageWin/Math.abs(summary.averageLoss) :
                                       summary.averageWin;
         }
         
         return summary;
      }
   };
   
   private class PnlPair {
      public boolean seenNonZeroPnl;
      public double pnl;
      
      public PnlPair(double d) {
         pnl = d;
         seenNonZeroPnl = pnl != 0.0;
      }
      
      public boolean seenNonZero() { return seenNonZeroPnl; }
      public double pnl() { return pnl; }
      
      public void add(double pnl) {
         this.pnl += pnl; 
         seenNonZeroPnl |= this.pnl != 0.0;
      }
   }
   
   /**
    * @throws SQLException 
    * @brief Computes total statistics for all trades for this strategy
    * in the database.
    *
    * Goes through the trades and the pnls for all instruments and
    * computes "TradeSummary". The new trade summary and the pnl are
    * inserted into the corresponding tables using the string "name"
    * as the symbol for the instrument.
    * 
    * We use "TOTAL" for "name" (unlikely to have a real symbol TOTAL),
    * but it's good to have things flexible.
    *
    * @param[in] the id to use for the entries in the various tables
    */
   protected void totalTradeStats(String name) throws SQLException {

      connectIfNecessary();
      
      getDbId();
      
      String query = "DELETE FROM pnls WHERE strategy_id=" + Long.toString(dbId) +
            " AND symbol = \"" + name + "\"";
      Statement stmt = connection.createStatement();
      stmt.executeUpdate(query);
      connection.commit();
      
      stmt.close();
      
      query = "DELETE FROM trade_summaries WHERE strategy_id=" + Long.toString(dbId) +
            " AND symbol = \"" + name + "\"";
      stmt = connection.createStatement();
      stmt.executeUpdate(query);
      connection.commit();
      
      stmt.close();
      
      // Go through each individual trade
      TradeTotalsBuilder shortsBuilder = new TradeTotalsBuilder();
      TradeTotalsBuilder longsBuilder = new TradeTotalsBuilder();
      TradeTotalsBuilder allBuilder = new TradeTotalsBuilder();
      
      query = "SELECT initial_position,pnl FROM trades WHERE strategy_id=" + Long.toString(dbId);
      stmt = connection.createStatement();
      ResultSet rs = stmt.executeQuery(query);
      while(rs.next()) {
         long position = rs.getLong(1);
         double pnl = rs.getDouble(2);
         if(position < 0) {
            shortsBuilder.add(position, pnl);
            allBuilder.add(position, pnl);
         } else {
            longsBuilder.add(position, pnl);
            allBuilder.add(position, pnl);
         }
      }
      stmt.close();
      
      // The pair tells us:
      //    1. Weather we have seen non-zero PnL for that timestamp
      //    2. The total PnL
      TreeMap<LocalDateTime,PnlPair> pnlMap = new TreeMap<LocalDateTime, PnlPair>();
      query = "SELECT ts,pnl FROM pnls WHERE strategy_id=?";
      PreparedStatement pstmt = connection.prepareStatement(query);
      pstmt.setLong(1, dbId);
      rs = pstmt.executeQuery(); 
      while(rs.next()) {
         LocalDateTime ldt = rs.getTimestamp(1).toLocalDateTime();
         double pnl = rs.getDouble(2);
         PnlPair pp = pnlMap.get(ldt);
         if(pp != null) {
            pp.add(pnl);
         } else {
            pnlMap.put(ldt, new PnlPair(pnl));
         }
      }
      pstmt.close();
      
      // Write the total PnL and collect the basic per-bar and equity statistics
      query = "INSERT INTO pnls (strategy_id,symbol,ts,pnl) values (?,?,?,?)";
      pstmt = connection.prepareStatement(query);
      pstmt.setLong(1, dbId);
      pstmt.setString(2, name);
      
      AverageAndVariance barStats = new AverageAndVariance();
      
      double equity = 0.0;
      double maxEquity = Double.MIN_VALUE;
      double minEquity = Double.MAX_VALUE;
      double maxDrawdown = Double.MAX_VALUE;
      
      for(Map.Entry<LocalDateTime,PnlPair> entry : pnlMap.entrySet()) {
         PnlPair pp = entry.getValue();
         double pnl = pp.pnl();

         // Write the PnL
         pstmt.setTimestamp(3, Timestamp.valueOf(entry.getKey()));
         pstmt.setDouble(4, pnl);
         // pstmt.executeUpdate();
         pstmt.addBatch();
         
         // Collect statistics
         if(pp.seenNonZero()) barStats.add(pnl);
         
         equity += pnl;
         maxEquity = Math.max(maxEquity, equity);
         minEquity = Math.min(minEquity, equity);
         maxDrawdown = Math.min(maxDrawdown, equity - maxEquity);
      }
      
      pstmt.executeBatch();
      connection.commit();
      pstmt.close();
      
      // Write out the total as a trade summary
      TradeSummary summary = allBuilder.summarize();
      summary.equityMin = minEquity;
      summary.equityMax = maxEquity;
      summary.maxDrawdown = maxDrawdown;

      summary.averageDailyPnl = barStats.getAverage();
      summary.dailyPnlStdDev = barStats.getStdDev();
      summary.sharpeRatio = Functions.sharpeRatio(summary.averageDailyPnl, summary.dailyPnlStdDev, 252);
      
      writeTradeSummary(name, "All", summary);
      
      // For the shorts and longs totals we don't have equityMin, equityMax, etc
      writeTradeSummary(name, "Long", longsBuilder.summarize());
      writeTradeSummary(name, "Short", shortsBuilder.summarize());
      
      connection.commit();
   }
   
   public void totalTradeStats() throws Exception {
      totalTradeStats("TOTAL");
   }
   
   // public Portfolio getPortfolio() { return portfolio; }
   public Account getAccount() {return account; }
   
   protected class Status {
      public long getId() { return id; }
      public void setId(long id) { this.id = id; }
      
      public long getStrategyId() { return strategyId; }
      public void setStrategyId(long strategyId) { this.strategyId = strategyId; }
      
      public String getSymbol() { return symbol; }
      public void setSymbol(String symbol) { this.symbol = symbol; }
      
      public LocalDateTime getDateTime() { return ts; }
      public void setDateTime(LocalDateTime ts) { this.ts = ts; }
      
      public double getPosition() { return position; }
      public void setPosition(double position) { this.position = position; }
      
      public double getPnl() { return pnl; }
      public void setPnl(double pnl) { this.pnl = pnl; }
      
      public double getLastClose() { return lastClose; }
      public void setLastClose(double lastClose) { this.lastClose = lastClose; }
      
      public LocalDateTime getLastDateTime() { return lastDateTime; }
      public void setLastDateTime(LocalDateTime ldt) { this.lastDateTime = ldt; }
      
      public double getEntryPrice() { return entryPrice; }
      public void setEntryPrice(double entryPrice) { this.entryPrice = entryPrice; }
      
      public double getEntryRisk() { return entryRisk; }
      public void setEntryRisk(double entryRisk) { this.entryRisk = entryRisk; }
      
      public LocalDateTime getEntryDateTime() { return since; }
      public void setEntryDateTime(LocalDateTime ldt) { this.since = ldt; }
      
      public void addOrder(Order order) { orders.add(order); }
      
      public Status(String symbol) {
         setSymbol(symbol);
         orders = new ArrayList<Order>();
         numericProperties = new HashMap<String, Double>();
      }
      
      public Status(int strategyId, String symbol) {
         this(symbol);
         setStrategyId(strategyId);
      }
      
      public void addProperty(String name, double value) {
         numericProperties.put(name, value);
      }
      
      public void persist(Connection con) throws Exception {
         
         // Build the JSON status
         JsonObject jo = new JsonObject();
         assert getPosition() != Double.NaN : "Position must not be NaN!";
         jo.addProperty("position", getPosition());
         if(!Double.isNaN(getPnl())) {
            jo.addProperty("pnl", getPnl());
         }
         jo.addProperty("last_close", getLastClose());
         if(!Double.isNaN(getEntryPrice())) {
            jo.addProperty("entry_price", getEntryPrice());
         }
         numericProperties.forEach((k, v) -> jo.addProperty(k, v));
         JsonArray ordersArray = new JsonArray();
         for(Order oo : orders) {
            ordersArray.add(oo.toJsonString());
         }
         jo.add("orders", ordersArray);
         Gson gson = new GsonBuilder().setPrettyPrinting()
                           .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                           .create();
         
         String query = " INSERT INTO strategy_positions " +
               " (strategy_id,symbol,ts,position,last_close,last_ts,details) " +
               " VALUES(?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE " +
               " position=?,last_close=?,ts=?,details=?";
         String jsonString = gson.toJson(jo);
         PreparedStatement stmt = con.prepareStatement(query);
         stmt.setLong(1, getStrategyId());
         stmt.setString(2, getSymbol());
         stmt.setTimestamp(3, Timestamp.valueOf(getDateTime()));
         stmt.setDouble(4, getPosition());
         stmt.setDouble(5, getLastClose());
         stmt.setTimestamp(6, Timestamp.valueOf(getLastDateTime()));
         stmt.setString(7, jsonString);
         stmt.setDouble(8, getPosition());
         stmt.setDouble(9, getLastClose());
         stmt.setTimestamp(10, Timestamp.valueOf(getDateTime()));
         stmt.setString(11, jsonString);
         
         stmt.executeUpdate();
         
         con.commit();
      }
      
      private long id;
      private long strategyId;
      private String symbol;
      private LocalDateTime ts = null;
      private double position;
      private LocalDateTime since = null;
      private double pnl;
      private double lastClose;
      private LocalDateTime lastDateTime = null;
      private double entryPrice;
      private double entryRisk;
      private List<Order> orders = null;
      private HashMap<String, Double> numericProperties = null;
   }
   
   public void persistStatus(Strategy.Status status) throws Exception {
      connectIfNecessary();
      status.persist(connection);
   }
   
   public TradeSummary getSummary(String symbol, String type) throws SQLException {
      TradeSummary summary = new TradeSummary();
      
      connectIfNecessary();
      
      String query = " SELECT num_trades, gross_profits, gross_losses, profit_factor, " +
                     "      average_daily_pnl, daily_pnl_stddev, sharpe_ratio, " +
                     "      average_trade_pnl, trade_pnl_stddev, pct_positive, " +
                     "      pct_negative, max_win, max_loss, average_win, average_loss, " +
                     "      average_win_loss, equity_min, equity_max, max_drawdown " +
                     " FROM trade_summaries " +
                     " WHERE strategy_id = ? AND symbol = ? AND type = ?";
      PreparedStatement stmt = connection.prepareStatement(query);
      stmt.setLong(1, dbId);
      stmt.setString(2, symbol);
      stmt.setString(3, type);
      
      ResultSet rs = stmt.executeQuery();
      if(!rs.next()) return null;
      
      summary.numTrades = rs.getLong(1);
      summary.grossProfits = rs.getDouble(2);
      summary.grossLosses = rs.getDouble(3);
      summary.profitFactor = rs.getDouble(4);
      summary.averageDailyPnl = rs.getDouble(5);
      summary.dailyPnlStdDev = rs.getDouble(6);
      summary.sharpeRatio = rs.getDouble(7);
      summary.averageTradePnl = rs.getDouble(8);
      summary.tradePnlStdDev = rs.getDouble(9);
      summary.pctPositive = rs.getDouble(10);
      summary.pctNegative = rs.getDouble(11);
      summary.maxWin = rs.getDouble(12);
      summary.maxLoss = rs.getDouble(13);
      summary.averageWin = rs.getDouble(14);
      summary.averageLoss = rs.getDouble(15);
      summary.averageWinLoss = rs.getDouble(16);
      summary.equityMin = rs.getDouble(17);
      summary.equityMax = rs.getDouble(18);
      summary.maxDrawdown = rs.getDouble(19);
      
      connection.commit();
      return summary;
   }
   
   public JsonObject writeStrategyReport() throws Exception {
      // Annual statistics
      Series annualStats = getAnnualStats();
      
      JsonObject result = new JsonObject();
      
      if(annualStats.size() > 0) {
      
         Average avgPnl = new Average();
         Average avgPnlPct = new Average();
         Average avgDD = new Average();
         Average avgDDPct = new Average();
         JsonArray asa = new JsonArray();
         
         for(int ii = 0; ii < annualStats.size(); ++ii) {
            JsonObject ajo = new JsonObject();
            ajo.addProperty("year", annualStats.getTimestamp(ii).getYear());
            ajo.addProperty("pnl", Math.round(annualStats.get(ii, 0)));
            ajo.addProperty("pnl_pct", annualStats.get(ii, 1)*100.0);
            ajo.addProperty("end_equity", Math.round(annualStats.get(ii, 2)));
            ajo.addProperty("maxdd", Math.round(annualStats.get(ii, 3)));
            ajo.addProperty("maxdd_pct", annualStats.get(ii, 4)*100.0);
            asa.add(ajo);
            
            avgPnl.add(annualStats.get(ii, 0));
            avgPnlPct.add(annualStats.get(ii, 1));
            avgDD.add(Math.abs(annualStats.get(ii,3)));
            avgDDPct.add(Math.abs(annualStats.get(ii, 4)));
         }
         
         result.add("annual_stats", asa);
         
         result.addProperty("pnl", Math.round(avgPnl.get()));
         result.addProperty("pnl_pct", avgPnlPct.get()*100.0);
         result.addProperty("maxdd", Math.round(avgDD.get()));
         result.addProperty("maxdd_pct", avgDDPct.get()*100.0);
         result.addProperty("gain_to_pain", avgPnl.get() / avgDD.get());
      }
      
      // Global statistics
      LocalDateTime maxDateTime = LocalDateTime.MIN;
      double maxEndEq = Double.MIN_VALUE;
      
      Series equity = getEquity();
      for(int ii = 0; ii < equity.size(); ++ii) {
         if(equity.get(ii) > maxEndEq) {
            maxEndEq = equity.get(ii);
            maxDateTime = equity.getTimestamp(ii);
         }
      }
      
      double lastDD = maxEndEq - equity.get(equity.size()-1);
      double lastDDPct = lastDD/maxEndEq*100;
      
      JsonObject jo = new JsonObject();
      jo.addProperty("cash", lastDD);
      jo.addProperty("pct", lastDDPct);
      
      result.add("total_maxdd", jo);
      
      jo = new JsonObject();
      jo.addProperty("date", maxDateTime.format(DateTimeFormatter.ISO_DATE));
      jo.addProperty("equity", Math.round(maxEndEq));
      
      result.add("total_peak", jo);
      
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
            
            jo = new JsonObject();
            jo.addProperty("cash", lastDD);
            jo.addProperty("pct", lastDDPct);
            
            result.add("latest_maxdd", jo);
            
            jo = new JsonObject();
            jo.addProperty("date", maxDateTime.format(DateTimeFormatter.ISO_DATE));
            jo.addProperty("equity", Math.round(maxEndEq));
            
            result.add("latest_peak", jo);
         }
      }
      
      TradeSummary summary = getSummary("TOTAL", "All");
      result.addProperty("avg_trade_pnl", Math.round(summary.averageTradePnl));
      result.addProperty("maxdd", Math.round(summary.maxDrawdown));
      result.addProperty("num_trades", summary.numTrades);
      
      Gson gson = new GsonBuilder()
                        .setPrettyPrinting()
                        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                        .create();

      connectIfNecessary();
      
      String query = " INSERT INTO strategy_report (strategy_id,last_date,report) " +
                     " VALUES(?,?,?) ON DUPLICATE KEY UPDATE report=?";
      PreparedStatement stmt = connection.prepareStatement(query);
      stmt.setLong(1, dbId);
      stmt.setTimestamp(2, Timestamp.valueOf(getLastTimestamp()));
      String jsonString = gson.toJson(result); 
      stmt.setString(3, jsonString);
      stmt.setString(4, jsonString);
      stmt.executeUpdate();
      
      connection.commit();
      
      return result;
   }
   
   public Order enterLong(String symbol, long quantity, String signal) throws Exception {
      Order order = Order.enterLong(symbol, quantity, signal);
      broker.submitOrder(order);
      return order;
   }
   
   public Order enterLong(String symbol, long quantity) throws Exception {
      Order order = Order.enterLong(symbol, quantity);
      broker.submitOrder(order);
      return order;
   }
   
   public Order enterLongStop(String symbol, double stopPrice,long quantity) throws Exception {
      assert quantity > 0;
      assert broker != null;

      Order order = Order.enterLongStop(symbol, quantity, stopPrice);
      broker.submitOrder(order);
      return order;
   }

   public Order enterLongStop(String symbol, double stopPrice, long quantity, String signal) throws Exception {
      assert quantity > 0;
      assert broker != null;

      Order order = Order.enterLongStop(symbol, quantity, stopPrice, signal);
      broker.submitOrder(order);
      return order;
   }
   
   public Order enterLongStop(String symbol, double stopPrice, long quantity, String signal, int barsValidFor) throws Exception {
      assert quantity > 0;
      assert broker != null;

      Order order = Order.enterLongStop(symbol, quantity, stopPrice, signal);
      order.setExpiration(barsValidFor);
      broker.submitOrder(order);
      return order;
   }

   public Order enterShortStop(String symbol, double stopPrice, long quantity) throws Exception {
      assert quantity > 0;
      assert broker != null;

      Order order = Order.enterShortStop(symbol, quantity, stopPrice);
      broker.submitOrder(order);
      
      return order;
   }

   public Order enterShortStop(String symbol, double stopPrice, long quantity, String signal) throws Exception {
      assert quantity > 0;
      assert broker != null;

      Order order = Order.enterShortStop(symbol, quantity, stopPrice, signal);
      broker.submitOrder(order);
      
      return order;
   }
   
   public Order enterShortStop(String symbol, double stopPrice, long quantity, String signal, int barsValidFor) throws Exception {
      assert quantity > 0;
      assert broker != null;

      Order order = Order.enterShortStop(symbol, quantity, stopPrice, signal);
      order.setExpiration(barsValidFor);
      broker.submitOrder(order);
      
      return order;
   }

   public Order enterShort(String symbol, long quantity, String signal) throws Exception {
      Order order = Order.enterShort(symbol, quantity, signal);
      broker.submitOrder(order);
      return order;
   }
   
   public Order enterShort(String symbol, long quantity) throws Exception {
      Order order = Order.enterShort(symbol, quantity);
      broker.submitOrder(order);
      return order;
   }
   
   public Order exitShort(String symbol) throws Exception {
      Order order = Order.exitShort(symbol, Order.POSITION_QUANTITY);
      broker.submitOrder(order);
      return order;
   }

   public Order exitShort(String symbol, long quantity) throws Exception {
      Order order = Order.exitShort(symbol, quantity);
      broker.submitOrder(order);
      return order;
   }

   public Order exitShort(String symbol, long quantity, String signal) throws Exception {
      Order order = Order.exitShort(symbol, quantity, signal);
      broker.submitOrder(order);
      return order;
   }
   
   public Order exitLong(String symbol) throws Exception {
      Order order = Order.exitLong(symbol, Order.POSITION_QUANTITY);
      broker.submitOrder(order);
      return order;
   }

   public Order exitLong(String symbol, long quantity) throws Exception {
      Order order = Order.exitLong(symbol, quantity);
      broker.submitOrder(order);
      return order;
   }

   public Order exitLong(String symbol, long quantity, String signal) throws Exception {
      Order order = Order.exitLong(symbol, quantity, signal);
      broker.submitOrder(order);
      return order;
   }
   
   public Order exitLongStop(String symbol, double stopPrice, long quantity) throws Exception {
      assert quantity > 0;
      assert broker != null;

      Order order = Order.exitLongStop(symbol, quantity, stopPrice);
      broker.submitOrder(order);
      
      return order;
   }
   
   public Order exitLongStop(String symbol, double stopPrice, long quantity, String signal) throws Exception {
      assert quantity > 0;
      assert broker != null;

      Order order = Order.exitLongStop(symbol, quantity, stopPrice, signal);
      broker.submitOrder(order);
      
      return order;
   }
   
   public Order exitLongStop(String symbol, double stopPrice, long quantity, String signal, int barsValidFor) throws Exception {
      assert quantity > 0;
      assert broker != null;

      Order order = Order.exitLongStop(symbol, quantity, stopPrice, signal);
      order.setExpiration(barsValidFor);
      broker.submitOrder(order);
      
      return order;
   }
   
   public Order exitShortStop(String symbol, double stopPrice, long quantity) throws Exception {
      assert quantity > 0;
      assert broker != null;

      Order order = Order.exitShortStop(symbol, quantity, stopPrice);
      broker.submitOrder(order);
      
      return order;
   }
   
   public Order exitShortStop(String symbol, double stopPrice, long quantity, String signal) throws Exception {
      assert quantity > 0;
      assert broker != null;

      Order order = Order.exitShortStop(symbol, quantity, stopPrice, signal);
      broker.submitOrder(order);
      
      return order;
   }
   
   public Order exitShortStop(String symbol, double stopPrice, long quantity, String signal, int barsValidFor) throws Exception {
      assert quantity > 0;
      assert broker != null;

      Order order = Order.exitShortStop(symbol, quantity, stopPrice, signal);
      order.setExpiration(barsValidFor);
      broker.submitOrder(order);
      
      return order;
   }
   
   public Order enterLongStopLimit(String symbol, double stopPrice, double limitPrice, long quantity, String signal, int barsValidFor) throws Exception
   {
      assert quantity > 0;
      assert broker != null;

      Order order = Order.enterLongStopLimit(symbol, quantity, stopPrice, limitPrice, signal);
      order.setExpiration(barsValidFor);
      broker.submitOrder(order);
      
      return order;
   }
   
   public Order enterShortStopLimit(String symbol, double stopPrice, double limitPrice, long quantity, String signal, int barsValidFor) throws Exception
   {
      assert quantity > 0;
      assert broker != null;

      Order order = Order.enterShortStopLimit(symbol, quantity, stopPrice, limitPrice, signal);
      order.setExpiration(barsValidFor);
      broker.submitOrder(order);
      
      return order;
   }
   
   public void start() throws Exception {
      getBroker().start();
   }
   
   public void finalize() throws Exception {
      
   }
   
   public void setTradingStart(LocalDateTime ldt) {tradingStart = ldt; }
   public LocalDateTime getTradingStart() { return tradingStart; }
   
   public void updateEndEquity() { getAccount().updateEndEquity(); }
}
