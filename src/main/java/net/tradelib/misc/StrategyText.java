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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class StrategyText {

   public static String build(String dbUrl, String strategy, LocalDate date, String sep) throws Exception {
      return build(dbUrl, strategy, date, sep, null, ',');
   }
   
   public static String build(String dbUrl, String strategy, LocalDate date, String sep, String csvPath, char csvSep) throws Exception {
      String text = "";
      
      Connection connection = DriverManager.getConnection(dbUrl);
      
      List<InstrumentText> lit = StrategyText.buildList(connection, strategy, date, csvPath, csvSep);
      for(InstrumentText it : lit) {
         if(!it.isSection()) {
            text += String.format(
                        "\n%20s" + sep + "%4s" + sep + "%10s" + sep + "%s",
                        it.getName(),
                        it.getSymbol(),
                        it.getExpiration(),
                        it.getStatus());
         }
      }
      
      connection.close();
      
      return text;
   }
   
   public static List<InstrumentText> buildList(Connection con, String strategy, LocalDate date, String csvPath, char csvSep) throws Exception {
   // public static List<InstrumentText> buildList(Connection con, String strategy, LocalDate date) throws Exception {
      ArrayList<InstrumentText> result = new ArrayList<InstrumentText>();
      
      CSVPrinter printer = null;
      if(csvPath != null)
      {
         // Add withHeader for headers
         printer = CSVFormat.DEFAULT.withDelimiter(csvSep).print(new BufferedWriter(new FileWriter(csvPath)));
      }
      
      int numCsvColumns = 12;
      
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
      
      String prevCategory = "";
      PreparedStatement pstmt = con.prepareStatement(query);
      pstmt.setString(1, strategy);
      pstmt.setTimestamp(2, Timestamp.valueOf(date.atStartOfDay()));
      ResultSet rs = pstmt.executeQuery();
      while(rs.next()) {
         String category = rs.getString(2);
         if(!category.equals(prevCategory)) {
            result.add(InstrumentText.makeSection(category));
            prevCategory = category;
            
            if(printer != null) {
               printer.print(category);
               for(int ii = 1; ii < numCsvColumns; ++ii) {
                  printer.print("");
               }
               printer.println();
            }
         }
         String name = rs.getString(3);
         String symbol = rs.getString(4);
         int ndays = rs.getInt(12);
         String contract;
         if(ndays > 1) {
            contract = rs.getString(10);
         } else {
            contract = "Roll to " + rs.getString(11);
         }
         
         if(printer != null) {
            printer.print(name);
            printer.print(symbol);
            printer.print(contract);
         }
         
         String signal;
         long position = (long)rs.getDouble(5);
         JsonObject jo = new Gson().fromJson(rs.getString(9), JsonObject.class);
         if(position > 0.0) {
            BigDecimal entryPrice;
            double pnl;
            try {
               entryPrice = jo.get("entry_price").getAsBigDecimal();
               pnl = jo.get("pnl").getAsDouble();
            } catch(Exception e) {
               entryPrice = BigDecimal.valueOf(Double.MIN_VALUE);
               pnl = Double.MIN_VALUE;
            }
            signal = String.format("Long [%d] since %s [at %s].", position, rs.getString(6), formatBigDecimal(entryPrice));
            if(printer != null) printer.print(signal);
            String openProfit = String.format("Open equity profit %,d.", (int)Math.floor(pnl));
            signal += " " + openProfit;
            if(printer != null) printer.print(openProfit);
         } else if(position < 0.0) {
            BigDecimal entryPrice;
            double pnl;
            try {
               entryPrice = jo.get("entry_price").getAsBigDecimal();
               pnl = jo.get("pnl").getAsDouble();
            } catch(Exception e) {
               entryPrice = BigDecimal.valueOf(-1);
               pnl = -1;
            }
            signal = String.format("Short [%d] since %s [at %s].", Math.abs(position), rs.getString(6), formatBigDecimal(entryPrice));
            if(printer != null) printer.print(signal);
            String openProfit = String.format("Open equity profit %,d.", (int)Math.floor(pnl));
            signal += " " + openProfit;
            if(printer != null) printer.print(openProfit);
         } else {
            signal = "Out.";
            if(printer != null)
            {
               printer.print(signal);
               // An empty column follows the status if there is no position - there is no profit.
               printer.print("");
            }
         }
         
         boolean hasOrder = false;
         JsonArray ja = jo.get("orders").getAsJsonArray();
         double entryRisk;
         try {
            entryRisk = jo.get("entry_risk").getAsDouble();
         } catch(Exception ee) {
            entryRisk = Double.NaN;
         }
         String profitTarget;
         Double profitTargetDbl;
         try {
            profitTarget = formatBigDecimal(jo.get("profit_target").getAsBigDecimal());
            profitTargetDbl = jo.get("profit_target").getAsDouble();
         } catch(Exception ee) {
            profitTarget = null;
            profitTargetDbl = null;
         }
         String stopLoss;
         Double stopLossDbl;
         try {
            stopLoss = formatBigDecimal(jo.get("stop_loss").getAsBigDecimal());
            stopLossDbl = jo.get("stop_loss").getAsDouble();
         } catch(Exception ee) {
            stopLoss = null;
            stopLossDbl = null;
         }
         
         Double lastClose;
         try {
            lastClose = jo.get("last_close").getAsDouble();
         } catch(Exception ee) {
            lastClose = null;
         }

         // Currently maximum one entry and maximum one exit are supported.
         String entryStr = "";
         String exitStr = "";
         String contractRiskStr = "";
         
         for(int ii = 0; ii < ja.size(); ++ii) {
            JsonObject jorder = ja.get(ii).getAsJsonObject();
            
            switch(jorder.get("type").getAsString()) {
            case "EXIT_LONG_STOP":
               exitStr = "Exit long at stop " + formatBigDecimal(jorder.get("stop_price").getAsBigDecimal()) + ".";
               signal += " " + exitStr;
               break;
               
            case "EXIT_SHORT_STOP":
               exitStr = "Exit short at stop " + formatBigDecimal(jorder.get("stop_price").getAsBigDecimal()) + ".";
               signal += " " + exitStr;
               break;
               
            case "ENTER_LONG":
               if(!Double.isNaN(entryRisk)){
                  entryStr = String.format("Enter long at open. Contract risk is %s." , formatDouble(entryRisk, 0, 0)); 
                  signal += " " + entryStr;
               } else {
                  entryStr = "Enter long at open."; 
                  signal += " " + entryStr;
               }
               break;
               
            case "ENTER_SHORT":
               if(!Double.isNaN(entryRisk)){
                  entryStr = String.format("Enter short at open. Contract risk is %s." , formatDouble(entryRisk, 0, 0));
                  signal += " " + entryStr;
               } else {
                  entryStr = "Enter short at open."; 
                  signal += " " + entryStr;
               }
               break;
               
            case "ENTER_LONG_STOP":
               position = jorder.get("quantity").getAsLong();
               entryStr = String.format(
                              "Enter long [%d] at stop %s [%s%%].",
                              position,
                              formatBigDecimal(jorder.get("stop_price").getAsBigDecimal()),
                              formatPercentage(jorder.get("stop_price").getAsDouble()/lastClose*100-100));
               signal += " " + entryStr;
               if(!Double.isNaN(entryRisk)){
                  contractRiskStr = String.format(" Contract risk is %s." , formatDouble(entryRisk, 0, 0));
                  signal += " " + contractRiskStr;
               }
               break;
               
            case "ENTER_LONG_STOP_LIMIT":
               position = jorder.get("quantity").getAsLong();
               entryStr = String.format(
                              "Enter long [%d] at limit %s, stop at %s [%s%%].",
                              position,
                              formatBigDecimal(jorder.get("limit_price").getAsBigDecimal()),
                              formatBigDecimal(jorder.get("stop_price").getAsBigDecimal()),
                              formatPercentage(jorder.get("stop_price").getAsDouble()/lastClose*100-100));
               signal += " " + entryStr;
               if(!Double.isNaN(entryRisk)){
                  contractRiskStr = String.format(" Contract risk is %s." , formatDouble(entryRisk, 0, 0));
                  signal += contractRiskStr;
               }
               break;
               
            case "ENTER_SHORT_STOP":
               // signal += " Enter short at stop " + formatBigDecimal(jorder.get("stop_price").getAsBigDecimal()) + ".";
               position = jorder.get("quantity").getAsLong();
               entryStr = String.format(
                              "Enter short [%d] at stop %s [%s%%].",
                              Math.abs(position),
                              formatBigDecimal(jorder.get("stop_price").getAsBigDecimal()),
                              formatPercentage(jorder.get("stop_price").getAsDouble()/lastClose*100-100));
               signal += " " + entryStr;
               if(!Double.isNaN(entryRisk)){
                  contractRiskStr = String.format(" Contract risk is %s." , formatDouble(entryRisk, 0, 0));
                  signal += " " + contractRiskStr;
               }
               break;

            case "ENTER_SHORT_STOP_LIMIT":
               position = jorder.get("quantity").getAsLong();
               entryStr = String.format(
                              "Enter short [%d] at limit %s, stop at %s [%s%%].",
                              Math.abs(position),
                              formatBigDecimal(jorder.get("limit_price").getAsBigDecimal()),
                              formatBigDecimal(jorder.get("stop_price").getAsBigDecimal()),
                              formatPercentage(jorder.get("stop_price").getAsDouble()/lastClose*100-100));
               signal += " " + entryStr;
               if(!Double.isNaN(entryRisk)){
                  contractRiskStr = String.format(" Contract risk is %s." , formatDouble(entryRisk, 0, 0));
                  signal += " " + contractRiskStr;
               }
               break;

            case "EXIT_LONG":
               exitStr = "Exit long at open.";
               signal += " " + exitStr;
               break;

            case "EXIT_SHORT":
               exitStr = "Exit short at open.";
               signal += " " + exitStr;
               break;
               
            case "EXIT_SHORT_STOP_LIMIT":
               exitStr = "Exit short at limit " + formatBigDecimal(jorder.get("limit_price").getAsBigDecimal()) +
                     ", stop at " + formatBigDecimal(jorder.get("stop_price").getAsBigDecimal()) +
                     " [" + formatPercentage(jorder.get("stop_price").getAsDouble()/lastClose*100-100) + "%]" +
                     ".";
               signal += " " + exitStr;
               break;
               
            case "EXIT_LONG_STOP_LIMIT":
               exitStr = "Exit long at limit " + formatBigDecimal(jorder.get("limit_price").getAsBigDecimal()) +
                        ", stop at " + formatBigDecimal(jorder.get("stop_price").getAsBigDecimal()) + 
                        " [" + formatPercentage(jorder.get("stop_price").getAsDouble()/lastClose*100-100) + "%]" +
                        ".";
               signal += " " + exitStr;
               break;
            }
            hasOrder = true;
         }
         
         String lastCloseStr = "Last close at " + formatBigDecimal(jo.get("last_close").getAsBigDecimal()) + ".";
         String stopLossStr = "";
         String profitTargetStr = "";
         
         if(hasOrder) {
            signal += " " + lastCloseStr;
         }
         
         if(stopLoss != null) {
            stopLossStr = "Stop loss at " + stopLoss;
            if(lastClose != null && stopLossDbl != null) {
               stopLossStr += " [" + formatPercentage(stopLossDbl/lastClose*100-100) + "%]";
            }
            stopLossStr += ".";
            signal += " " + stopLossStr;
         }
         
         if(profitTarget != null) {
            profitTargetStr = "Profit target at about " + profitTarget;
            if(profitTargetDbl != null && lastClose != null) {
               profitTargetStr += " [" + formatPercentage(profitTargetDbl/lastClose*100-100) + "%]";
            }
            profitTargetStr += ".";
            signal += " " + profitTargetStr;
         }
         
         if(printer != null) {
            printer.print(exitStr);
            printer.print(entryStr);
            printer.print(contractRiskStr);
            printer.print(lastCloseStr);
            printer.print(stopLossStr);
            printer.print(profitTargetStr);
            printer.println();
         }
         
         result.add(InstrumentText.make(name, symbol, contract, signal));
      }
      
      rs.close();
      pstmt.close();
      
      if(printer != null) printer.flush();
      
      return result;
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
   
   static private String formatPercentage(double dd) {
      return formatDouble(dd, 2, 2);
   }
}
