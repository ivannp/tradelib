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

package net.tradelib.misc;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDate;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class StrategyText {

   public static String build(String dbUrl, String strategy, LocalDate date, String sep) throws Exception {
      String text = "";
      
      Connection connection = DriverManager.getConnection(dbUrl);
      connection.setAutoCommit(false);
      
      String query;
      
      query = " select c.id as cid, c.name as cname, i.comment as name, coalesce(ivar.symbol, spos.symbol) as symbol, " +
            "     spos.position as position, date_format(spos.ts, '%Y-%m-%d') as date, spos.last_close as close, " +
            "     spos.last_ts as close_date, " +
            "     spos.details AS details, date_format(i.current_contract,'%b\\'%y') as current_contract, " +
            "     date_format(i.next_contract,'%b\\'%y') as next_contract, i.trading_days as days " +
            " from strategy_positions spos " +
            " inner join strategies s on s.id = spos.strategy_id " +
            " inner join instrument i on i.symbol = spos.symbol and i.provider = 'csi' " +
            " left join instrument_variation ivar on spos.symbol = ivar.original_symbol and ivar.original_provider = 'csi' " +
            " left join instrument_visiable iv on iv.instrument_id = i.id " +
            " left join categories c on iv.categories_id = c.id " +
            " WHERE s.name = ? AND DATE(spos.last_ts) = DATE(?) " +
            " ORDER BY cid, iv.ord";
      
      PreparedStatement pstmt = connection.prepareStatement(query);
      pstmt.setString(1, strategy);
      pstmt.setTimestamp(2, Timestamp.valueOf(date.atStartOfDay()));
      ResultSet rs = pstmt.executeQuery();
      while(rs.next()) {
         String name = rs.getString(3);
         String symbol = rs.getString(4);
         int ndays = rs.getInt(12);
         String contract;
         if(ndays > 0) {
            contract = rs.getString(10);
         } else {
            contract = "Roll to " + rs.getString(11);
         }
         
         String signal;
         double position = rs.getDouble(5);
         JsonObject jo = new Gson().fromJson(rs.getString(9), JsonObject.class);
         if(position > 0.0) {
            signal = "Long since " + rs.getString(6);
            signal += String.format(" [at %s]. Open equity profit %,d.",
                        formatBigDecimal(jo.get("entry_price").getAsBigDecimal()),
                        (int)Math.floor(jo.get("pnl").getAsDouble()));
         } else if(position < 0.0) {
            signal = "Short since " + rs.getString(6);
            signal += String.format(" [at %s]. Open equity profit %,d.",
                        formatBigDecimal(jo.get("entry_price").getAsBigDecimal()), 
                        (int)Math.floor(jo.get("pnl").getAsDouble()));
         } else {
            signal = "Out.";
         }
         
         boolean hasOrder = false;
         JsonArray ja = jo.get("orders").getAsJsonArray();
         for(int ii = 0; ii < ja.size(); ++ii) {
            JsonObject jorder = ja.get(ii).getAsJsonObject();
            switch(jorder.get("type").getAsString()) {
            case "EXIT_LONG_STOP":
               signal += " Exit long at stop " + formatBigDecimal(jorder.get("stop_price").getAsBigDecimal()) + ".";
               break;
            case "EXIT_SHORT_STOP":
               signal += " Exit short at stop " + formatBigDecimal(jorder.get("stop_price").getAsBigDecimal()) + ".";
               break;
            case "ENTER_LONG":
               signal += " Enter long at open.";
               break;
            case "ENTER_SHORT":
               signal += " Enter short at open.";
               break;
            case "ENTER_LONG_STOP":
               signal += " Enter long at stop " + formatBigDecimal(jorder.get("stop_price").getAsBigDecimal()) + ".";
               break;
            case "ENTER_LONG_STOP_LIMIT":
               signal += " Enter long at limit " + formatBigDecimal(jorder.get("limit_price").getAsBigDecimal()) +
                     ", stop at " + formatBigDecimal(jorder.get("stop_price").getAsBigDecimal()) + ".";
               break;
            case "ENTER_SHORT_STOP":
               signal += " Enter short at stop " + formatBigDecimal(jorder.get("stop_price").getAsBigDecimal()) + ".";
               break;
            case "ENTER_SHORT_STOP_LIMIT":
               signal += " Enter short at limit " + formatBigDecimal(jorder.get("limit_price").getAsBigDecimal()) +
                     ", stop at " + formatBigDecimal(jorder.get("stop_price").getAsBigDecimal()) + ".";
               break;
            case "EXIT_LONG":
               signal += " Exit long at open.";
               break;
            case "EXIT_SHORT":
               signal += " Exit short at open.";
               break;
            case "EXIT_SHORT_STOP_LIMIT":
               signal += " Exit short at limit " + formatBigDecimal(jorder.get("limit_price").getAsBigDecimal()) +
                     ", stop at " + formatBigDecimal(jorder.get("stop_price").getAsBigDecimal()) + ".";
               break;
            case "EXIT_LONG_STOP_LIMIT":
               signal += " Exit long at limit " + formatBigDecimal(jorder.get("limit_price").getAsBigDecimal()) +
                        ", stop at " + formatBigDecimal(jorder.get("stop_price").getAsBigDecimal()) + ".";
               break;
            }
            hasOrder = true;
         }
         
         if(hasOrder) {
            signal += " Last close at " + formatBigDecimal(jo.get("last_close").getAsBigDecimal()) + "."; 
         }
         
         text += "\n" + name + sep + symbol + sep + contract + sep + signal;
      }
      
      rs.close();
      pstmt.close();
      
      return text;
   }
   
   static private String formatBigDecimal(BigDecimal bd, int minPrecision, int maxPrecision) {
      bd = bd.setScale(maxPrecision, RoundingMode.HALF_UP).stripTrailingZeros();
      int scale = bd.scale() <= minPrecision ? minPrecision : bd.scale();
      return String.format("%,." + Integer.toString(scale) + "f", bd);
   }
   
   static private String formatBigDecimal(BigDecimal bd) {
      return formatBigDecimal(bd, 2, 7);
   }
   
   static private String formatDouble(double dd, int minPrecision, int maxPrecision) {
      BigDecimal bd = new BigDecimal(dd, MathContext.DECIMAL128);
      bd = bd.setScale(maxPrecision, RoundingMode.HALF_UP);
      return formatBigDecimal(bd);
   }
   
   static private String formatDouble(double dd) {
      return formatDouble(dd, 2, 7);
   }
}
