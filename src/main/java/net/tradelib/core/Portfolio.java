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

package net.tradelib.core;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.logging.Logger;

import com.google.common.collect.TreeBasedTable;

public class Portfolio {
   public static final String DEFAULT_NAME = "DefaultPortfolio";
   
   private static final Logger logger = Logger.getLogger(Portfolio.class.getName());

   private class Transaction {
      public LocalDateTime ts;
      public long quantity;
      public double price;
      public double value;
      public double averageCost;
      public long positionQuantity;
      public double positionAverageCost;
      public double grossPnl;
      public double netPnl;
      public double fees;

      public Transaction(LocalDateTime ldt, long q, double p, double f) {
         ts = ldt;
         quantity = q;
         price = p;
         fees = f;
         value = 0.0;
         averageCost = 0.0;
         positionQuantity = 0;
         positionAverageCost = 0.0;
         grossPnl = 0.0;
         netPnl = 0.0;
      }

      public Transaction(LocalDateTime ldt) {
         ts = ldt;
         quantity = 0;
         price = 0.0;
         fees = 0.0;
         value = 0.0;
         averageCost = 0.0;
         positionQuantity = 0;
         positionAverageCost = 0.0;
         grossPnl = 0.0;
         netPnl = 0.0;
      }
   }
   
   // 'Long.Value', 'Short.Value', 'Net.Value', 'Gross.Value', 'Period.Realized.PL', 'Period.Unrealized.PL', 'Gross.Trading.PL', 'Txn.Fees', 'Net.Trading.PL'
   private class Summary {
      public LocalDateTime ts; // the timestamp for this summary
      public double longValue = 0.0;
      public double shortValue = 0.0;
      public double netValue = 0.0;
      public double grossValue = 0.0;
      public double txnFees = 0.0;
      public double realizedPnl = 0.0; // period realized PnL
      public double unrealizedPnl = 0.0; // period unrealized PnL
      public double grossPnl = 0.0; // gross trading PnL
      public double netPnl = 0.0; // net trading PnL
      
      public Summary(LocalDateTime ts) {
         this.ts = ts;
      }
   }
   
   private TreeMap<LocalDateTime, Summary> summaries;
   
   private class InstrumentData {
      public ArrayList<Transaction> transactions;
      public ArrayList<PositionPnl> positionPnls;
      
      // The last processed transaction
      public int lastTxn;
      
      // The last prices seen for this instrument. Needed to maintain
      // the running position PnL. A price must be recorded before a
      // transaction occurs.
      public double lastPrice;
      
      // The PnL for this instrument - computed accumulatively.
      public Pnl pnl;
      
      public InstrumentData() {
         transactions = new ArrayList<Transaction>();
         transactions.add(new Transaction(LocalDateTime.MIN));
         
         lastTxn = 0;
         lastPrice = Double.NaN;
         
         pnl = new Pnl();
         
         positionPnls = new ArrayList<PositionPnl>();
         positionPnls.add(new PositionPnl());
      }
   }
   
   String name_;
   HashMap<String, InstrumentData> instrumentMap;
   
   public Portfolio(String name) {
      this.name_ = name;
      this.instrumentMap = new HashMap<String, InstrumentData>();
      this.summaries = new TreeMap<LocalDateTime, Portfolio.Summary>();
   }
   
   public Portfolio() {
      this.name_ = DEFAULT_NAME;
      this.instrumentMap = new HashMap<String, InstrumentData>();
      this.summaries = new TreeMap<LocalDateTime, Portfolio.Summary>();
   }
   
   public void addInstrument(Instrument i) {
      InstrumentData icb = instrumentMap.get(i.getSymbol());
      if(icb == null) {
         instrumentMap.put(i.getSymbol(), new InstrumentData());
      }
   }
   
   public void addTransaction(Instrument i, LocalDateTime ldt, long q, double p, double f) {
      InstrumentData id = instrumentMap.get(i.getSymbol());
      if(id == null) {
         id = new InstrumentData();
         instrumentMap.put(i.getSymbol(), id);
      }
      
      ArrayList<Transaction> instrumentTransactions = id.transactions;
      
      assert ldt.isAfter(instrumentTransactions.get(instrumentTransactions.size()-1).ts) :
             "Transactions must be added in chronological order [" +
             ldt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:mm nnn")) + " " +
             instrumentTransactions.get(instrumentTransactions.size()-1).ts.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:mm nnn")) + "]";
      
      Transaction transaction = new Transaction(ldt, q, p, f);
      long ppq = instrumentTransactions.get(instrumentTransactions.size()-1).positionQuantity;
      if(ppq != 0 && ppq != -transaction.quantity && Long.signum(ppq + transaction.quantity) != Long.signum(ppq)) {
         // Split the transaction into two, first add the zero-ing transaction
         double perUnitFee = transaction.fees / Math.abs(transaction.quantity);
         addTransaction(i, transaction.ts, -ppq, transaction.price, perUnitFee * Math.abs(ppq));

         // Adjust the inputs to reflect what's left to transact, increase the date
         // time by a microsecond to keep the uniqueness in the transaction set.
         transaction.ts = transaction.ts.plusNanos(1000);
         transaction.quantity += ppq;
         ppq = 0;
         transaction.fees = perUnitFee * Math.abs(transaction.quantity); 
      }
      
      // Transaction value, gross of fees
      transaction.value = transaction.quantity * transaction.price * i.getBpv();

      // Transaction average cost
      transaction.averageCost = transaction.value / (transaction.quantity * i.getBpv());

      // Calculate the new quantity for this position
      transaction.positionQuantity = ppq + transaction.quantity;

      // Previous position average cost
      double ppac = instrumentTransactions.get(instrumentTransactions.size()-1).positionAverageCost;

      // Calculate position average cost
      if (transaction.positionQuantity == 0) {
         transaction.positionAverageCost = 0.0;
      } else if(Math.abs(ppq) > Math.abs(transaction.positionQuantity)) {
         transaction.positionAverageCost = ppac;
      } else {
         transaction.positionAverageCost = (ppq * ppac * i.getBpv() + transaction.value) /
                        (transaction.positionQuantity * i.getBpv());
      }

      // Calculate PnL
      if(Math.abs(ppq) < Math.abs(transaction.positionQuantity) || ppq == 0) {
         transaction.grossPnl = 0.0;
      } else {
         transaction.grossPnl = transaction.quantity * i.getBpv() * (ppac - transaction.averageCost); 
      }

      transaction.netPnl = transaction.grossPnl + transaction.fees;
      
//      logger_.info("Portfolio: adding transaction at " + transaction.ts.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) +
//            ", previous transaction at " + transactions.get(transactions.size()-1).ts.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
      
      assert transaction.ts.isAfter(instrumentTransactions.get(instrumentTransactions.size()-1).ts) :
         "Transactions must be added in chronological order [" +
         transaction.ts.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:mm nnn")) + " " +
         instrumentTransactions.get(instrumentTransactions.size()-1).ts.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:mm nnn")) + "]";
      
      instrumentTransactions.add(transaction);
   }
   
   public void addTransaction(Execution e) {
      addTransaction(e.getInstrument(), e.getDateTime(), e.getQuantity(), e.getPrice(), e.getFees());
   }
   
   public void markSummary(PositionPnl posPnl) {
      Summary ss = summaries.get(posPnl.ts);
      if(ss == null) {
         ss = new Summary(posPnl.ts);
      }
      
      ss.grossValue += Math.abs(posPnl.positionValue);
      ss.netValue += posPnl.positionValue;
      if(posPnl.positionValue > 0.0) ss.longValue += posPnl.positionValue;
      else if(posPnl.positionValue < 0.0) ss.shortValue += posPnl.positionValue;
      
      ss.realizedPnl += posPnl.realizedPnl;
      ss.unrealizedPnl += posPnl.unrealizedPnl;
      ss.grossPnl += posPnl.grossPnl;
      ss.netPnl += posPnl.netPnl;
      ss.txnFees += posPnl.fees;
      
      summaries.put(posPnl.ts, ss);
   }

   public List<PositionPnl> mark(Instrument instrument, LocalDateTime ts, double price) {
      InstrumentData idata = instrumentMap.get(instrument.getSymbol());
      
      if(idata == null) {
         idata = new InstrumentData();
         instrumentMap.put(instrument.getSymbol(), idata);
      }
      
      if(idata.transactions.size() == 1) {
         // No real transactions, record the price and return
         idata.lastPrice = price;
         return null;
      }
      
      if(Double.isNaN(idata.lastPrice)) {
         throw new IllegalStateException(
               "Portfolio::mark must be called with a valid price before recording a transaction!");
      }
      
      ArrayList<PositionPnl> result = new ArrayList<PositionPnl>();
      
      List<Transaction> txns = idata.transactions;
      
      PositionPnl lastPnl = idata.positionPnls.get(idata.positionPnls.size() - 1);
      
      // Only the last transaction can have the same timestamp as the price.
      int txnId = idata.lastTxn + 1;
      while(txnId < txns.size() && idata.transactions.get(txnId).ts.isBefore(ts)) {
         Transaction txn = txns.get(txnId);
         
         PositionPnl posPnl = new PositionPnl(txn.ts);
         
         posPnl.positionQuantity = txn.positionQuantity;
         posPnl.positionAverageCost = txn.positionAverageCost;
         posPnl.transactionValue = txn.value;
         posPnl.fees = txn.fees;
         // Use the previous price
         posPnl.positionValue = instrument.getBpv() * posPnl.positionQuantity * idata.lastPrice;
         posPnl.grossPnl = posPnl.positionValue - lastPnl.positionValue - posPnl.transactionValue;
         posPnl.realizedPnl = txn.grossPnl;
         posPnl.unrealizedPnl = posPnl.grossPnl - txn.grossPnl;
         posPnl.netPnl = posPnl.grossPnl + txn.fees;
         
         // Accumulate the unrealized PnL
         idata.pnl.add(posPnl.realizedPnl, posPnl.unrealizedPnl);

         assert posPnl.ts.isAfter(lastPnl.ts);
         idata.positionPnls.add(posPnl);
         result.add(posPnl);
         lastPnl = posPnl;
         
         markSummary(posPnl);
         
         ++txnId;
      }
      
      // Process the price
      if(txnId < txns.size() && idata.transactions.get(txnId).ts.equals(ts)) {
         // A transaction with a same timestamp as the price
         Transaction txn = txns.get(txnId);
         
         PositionPnl posPnl = new PositionPnl(txn.ts);
         
         // The current time is both in the price list and in the transaction list
         posPnl.positionQuantity = txn.positionQuantity;
         posPnl.positionAverageCost = txn.positionAverageCost;
         posPnl.transactionValue = txn.value;
         posPnl.fees = txn.fees;
         posPnl.positionValue = instrument.getBpv() * posPnl.positionQuantity * price;
         posPnl.grossPnl = posPnl.positionValue - lastPnl.positionValue - posPnl.transactionValue;
         posPnl.realizedPnl = txn.grossPnl;
         posPnl.unrealizedPnl = posPnl.grossPnl - txn.grossPnl;
         posPnl.netPnl = posPnl.grossPnl + txn.fees;
         
         // Accumulate the unrealized PnL
         idata.pnl.add(posPnl.realizedPnl, posPnl.unrealizedPnl);

         assert posPnl.ts.isAfter(lastPnl.ts);
         idata.positionPnls.add(posPnl);
         result.add(posPnl);
         lastPnl = posPnl;
         
         markSummary(posPnl);
         
         ++txnId;
      } else {
         // Create an entry based on the price itself
         PositionPnl posPnl = new PositionPnl(ts);
         
         // No position change, only mark for the price
         posPnl.positionQuantity = lastPnl.positionQuantity;
         posPnl.positionAverageCost = lastPnl.positionAverageCost;
         posPnl.transactionValue = 0.0;
         posPnl.fees = 0.0;
         posPnl.positionValue = instrument.getBpv() * posPnl.positionQuantity * price;
         posPnl.grossPnl = posPnl.positionValue - lastPnl.positionValue;
         posPnl.realizedPnl = 0.0;
         posPnl.unrealizedPnl = posPnl.grossPnl;
         posPnl.netPnl = posPnl.grossPnl;
         
         // Accumulate the unrealized PnL
         idata.pnl.add(posPnl.realizedPnl, posPnl.unrealizedPnl);

         assert posPnl.ts.isAfter(lastPnl.ts);
         idata.positionPnls.add(posPnl);
         result.add(posPnl);
         lastPnl = posPnl;
         
         markSummary(posPnl);
      }
      
      // Update the price
      idata.lastPrice = price;
      
      idata.lastTxn = txnId - 1;
      
      // Sometimes we split transactions adding a nanosecond to the price. Thus,
      // we may have a situation where there are transactions left. We will process
      // them next time (alternatively, we may want to continue here).
      
      return result;
   }
   
   public void updatePnl(Instrument instrument, TimeSeries<Double> prices) {
      InstrumentData id = instrumentMap.get(instrument.getSymbol());
      List<Transaction> transactions = id.transactions;
      
      PositionPnl lastPnl = id.positionPnls.get(id.positionPnls.size() - 1);
      
      // We only process prices after the last PnL
      int priceId = 0;
      while(priceId < prices.size() && prices.getTimestamp(priceId).isBefore(lastPnl.ts)) {
         ++priceId;
      }
      
      // Find qualifying transactions
      int txnId = 0;
      while(txnId < transactions.size() && !transactions.get(txnId).ts.isAfter(lastPnl.ts)) {
         ++txnId;
      }
      
      boolean hasPrices = priceId < prices.size();
      boolean hasTxns = txnId < transactions.size();
      
      while(hasPrices || hasTxns) {
         if(hasPrices && hasTxns && prices.getTimestamp(priceId).equals(transactions.get(txnId).ts)) {
            Transaction txn = transactions.get(txnId);
            
            PositionPnl ppnl = new PositionPnl(txn.ts);
            
            // The current time is both in the price list and in the transaction list
            ppnl.positionQuantity = txn.positionQuantity;
            ppnl.positionAverageCost = txn.positionAverageCost;
            ppnl.transactionValue = txn.value;
            ppnl.fees = txn.fees;
            ppnl.positionValue = instrument.getBpv() * ppnl.positionQuantity * prices.get(priceId);
            ppnl.grossPnl = ppnl.positionValue - lastPnl.positionValue - ppnl.transactionValue;
            ppnl.realizedPnl = txn.grossPnl;
            ppnl.unrealizedPnl = ppnl.grossPnl - txn.grossPnl;
            ppnl.netPnl = ppnl.grossPnl + txn.fees;
            
            // Accumulate the unrealized PnL
            id.pnl.add(ppnl.realizedPnl, ppnl.unrealizedPnl);

            id.positionPnls.add(ppnl);
            lastPnl = ppnl;
            
            ++priceId;
            ++txnId;
            
            hasPrices = priceId < prices.size();
            hasTxns = txnId < transactions.size();
         } else if(!hasTxns || prices.getTimestamp(priceId).isBefore(transactions.get(txnId).ts)) {
            PositionPnl ppnl = new PositionPnl(prices.getTimestamp(priceId));
            
            // No position change, only mark for the price
            ppnl.positionQuantity = lastPnl.positionQuantity;
            ppnl.positionAverageCost = lastPnl.positionAverageCost;
            ppnl.transactionValue = 0.0;
            ppnl.fees = 0.0;
            ppnl.positionValue = instrument.getBpv() * ppnl.positionQuantity * prices.get(priceId);
            ppnl.grossPnl = ppnl.positionValue - lastPnl.positionValue;
            ppnl.realizedPnl = 0.0;
            ppnl.unrealizedPnl = ppnl.grossPnl;
            ppnl.netPnl = ppnl.grossPnl;

            id.positionPnls.add(ppnl);
            lastPnl = ppnl;
            
            ++priceId;
            hasPrices = priceId < prices.size();
         } else {
            Transaction txn = transactions.get(txnId);
            
            PositionPnl ppnl = new PositionPnl(transactions.get(txnId).ts);
            
            ppnl.positionQuantity = txn.positionQuantity;
            ppnl.positionAverageCost = txn.positionAverageCost;
            ppnl.transactionValue = txn.value;
            ppnl.fees = txn.fees;
            // Use the previous price
            ppnl.positionValue = instrument.getBpv() * ppnl.positionQuantity * prices.get(priceId - 1);
            ppnl.grossPnl = ppnl.positionValue - lastPnl.positionValue - ppnl.transactionValue;
            ppnl.realizedPnl = txn.grossPnl;
            ppnl.unrealizedPnl = ppnl.grossPnl - txn.grossPnl;
            ppnl.netPnl = ppnl.grossPnl + txn.fees;
            
            // Accumulate the unrealized PnL
            id.pnl.add(ppnl.realizedPnl, ppnl.unrealizedPnl);

            id.positionPnls.add(ppnl);
            lastPnl = ppnl;
            
            ++txnId;
            hasTxns = txnId < transactions.size();
         }
      }
   }
   
   private InstrumentData getInstrumentData(Instrument instrument) {
      return instrumentMap.get(instrument.getSymbol());
   }
   
   /**
    * @brief Obtains the PnL series for an instrument
    * 
    * This call is to be used together with "mark"
    * 
    * @param instrument
    * @return The PnL series
    */
   Series getPnlSeries(Instrument instrument) {
      List<PositionPnl> positionPnls = getInstrumentData(instrument).positionPnls;
      Series ss = new Series(1);
      // Skip the first PositionPnl - it's artificial
      for(int ii = 1; ii < positionPnls.size(); ++ii) {
         PositionPnl ppnl = positionPnls.get(ii);
         ss.append(ppnl.ts, ppnl.netPnl);
      }
      return ss;
   }
   
   /**
    * @brief Computes the PnL for an instrument
    *
    * Computes the PnL for the specified instrument using the specified prices.
    *
    * @param[in] instrument - the instrument
    * @param[in] prices - the price series for the PnL computation
    */
   TimeSeries<Double> getPnlOld(Instrument instrument, TimeSeries<Double> prices)
   {
      TimeSeries<Double> pnl = new TimeSeries<Double>();
      
      ArrayList<Transaction> transactions = instrumentMap.get(instrument.getSymbol()).transactions;

      // Handle the trivial case of no transactions.
      if(transactions.size() <= 1) {
         pnl.addAll(prices.getTimestamps(), 0.0);
         return pnl;
      }

      // Find the start
      int currentTransaction = 1;
      int ii = 0;
      while(ii < prices.size() && prices.getTimestamp(ii).isBefore(transactions.get(currentTransaction).ts)) {
         ++ii;
      }

      // Set the pnl to 0 from beginning of time to the first transaction
      pnl.addAll(prices.getTimestamps(ii), 0.0);
      if(ii == prices.size()) return pnl;

      double previousPositionValue = 0.0;
      while(ii < prices.size() && currentTransaction < transactions.size()) {
         Transaction transaction = transactions.get(currentTransaction);
//         System.out.println("> " + prices.getTimestamp(ii).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + " " + prices.getTimestamp(ii).getNano() + " nanos, " + Integer.toString(ii));
//         System.out.println(transaction.ts.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + " " + 
//               transaction.ts.getNano() + " nanos, id=" + Integer.toString(currentTransaction) + ", quantity = " +
//               transaction.quantity);
         if(prices.getTimestamp(ii).equals(transaction.ts)) {
            // The current time is both in the price list and in the transaction list
            double transactionValue = transaction.value;
            double positionValue = instrument.getBpv() * transaction.positionQuantity * prices.get(ii);
            double pnlValue = positionValue - previousPositionValue - transactionValue + transaction.fees; 
            pnl.add(prices.getTimestamp(ii), pnlValue);

            ++ii;
            ++currentTransaction;

            previousPositionValue = positionValue;
         } else if(prices.getTimestamp(ii).isBefore(transaction.ts)) {
            // Only in the price list - use the previous position
            double positionValue = transactions.get(currentTransaction-1).positionQuantity * instrument.getBpv() * prices.get(ii).doubleValue();
            pnl.add(prices.getTimestamp(ii), positionValue - previousPositionValue);

            ++ii;

            previousPositionValue = positionValue;
         } else {
            if(ii > 0) {
               // Only in the transaction list - use the previous price
               double positionValue = transaction.positionQuantity * instrument.getBpv() * prices.get(ii-1);
               double pnlValue = positionValue - previousPositionValue - transaction.value;
               pnl.add(transaction.ts, pnlValue);

               previousPositionValue = positionValue;
            } else {
               // No "previous" price - no pnl
               pnl.add(transaction.ts, 0.0);
            }

            ++currentTransaction;
         }
      }

      while(ii < prices.size()) {
         double positionValue = transactions.get(currentTransaction - 1).positionQuantity * instrument.getBpv() * prices.get(ii).doubleValue();
         pnl.add(prices.getTimestamp(ii), positionValue - previousPositionValue);

         ++ii;

         previousPositionValue = positionValue;
      }
      
      return pnl;
   }
   
   public PositionPnl getPositionPnl(Instrument instrument) {
      InstrumentData id = instrumentMap.get(instrument.getSymbol());
      return id.positionPnls.get(id.positionPnls.size() - 1);
   }
   
   /**
    * @brief Computes the PnL for the current position
    *
    * Computes the PnL for the current position and the specified price. Undefined
    * behavior if a position doesn't exist.
    * 
    * A position starts with the first transaction which sets a quantity different than 0.
    *
    * Uses the gross PnL for all transactions part of this trade.
    *
    * @param[in] instrument the instrument
    * @param[in] price the price to compute the PnL
    */
   public Pnl getPositionPnl(Instrument instrument, Double price) {
      ArrayList<Transaction> instrumentTransactions = instrumentMap.get(instrument.getSymbol()).transactions;
      int ii = instrumentTransactions.size() - 1;
      Transaction transaction = instrumentTransactions.get(ii);
      assert transaction.positionQuantity != 0 : "Must not be called without a position";
      
      Pnl pnl = new Pnl();
      pnl.unrealized = instrument.getBpv() * transaction.positionQuantity * (price.doubleValue() - transaction.positionAverageCost);
      // Add all realized pnl
      while(transaction.positionQuantity != 0) {
         pnl.realized += transaction.grossPnl;
         --ii;
         transaction = instrumentTransactions.get(ii);
      }
      
      return pnl;
   }
   
   
   /**
    * @brief Closes the position for a given instrument.
    * 
    * Adds a transaction to close the position for an instrument. This is useful
    * at the end of a backtest, in order to include an open position into the
    * PnL computations.
    * 
    * @param instrument The instrument.
    * @param ldt The timestamp to use to close the position.
    * @param price The transaction price.
    * @param fees The transaction fees.
    */
   public void closePosition(Instrument instrument, LocalDateTime ldt, double price, double fees) {
      InstrumentData icb = instrumentMap.get(instrument.getSymbol());
      if(icb == null) return;
      ArrayList<Transaction> instrumentTransactions = icb.transactions;
      if(instrumentTransactions == null || instrumentTransactions.size() <= 1) return;
      Transaction transaction = instrumentTransactions.get(instrumentTransactions.size()-1);
      if(transaction.quantity != 0) addTransaction(instrument, ldt, -transaction.quantity, price, fees);
   }
   
   public TreeBasedTable<LocalDateTime, String, Double> summarize() {
      TreeBasedTable<LocalDateTime, String, Double> summary = TreeBasedTable.create();
      for(HashMap.Entry<String, InstrumentData> ee : instrumentMap.entrySet()) {
         for(PositionPnl pp : ee.getValue().positionPnls) {
            // Update Gross.Value
            Double grossValue = summary.get(pp.ts, "Gross.Value");
            if(grossValue == null) {
               summary.put(pp.ts, "Gross.Value", Math.abs(pp.positionValue));
            } else {
               summary.put(pp.ts, "Gross.Value", grossValue + Math.abs(pp.positionValue));
            }
            
            // Update Net.Value
            Double netValue = summary.get(pp.ts, "Net.Value");
            if(netValue == null) {
               summary.put(pp.ts, "Net.Value", pp.positionValue);
            } else {
               summary.put(pp.ts, "Net.Value", netValue + pp.positionValue);
            }
            
            // Update Long.Value
            if(pp.positionValue > 0.0) {
               Double longValue = summary.get(pp.ts, "Long.Value");
               if(longValue == null) {
                  summary.put(pp.ts, "Long.Value", pp.positionValue);
               } else {
                  summary.put(pp.ts, "Long.Value", longValue + pp.positionValue);
               }
            }
            
            // Update Short.Value
            if(pp.positionValue < 0.0) {
               Double shortValue = summary.get(pp.ts, "Short.Value");
               if(shortValue == null) {
                  summary.put(pp.ts, "Short.Value", pp.positionValue);
               } else {
                  summary.put(pp.ts, "Short.Value", shortValue + pp.positionValue);
               }
            }
            
            // 'Long.Value', 'Short.Value', 'Net.Value', 'Gross.Value', 'Period.Realized.PL', 'Period.Unrealized.PL', 'Gross.Trading.PL', 'Txn.Fees', 'Net.Trading.PL'
            for(String column : Arrays.asList("Period.Realized.PL", "Period.Unrealized.PL", "Gross.Trading.PL", "Txn.Fees", "Net.Trading.PL")) {
               double value = 0.0;
               switch(column) {
               case "Period.Realized.PL":
                  value = pp.realizedPnl;
                  break;
               case "Period.Unrealized.PL":
                  value = pp.unrealizedPnl;
                  break;
               case "Gross.Trading.PL":
                  value = pp.grossPnl;
                  break;
               case "Txn.Fees":
                  value = pp.fees;
                  break;
               case "Net.Trading.PL":
                  value = pp.netPnl;
                  break;
               }
               Double summaryValue = summary.get(pp.ts, column);
               if(summaryValue == null) {
                  summary.put(pp.ts, column, value);
               } else {
                  summary.put(pp.ts, column, summaryValue + value);
               }
            }
         }
      }
      
      return summary;
   }
   
   /**
    * @brief Computes statistics for each trade.
    *
    * @param[in] instrument the instrument
    */
   public List<Trade> getTrades(Instrument instrument) {
      List<Trade> list = new ArrayList<Trade>();

      InstrumentData icb = instrumentMap.get(instrument.getSymbol());
      if(icb == null) return list;
      
      ArrayList<Transaction> transactions = icb.transactions;
      if(transactions == null) return list;
      
      int ii = 0;
      // Position at the first non-zero quantity
      while(true) {
         if(ii == transactions.size()) return list; // No meaningful transactions
         if(transactions.get(ii).positionQuantity != 0) break; // Found a real transaction
         ++ii;
      }
      int jj = ii;
      for(++jj; jj < transactions.size() && transactions.get(jj).positionQuantity != 0; ++jj) {
      }
      if(jj != transactions.size()) ++jj;

      // [ii, jj) contains all transactions participating in the current trade
      while(true) {
         double positionCostBasis = 0.0;
         
         int kk = jj - 1;
         
         Trade ts = new Trade();
         
         ts.start = transactions.get(ii).ts; 
         ts.end = transactions.get(kk).ts;
         ts.initialPosition = transactions.get(ii).quantity;
         ts.maxPosition = 0;
         ts.numTransactions = 0;
         ts.maxNotionalCost = 0.0;
         ts.fees = 0.0;

         for(int ll = ii; ll < jj; ++ll) {
            Transaction transaction = transactions.get(ll);
            if(transaction.value != 0.0) ++ts.numTransactions;

            positionCostBasis = positionCostBasis + transaction.value;
            ts.fees += transaction.fees;

            if(Math.abs(transaction.positionQuantity) > Math.abs(ts.maxPosition)) {
               ts.maxPosition = transaction.positionQuantity;
               ts.maxNotionalCost = positionCostBasis;
            }
         }

         double positionValue =  transactions.get(kk).positionQuantity * instrument.getBpv() * transactions.get(kk).price;
         ts.pnl = positionValue - positionCostBasis;
         ts.pctPnl = ts.pnl/Math.abs(ts.maxNotionalCost); 
         ts.tickPnl = 0.0;
         
         list.add(ts);

         // Advance to the next trade
         if(jj == transactions.size()) break;
         ii = jj;
         for(++jj; jj < transactions.size() && transactions.get(jj).positionQuantity != 0; ++jj) {
         }
         if(jj != transactions.size()) {
            ++jj;
         } else {
            // The last trade is still open - don't add it to the list
            break;
         }
      }
      return list;
   }
   
   public TradingResults getTradingResults(Instrument instrument) {
      TradingResults tr = new TradingResults();
      tr.pnl = getPnlSeries(instrument);
      tr.stats = getTrades(instrument);
      TradeSummaryBuilder shorts = new TradeSummaryBuilder(tr.pnl);
      TradeSummaryBuilder longs = new TradeSummaryBuilder(tr.pnl);
      TradeSummaryBuilder all = new TradeSummaryBuilder(tr.pnl);
      
      for(Trade ts : tr.stats) {
         if(ts.initialPosition > 0) {
            all.add(ts);
            longs.add(ts);
         } else if(ts.initialPosition < 0) {
            all.add(ts);
            shorts.add(ts);
         }
      }
      tr.all = all.summarize();
      tr.longs = longs.summarize();
      tr.shorts = shorts.summarize();
      return tr;
   }
   
   public Iterable<String> symbols() {
      return instrumentMap.keySet();
   }
   
   public Series getSummary() {
      Series result = new Series(9);
      for(Summary ss : summaries.values()) {
         result.append(ss.ts, ss.longValue, ss.shortValue, ss.netValue,
               ss.grossValue, ss.txnFees, ss.realizedPnl, ss.unrealizedPnl,
               ss.grossPnl, ss.netPnl);
      }
      result.setNames("long.value", "short.value", "net.value", "gross.value",
                      "fees", "realized.pnl", "unrealized.pnl",
                      "gross.pnl", "net.pnl");
      return result;
   }
   
   public Series getPositionPnls(Instrument instrument) {
      Series result = new Series(9);
      List<PositionPnl> posPnls = getInstrumentData(instrument).positionPnls;
      // Skip the first PositionPnl - it's artificial
      for(int ii = 1; ii < posPnls.size(); ++ii) {
         PositionPnl posPnl = posPnls.get(ii);
         result.append(posPnl.ts, posPnl.positionQuantity, posPnl.positionValue,
                       posPnl.positionAverageCost, posPnl.transactionValue,
                       posPnl.realizedPnl, posPnl.unrealizedPnl,
                       posPnl.grossPnl, posPnl.netPnl, posPnl.fees);
      }
      
      result.setNames("quantity", "value", "avg.cost", "txn.value",
               "realized.pnl", "unrealized.pnl", "gross.pnl", "net.pnl", "fees");
      return result;
   }
   
   /**
    * Obtains the accumulative PnL for this instrument. That's the call to use
    * to report open equity PnL.
    * 
    * @param instrument
    * @return
    */
   public Pnl getPnl(Instrument instrument) {
      return getInstrumentData(instrument).pnl;
   }
}
