package org.tradelib.core;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;

public class MySQLDataFeed extends HistoricalDataFeed {
   
   private Properties config;
   
   public MySQLDataFeed(Context context) {
      super(context);
   }

   /**
    * Configures the data feed using a property file.
    * 
    * @param path The config path
    * @throws Exception 
    */
   @Override
   public void configure(String path) throws Exception {
      this.config = new Properties();
      this.config.load(new FileInputStream(path));
   }

   @Override
   public void start() throws Exception {
      if(subscriptions.size() == 0) return;

      Iterator<String> it = subscriptions.iterator();
      String symbols = "\"" + it.next() + "\"";
      while(it.hasNext()) {
         symbols = symbols + ",\"" + it.next() + "\""; 
      }
      
      Connection con = DriverManager.getConnection(config.getProperty("db.url"));
      
      String query = "SELECT symbol,ts,open,high,low,close,contract_interest,volume,total_interest " +
                     "FROM " + config.getProperty("bars.table") + " " +
                     "WHERE symbol IN (" + symbols + ") ";
      if(getFeedStart() != null) query += " AND ts >= DATE(?)";
      if(getFeedStop() != null) query += " AND ts <= DATE(?)";
      query += " ORDER BY ts ASC";

      PreparedStatement stmt = con.prepareStatement(query);
      int index = 1;
      if(getFeedStart() != null) stmt.setTimestamp(index++, Timestamp.valueOf(getFeedStart()));;
      if(getFeedStop() != null) stmt.setTimestamp(index++, Timestamp.valueOf(getFeedStop()));;
      
      ResultSet rs = stmt.executeQuery();
      boolean more = rs.next();
      while(more) {
         String symbol = rs.getString(1);
         Bar bar = new Bar(symbol, rs.getTimestamp(2).toLocalDateTime(),
                           rs.getBigDecimal(3).doubleValue(),
                           rs.getBigDecimal(4).doubleValue(),
                           rs.getBigDecimal(5).doubleValue(),
                           rs.getBigDecimal(6).doubleValue(),
                           rs.getLong(7), rs.getLong(8), rs.getLong(9));
         more = rs.next();
         bar.setLast(!more);
         
         // Notify all listeners
         Iterator<IBarListener> listenerIt = barListeners.iterator();
         while(listenerIt.hasNext()) ((IBarListener)listenerIt.next()).barNotification(bar);
      }
   }

   @Override
   public Instrument getInstrument(String symbol) throws SQLException {
      
      Connection con = DriverManager.getConnection(config.getProperty("db.url"));
      String table = config.getProperty("instruments.table");
      
      String query = "SELECT tick,bpv,comment,exchange " +
                     "FROM " + table + " " +
                     "WHERE symbol=?";
      PreparedStatement stmt = con.prepareStatement(query);
      stmt.setString(1, symbol);
      ResultSet rs = stmt.executeQuery();
      
      if(rs.next()) {
         return Instrument.makeFuture(symbol, rs.getBigDecimal(1), rs.getBigDecimal(2), rs.getString(3));
      }
      
      return null;
   }

   @Override
   public InstrumentVariation getInstrumentVariation(String provider, String symbol) throws SQLException {
      
      Connection con = DriverManager.getConnection(config.getProperty("db.url"));
      String table = config.getProperty("instruments.variations.table");
      
      if(table == null) {
         return null;
      }
      
      String query = "SELECT symbol,factor,tick " +
                     "FROM " + table + " " +
                     "WHERE original_symbol=? AND provider=?";
      PreparedStatement stmt = con.prepareStatement(query);
      stmt.setString(1, symbol);
      stmt.setString(2, provider);
      ResultSet rs = stmt.executeQuery();
      
      if(rs.next()) {
         return new InstrumentVariation(rs.getString(1), rs.getBigDecimal(2).doubleValue(), rs.getBigDecimal(3).doubleValue());
      }
      
      return null;
   }
}
