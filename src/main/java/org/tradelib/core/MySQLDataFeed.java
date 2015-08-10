// Copyright 2015 Ivan Popivanov
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.tradelib.core;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
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
      config = new Properties();
      config.load(new FileInputStream(path));
      
      String fs = config.getProperty("feed.start", null);
      if(fs != null) {
         try {
            LocalDateTime ldt = LocalDate.parse(fs, DateTimeFormatter.ofPattern("yyyy-MM-dd")).atStartOfDay();
            setFeedStart(ldt);
         } catch(Exception e) {
            
         }
      }
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
      
      String query = "SELECT symbol,ts,open,high,low,close,volume " +
                     "FROM " + config.getProperty("bars.table") + " " +
                     "WHERE symbol IN (" + symbols + ") ";
      if(getFeedStart() != null) query += " AND ts >= DATE(?)";
      if(getFeedStop() != null) query += " AND ts <= DATE(?)";
      query += " ORDER BY ts ASC";

      PreparedStatement stmt = con.prepareStatement(query);
      int index = 1;
      if(getFeedStart() != null) stmt.setTimestamp(index++, Timestamp.valueOf(getFeedStart()));
      if(getFeedStop() != null) stmt.setTimestamp(index++, Timestamp.valueOf(getFeedStop()));
      
      ResultSet rs = stmt.executeQuery();
      boolean moreRecords = rs.next();
      
      ArrayDeque<Bar> queue = new ArrayDeque<Bar>();
      HashMap<String, Integer> counters = new HashMap<String, Integer>();
      
      while(true) {
         while(moreRecords) {
            // No reason to read a record if the queue head has a record with
            // a counter of more than one. In this case, we know this is not
            // the last bar for this instrument.
            boolean readRecord = true;
            
            if(queue.size() != 0) {
               Integer count = counters.get(queue.peek().getSymbol());
               if(count != null && count > 1) readRecord = false;
            }
            
            if(!readRecord) break;
            
            // Read a bar, add it to the queue and to the counting hash.
            Bar bar = new Bar(rs.getString(1), rs.getTimestamp(2).toLocalDateTime(),
                              rs.getBigDecimal(3).doubleValue(),
                              rs.getBigDecimal(4).doubleValue(),
                              rs.getBigDecimal(5).doubleValue(),
                              rs.getBigDecimal(6).doubleValue(),
                              rs.getLong(7));
            queue.add(bar);
            Integer count = counters.get(bar.getSymbol());
            if(count == null) counters.put(bar.getSymbol(), 1);
            else counters.put(bar.getSymbol(), count + 1);
            
            moreRecords = rs.next();
         }
         
         if(queue.size() == 0) {
            // Done
            break;
         }
         
         // Get the next bar
         Bar bar = queue.poll();
         // Get the counter
         Integer counter = counters.get(bar.getSymbol());
         // Update the counter
         counters.put(bar.getSymbol(), counter - 1);
         
         bar.setLast(counter == 1);
         
         // Notify all listeners
         Iterator<IBarListener> listenerIt = barListeners.iterator();
         while(listenerIt.hasNext()) ((IBarListener)listenerIt.next()).barNotification(bar);
      }
   }

   @Override
   public Instrument getInstrument(String symbol) throws SQLException {
      
      Connection con = DriverManager.getConnection(config.getProperty("db.url"));
      String table = config.getProperty("instruments.table");
      String provider = config.getProperty("instrument.provider");
      
      String query = "SELECT type,tick,bpv,comment,exchange " +
                     "FROM " + table + " " +
                     "WHERE symbol=? AND provider=?";
      PreparedStatement stmt = con.prepareStatement(query);
      stmt.setString(1, symbol);
      stmt.setString(2, provider);
      ResultSet rs = stmt.executeQuery();
      
      Instrument result = null;
      
      if(rs.next()) {
         String type = rs.getString(1);
         String comment;
         switch(type) {
         case "FUT":
            result = Instrument.makeFuture(symbol, rs.getBigDecimal(2), rs.getBigDecimal(3), rs.getString(4));
            break;
         case "FX":
            comment = rs.getString(4);
            result = Instrument.makeForex(symbol, rs.getBigDecimal(2), comment.substring(3));
            result.setName(comment);
            break;
         }
      }
      
      stmt.close();
      con.close();
      
      return result;
   }

   @Override
   public InstrumentVariation getInstrumentVariation(String provider, String symbol) throws SQLException {
      
      Connection con = DriverManager.getConnection(config.getProperty("db.url"));
      String table = config.getProperty("instruments.variations.table");
      String originalProvider = config.getProperty("instrument.provider");
      
      InstrumentVariation result = null;
      
      if(table != null) {
         String query = "SELECT symbol,factor,tick " +
                        "FROM " + table + " " +
                        "WHERE original_symbol=? AND original_provider=? AND provider=?";
         PreparedStatement stmt = con.prepareStatement(query);
         stmt.setString(1, symbol);
         stmt.setString(2, originalProvider);
         stmt.setString(3, provider);
         ResultSet rs = stmt.executeQuery();
         
         if(rs.next()) {
            result = new InstrumentVariation(rs.getString(1), rs.getBigDecimal(2).doubleValue(), rs.getBigDecimal(3).doubleValue());
         }
      }
      
      con.close();
      
      return result;
   }
}
