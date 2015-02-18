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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
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
   protected Portfolio portfolio = new Portfolio();
   protected EventBus eventBus;
   
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
      
      Connection con = DriverManager.getConnection(dbUrl);
      con.setAutoCommit(false);
      
      // Load the strategy unique id from the "strategies" table
      getDbId(con);

      String query = "DELETE FROM executions WHERE strategy_id=?"; 
      PreparedStatement stmt = con.prepareStatement(query);
      stmt.setLong(1, dbId);
      stmt.executeUpdate();
      stmt.close();
      
      query = "DELETE FROM trades WHERE strategy_id=?"; 
      stmt = con.prepareStatement(query);
      stmt.setLong(1, dbId);
      stmt.executeUpdate();
      stmt.close();
      
      query = "DELETE FROM pnls WHERE strategy_id=?"; 
      stmt = con.prepareStatement(query);
      stmt.setLong(1, dbId);
      stmt.executeUpdate();
      stmt.close();
      
      query = "DELETE FROM trade_summaries WHERE strategy_id=?"; 
      stmt = con.prepareStatement(query);
      stmt.setLong(1, dbId);
      stmt.executeUpdate();
      stmt.close();
      
      query = "DELETE FROM strategy_positions WHERE strategy_id=?"; 
      stmt = con.prepareStatement(query);
      stmt.setLong(1, dbId);
      stmt.executeUpdate();
      stmt.close();
      
      con.commit();
   }
   
   public void getDbId(Connection con) throws SQLException {
      if(dbId >= 0) return;

      // Get the strategy id
      String query = "SELECT id FROM strategies where name=?";
      PreparedStatement stmt = con.prepareStatement(query);
      stmt.setString(1, name);
      ResultSet rs = stmt.executeQuery();
      if(!rs.next()) {
         // The name doesn't exist, insert it
         rs.close();
         stmt.close();
         stmt = con.prepareStatement("INSERT INTO strategies(name) values (?)");
         stmt.setString(1, name);
         // Ignore errors, some other process may insert the strategy.
         try {
            stmt.executeUpdate();
         } catch(Exception e) {
         }
         stmt.close();
         // Repeat the query
         stmt = con.prepareStatement(query);
         stmt.setString(1, name);
         rs = stmt.executeQuery();
         rs.next();
      }
      
      dbId = rs.getLong(1);
      rs.close();
      stmt.close();
   }
   
   public void writeExecutions() throws SQLException {
      Connection con = DriverManager.getConnection(dbUrl);
      con.setAutoCommit(false);

      getDbId(con);
      
      String query = "INSERT INTO executions(symbol,strategy_id,ts,price,quantity,signal_name) VALUES (?,?,?,?,?,?)";
      PreparedStatement stmt = con.prepareStatement(query);
      for(Execution execution : executions) {
         stmt.setString(1, execution.getSymbol());
         stmt.setLong(2, dbId);
         stmt.setTimestamp(3, Timestamp.valueOf(execution.getDateTime()));
         stmt.setDouble(4, execution.getPrice());
         stmt.setLong(5, execution.getQuantity());
         stmt.setString(6, execution.getSignal());
         stmt.executeUpdate();
      }
      con.commit();
   }
   
   public void writeTrades(Instrument instrument) throws SQLException {
      BarHistory history = barData.getHistory(instrument.getSymbol(), Duration.ofDays(1));
      TimeSeries<Double> close = history.getCloseSeries();
      TimeSeries<Double> pnl = portfolio.getPnl(instrument, close);
      if(pnl.size() == 0) return;
      
      Connection con = DriverManager.getConnection(dbUrl);
      con.setAutoCommit(false);

      getDbId(con);
      
      // Write the PnL
      String query = " INSERT INTO pnls(strategy_id,symbol,ts,pnl) VALUE (?,?,?,?) " +
                     " ON DUPLICATE KEY UPDATE pnl=?";
      PreparedStatement stmt = con.prepareStatement(query);
      stmt.setLong(1, dbId);
      stmt.setString(2, instrument.getSymbol());
      for(int ii = 0; ii < pnl.size(); ++ii) {
         stmt.setTimestamp(3, Timestamp.valueOf(pnl.getTimestamp(ii)));
         stmt.setDouble(4, pnl.get(ii));
         stmt.setDouble(5, pnl.get(ii));
         stmt.executeUpdate();
      }
      con.commit();
      stmt.close();

      Portfolio.TradingResults tr = portfolio.getTradingResults(instrument, close);
      // Write the trade statistics
      if(tr.stats.size() > 0) {
         
         query = " INSERT INTO trades(strategy_id,symbol,start,end,initial_position, " +
                 "       max_position,num_transactions,pnl,pct_pnl,tick_pnl,fees) " +
                 " VALUES(?,?,?,?,?,?,?,?,?,?,?)";
         stmt = con.prepareStatement(query);
         stmt.setLong(1, dbId);
         stmt.setString(2, instrument.getSymbol());
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
            stmt.executeUpdate();
         }
         con.commit();
         stmt.close();
      }
      
      writeTradeSummary(con, instrument, "All", tr.all);
      writeTradeSummary(con, instrument, "Long", tr.longs);
      writeTradeSummary(con, instrument, "Short", tr.shorts);
      
      con.commit();
   }
   
   protected void writeTradeSummary(Connection con, String symbol, String type, TradeSummary tradeSummary) throws SQLException {
      if(tradeSummary.numTrades > 0) {
         String query = " INSERT INTO trade_summaries (strategy_id,symbol,type,num_trades,gross_profits, " +
                        "      gross_losses,profit_factor,average_daily_pnl,daily_pnl_stddev,sharpe_ratio, " +
                        "      average_trade_pnl,trade_pnl_stddev,pct_positive,pct_negative,max_win,max_loss, " +
                        "      average_win,average_loss,average_win_loss,equity_min,equity_max,max_drawdown) " +
                        " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
         PreparedStatement stmt = con.prepareStatement(query);
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
         con.commit();
      }
   }
   
   private void setDoubleParam(PreparedStatement stmt, int index, double value) throws SQLException {
      if(!Double.isNaN(value)) {
         stmt.setDouble(index, value);
      } else {
         stmt.setNull(index, Types.DOUBLE);
      }
   }
   
   protected void writeTradeSummary(Connection con, Instrument instrument, String type, TradeSummary tradeSummary) throws SQLException {
      writeTradeSummary(con, instrument.getSymbol(), type, tradeSummary);
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
   }

   public void orderExecutedHandler(OrderNotification on) throws Exception {
      executions.add(on.execution);
      portfolio.addTransaction(on.execution);
      onOrderNotification(on);
   }

   /**
    * @brief Get basic statistics used to evaluate performance.
    * 
    * For a quick strategy evaluation, I currently use the approach from
    * "Building Reliable Trading Systems", by Keith Fitschen. 
    *    * PnL (annual and total)
    *    * Drawdown (max and average annual max)
    *    
    * @return A time series with two columns - PnL and MaxDrawdown
    * @throws SQLException 
    */
   public TimeSeries<Double> getAnnualStats() throws SQLException {
      TimeSeries<Double> annualStats = new TimeSeries<Double>(2);
      
      Connection con = DriverManager.getConnection(dbUrl);
      con.setAutoCommit(false);

      getDbId(con);
      
      String query = "SELECT ts,pnl FROM pnls WHERE strategy_id=" + Long.toString(dbId) +
               " AND symbol = 'TOTAL' ORDER BY ts ASC";
      Statement stmt = con.createStatement();
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
               annualStats.add(last, equity, maxDrawdown);
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
         annualStats.add(last, equity, maxDrawdown);
      }
      
      con.commit();
      
      return annualStats;
   }
   
   public TimeSeries<BigDecimal> getAnnualPnl(String symbols) throws SQLException {
      TimeSeries<BigDecimal> pnl = new TimeSeries<BigDecimal>(1);
      
      Connection con = DriverManager.getConnection(dbUrl);
      con.setAutoCommit(false);

      getDbId(con);
      
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

      Statement stmt = con.createStatement();
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
      
      con.commit();
      
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
         seenNonZeroPnl |= this.pnl != 0;
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
   void totalTradeStats(String name) throws SQLException {
      Connection con = DriverManager.getConnection(dbUrl);
      con.setAutoCommit(false);

      getDbId(con);
      
      String query = "DELETE FROM pnls WHERE strategy_id=" + Long.toString(dbId) +
            " AND symbol = \"" + name + "\"";
      Statement stmt = con.createStatement();
      stmt.executeUpdate(query);
      con.commit();
      
      stmt.close();
      
      query = "DELETE FROM trade_summaries WHERE strategy_id=" + Long.toString(dbId) +
            " AND symbol = \"" + name + "\"";
      stmt = con.createStatement();
      stmt.executeUpdate(query);
      con.commit();
      
      stmt.close();
      
      // Go through each individual trade
      TradeTotalsBuilder shortsBuilder = new TradeTotalsBuilder();
      TradeTotalsBuilder longsBuilder = new TradeTotalsBuilder();
      TradeTotalsBuilder allBuilder = new TradeTotalsBuilder();
      
      query = "SELECT initial_position,pnl FROM trades WHERE strategy_id=" + Long.toString(dbId);
      stmt = con.createStatement();
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
      
      // The pair tells us:
      //    1. Weather we have seen non-zero PnL for that timestamp
      //    2. The total PnL
      TreeMap<LocalDateTime,PnlPair> pnlMap = new TreeMap<LocalDateTime, PnlPair>();
      query = "SELECT ts,pnl FROM pnls WHERE strategy_id=" + Long.toString(dbId);
      stmt = con.createStatement();
      rs = stmt.executeQuery(query);
      while(rs.next()) {
         LocalDateTime ldt = rs.getTimestamp(1).toLocalDateTime();
         double pnl = rs.getDouble(2);
         PnlPair pp = pnlMap.get(ldt);
         if(pp != null) {
            pp.add(pnl);
            pnlMap.put(ldt, pp);
         } else {
            pnlMap.put(ldt, new PnlPair(pnl));
         }
      }
      stmt.close();
      
      // Write the total PnL and collect the basic per-bar and equity statistics
      query = "INSERT INTO pnls (strategy_id,symbol,ts,pnl) values (?,?,?,?)";
      PreparedStatement pstmt = con.prepareStatement(query);
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
         pstmt.executeUpdate();
         
         // Collect statistics
         if(pp.seenNonZero()) barStats.add(pnl);
         
         equity += pnl;
         maxEquity = Math.max(maxEquity, equity);
         minEquity = Math.min(minEquity, equity);
         maxDrawdown = Math.min(maxDrawdown, equity - maxEquity);
      }
      
      con.commit();
      pstmt.close();
      
      // Write out the total as a trade summary
      TradeSummary summary = allBuilder.summarize();
      summary.equityMin = minEquity;
      summary.equityMax = maxEquity;
      summary.maxDrawdown = maxDrawdown;

      summary.averageDailyPnl = barStats.getAverage();
      summary.dailyPnlStdDev = barStats.getStdDev();
      summary.sharpeRatio = Functions.sharpeRatio(summary.averageDailyPnl, summary.dailyPnlStdDev, 252);
      
      writeTradeSummary(con, name, "All", summary);
      
      // For the shorts and longs totals we don't have equityMin, equityMax, etc
      writeTradeSummary(con, name, "Long", longsBuilder.summarize());
      writeTradeSummary(con, name, "Short", shortsBuilder.summarize());
      
      con.commit();
   }
   
   public void totalTradeStats() throws Exception {
      totalTradeStats("TOTAL");
   }
   
   public Portfolio getPortfolio() { return portfolio; }
   
   protected class Status {
      public long getId() { return id_; }
      public void setId(long id) { id_ = id; }
      
      public long getStrategyId() { return strategyId_; }
      public void setStrategyId(long strategyId) { strategyId_ = strategyId; }
      
      public String getSymbol() { return symbol_; }
      public void setSymbol(String symbol) { symbol_ = symbol; }
      
      public LocalDateTime getDateTime() { return ts_; }
      public void setDateTime(LocalDateTime ts) { ts_ = ts; }
      
      public double getPosition() { return position_; }
      public void setPosition(double position) { position_ = position; }
      
      public double getPnl() { return pnl_; }
      public void setPnl(double pnl) { pnl_ = pnl; }
      
      public double getLastClose() { return lastClose_; }
      public void setLastClose(double lastClose) { lastClose_ = lastClose; }
      
      public double getEntryPrice() { return entryPrice_; }
      public void setEntryPrice(double entryPrice) { entryPrice_ = entryPrice; }
      
      public double getEntryRisk() { return entryRisk_; }
      public void setEntryRisk(double entryRisk) { entryRisk_ = entryRisk; }
      
      public LocalDateTime getEntryDateTime() { return since_; }
      public void setEntryDateTime(LocalDateTime ldt) { since_ = ldt; }
      
      public void addOrder(Order order) { orders_.add(order); }
      
      public Status(String symbol) {
         setSymbol(symbol);
         orders_ = new ArrayList<Order>();
         numericProperties = new HashMap<String, Double>();
      }
      
      public Status(int strategyId, String symbol) {
         this(symbol);
         setStrategyId(strategyId);
      }
      
      public void addProperty(String name, double value) {
         numericProperties.put(name, value);
      }
      
      public void persist(String dbUrl) throws Exception {
         
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
         for(Order oo : orders_) {
            ordersArray.add(oo.toJsonString());
         }
         jo.add("orders", ordersArray);
         Gson gson = new GsonBuilder().setPrettyPrinting()
                           .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                           .create();
         
         Connection con = DriverManager.getConnection(dbUrl);
         con.setAutoCommit(false);
         
         String query = "INSERT INTO strategy_positions " +
               "(strategy_id,symbol,ts,details) values(?,?,?,?)";
         PreparedStatement stmt = con.prepareStatement(query);
         stmt.setLong(1, getStrategyId());
         stmt.setString(2, getSymbol());
         stmt.setTimestamp(3, Timestamp.valueOf(getDateTime()));
         stmt.setString(4, gson.toJson(jo));
         
         stmt.executeUpdate();
         
         con.commit();
         con.close();
      }
      
      private long id_;
      private long strategyId_;
      private String symbol_;
      private LocalDateTime ts_;
      private double position_;
      private LocalDateTime since_;
      private double pnl_;
      private double lastClose_;
      private double entryPrice_;
      private double entryRisk_;
      private List<Order> orders_;
      private HashMap<String, Double> numericProperties;
   }
   
   public void enterLong(String symbol, long quantity, String signal) throws Exception {
      broker.submitOrder(Order.enterLong(symbol, quantity, signal));
   }
   
   public void enterLong(String symbol, long quantity) throws Exception {
      broker.submitOrder(Order.enterLong(symbol, quantity));
   }
   
   public void enterShort(String symbol, long quantity, String signal) throws Exception {
      broker.submitOrder(Order.enterShort(symbol, quantity, signal));
   }
   
   public void enterShort(String symbol, long quantity) throws Exception {
      broker.submitOrder(Order.enterShort(symbol, quantity));
   }
   
   public void exitShort(String symbol) throws Exception {
      broker.submitOrder(Order.exitShort(symbol, Order.POSITION_QUANTITY));
   }

   public void exitShort(String symbol, long quantity) throws Exception {
      broker.submitOrder(Order.exitShort(symbol, quantity));
   }

   public void exitShort(String symbol, long quantity, String signal) throws Exception {
      broker.submitOrder(Order.exitShort(symbol, quantity, signal));
   }
   
   public void exitLong(String symbol) throws Exception {
      broker.submitOrder(Order.exitLong(symbol, Order.POSITION_QUANTITY));
   }

   public void exitLong(String symbol, long quantity) throws Exception {
      broker.submitOrder(Order.exitLong(symbol, quantity));
   }

   public void exitLong(String symbol, long quantity, String signal) throws Exception {
      broker.submitOrder(Order.exitLong(symbol, quantity, signal));
   }
   
   public void enterLongStopLimit(String symbol, double stopPrice, double limitPrice, long quantity, String signal, int barsValidFor) throws Exception
   {
      assert quantity > 0;
      assert broker != null;

      Order order = Order.enterLongStopLimit(symbol, quantity, stopPrice, limitPrice, signal);
      order.setExpiration(barsValidFor);
      broker.submitOrder(order);
   }
   
   public void enterShortStopLimit(String symbol, double stopPrice, double limitPrice, long quantity, String signal, int barsValidFor) throws Exception
   {
      assert quantity > 0;
      assert broker != null;

      Order order = Order.enterShortStopLimit(symbol, quantity, stopPrice, limitPrice, signal);
      order.setExpiration(barsValidFor);
      broker.submitOrder(order);
   }
}
