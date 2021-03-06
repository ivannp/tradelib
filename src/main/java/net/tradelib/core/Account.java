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
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

public class Account {
   public static final String DEFAULT_NAME = "DefaultAccount";
   
   private String name;
   
   private class Summary {
      LocalDateTime ts;
      double addition = 0.0;
      double withdrawal = 0.0;
      double interest = 0.0;
      double realizedPnl = 0.0;
      double unrealizedPnl = 0.0;
      double grossPnl = 0.0;
      double txnFees = 0.0;
      double netPnl = 0.0;
      double advisoryFees = 0.0;
      double netPerformance = 0.0;
      double endEq = Double.NaN;
      
      public Summary(LocalDateTime ts, double endEq) {
         this.ts = ts;
         this.endEq = endEq;
      }
      
      public Summary(LocalDateTime ts) {
         this.ts = ts;
      }
   }
   
   private LocalDateTime endEquityTimestamp;
   
   private TreeMap<LocalDateTime, Summary> summaries;
   
   private class PortfolioData {
      Portfolio portfolio;
      TimeSeries<Double> summary;
      
      public PortfolioData(String name, LocalDateTime startDate) {
         portfolio = new Portfolio(name);
         
         summary = new TimeSeries<Double>(
                           "LongValue", "ShortValue", "NetValue",
                           "GrossValue", "RealizedPnL", "UnrealizedPnL",
                           "GrossPnL", "TxnFees", "NetPnL");
         summary.add(startDate, 0.0);
      }
   }
   
   private HashMap<String, PortfolioData> portfolios;
   
   public Account(String name, LocalDateTime startDate, double initialEquity, String ...portfolioNames) {
      this.name = name;
      
      portfolios = new HashMap<String, PortfolioData>();
      if(portfolioNames.length > 0) {
         for(int ii = 0; ii < portfolioNames.length; ++ii) {
            portfolios.put(portfolioNames[ii], new PortfolioData(portfolioNames[ii], startDate));
         }
      } else {
         portfolios.put(Portfolio.DEFAULT_NAME, new PortfolioData(Portfolio.DEFAULT_NAME, startDate));
      }
      
      summaries = new TreeMap<LocalDateTime, Summary>();
      summaries.put(startDate, new Summary(startDate, initialEquity));
      
      endEquityTimestamp = startDate;
   }
   
   public Account(LocalDateTime startDate, double initialEquity, String ...portfolioNames) {
      this(DEFAULT_NAME, startDate, initialEquity, portfolioNames);
   }
   
   public Account() {
      this(DEFAULT_NAME, LocalDateTime.MIN, 0.0, Portfolio.DEFAULT_NAME);
   }
   
   public void add(LocalDateTime ts, double amount) {
      Summary ss = summaries.get(ts);
      if(ss == null) ss = new Summary(ts);
      ss.addition += amount;
      summaries.put(ts, ss);
   }
   
   public void withdraw(LocalDateTime ts, double amount) {
      Summary ss = summaries.get(ts);
      if(ss == null) ss = new Summary(ts);
      ss.withdrawal += amount;
      summaries.put(ts, ss);
   }
   
   public void addInterest(LocalDateTime ts, double amount) {
      Summary ss = summaries.get(ts);
      if(ss == null) ss = new Summary(ts);
      ss.interest += amount;
      ss.netPerformance += amount;
      summaries.put(ts, ss);
   }
   
   public void addAdvisoryFee(LocalDateTime ts, double amount) {
      Summary ss = summaries.get(ts);
      if(ss == null) ss = new Summary(ts);
      ss.advisoryFees += amount;
      ss.netPerformance += amount;
      summaries.put(ts, ss);
   }
   
   public void addTransaction(String portfolio, Instrument instrument, LocalDateTime timeStamp, long quantity, double price, double fees) {
      portfolios.get(portfolio).portfolio.addTransaction(instrument, timeStamp, quantity, price, fees);
   }
   
   public void addTransaction(Instrument instrument, LocalDateTime timeStamp, long quantity, double price, double fees) {
      PortfolioData pd = portfolios.get(Portfolio.DEFAULT_NAME);
      if(pd == null) {
         pd = new PortfolioData(Portfolio.DEFAULT_NAME, timeStamp.minusDays(1));
      }
      pd.portfolio.addTransaction(instrument, timeStamp, quantity, price, fees);
   }
   
   public void addTransaction(Execution execution) {
      addTransaction(execution.getInstrument(), execution.getDateTime(), execution.getQuantity(), execution.getPrice(), execution.getFees());
   }
   
   public void setInitialEquity(LocalDateTime ts, double amount) {
      summaries.clear();
      summaries.put(ts, new Summary(ts, amount));
      endEquityTimestamp = ts;
   }
   
   public void updatePortfolio(String portfolio, Instrument instrument, TimeSeries<Double> prices) {
      portfolios.get(portfolio).portfolio.updatePnl(instrument, prices);
   }
   
   public void updatePortfolio(Instrument instrument, TimeSeries<Double> prices) {
      PortfolioData pd = portfolios.get(Portfolio.DEFAULT_NAME);
      if(pd == null) {
         pd = new PortfolioData(Portfolio.DEFAULT_NAME, prices.getTimestamp(0).minusDays(1));
      }
      pd.portfolio.updatePnl(instrument, prices);
   }
   
   public void mark(String portfolio, Instrument instrument, LocalDateTime ts, double price) {
      // Mark the portfolio
      List<PositionPnl> posPnls = portfolios.get(portfolio).portfolio.mark(instrument, ts, price);
      
      if(posPnls != null) {
         // Mark the account with the updates generated by the portfolio
         for(PositionPnl posPnl : posPnls) {
            Summary ss = summaries.get(posPnl.ts);
            if(ss == null) ss = new Summary(posPnl.ts);
            ss.realizedPnl += posPnl.realizedPnl;
            ss.unrealizedPnl += posPnl.unrealizedPnl;
            ss.grossPnl += posPnl.grossPnl;
            ss.netPnl += posPnl.netPnl;
            ss.netPerformance += posPnl.netPnl;
            ss.txnFees += posPnl.fees;
            summaries.put(ss.ts, ss);
         }
      }
   }
   
   public void mark(Instrument instrument, LocalDateTime ts, double price) {
      mark(Portfolio.DEFAULT_NAME, instrument, ts, price);
   }
   
   public void mark(Instrument instrument, Bar bar) {
      mark(instrument, bar.getDateTime(), bar.getClose());
   }
   
   public void updateEndEquity(LocalDateTime to) {
      double prevEndEquity = Double.NaN;
      for(Summary ss : summaries.values()) {
         if(ss.ts.isAfter(to)) {
            break;
         } else if(ss.ts.isAfter(endEquityTimestamp)) {
            if(Double.isNaN(prevEndEquity)) {
               throw new IllegalStateException("prevEndEquity not set!");
            }
            
            ss.endEq = prevEndEquity + ss.addition + ss.withdrawal + ss.netPerformance;
            endEquityTimestamp = ss.ts;
         }
         prevEndEquity = ss.endEq;
      }
   }
   
   public void updateEndEquity() {
      updateEndEquity(summaries.lastKey());
   }
   
   public Series getEquity() {
      Series result = new Series(1);
      for(Summary ss : summaries.values()) {
         result.append(ss.ts, ss.endEq);
      }
      return result;
   }
   
   public double getEndEquity() {
      // return summaries.lastEntry().getValue().endEq;
      return summaries.get(endEquityTimestamp).endEq;
   }
   
   public LocalDateTime getEndEquityTimestamp() {
      return endEquityTimestamp;
   }
   
   public double getEndEquity(LocalDateTime upTo) {
      assert !upTo.isAfter(endEquityTimestamp) : "NaN end equity requested";
      return summaries.floorEntry(upTo).getValue().endEq;
   }
   
   public PositionPnl getPositionPnl(String portfolio, Instrument instrument) {
      return portfolios.get(portfolio).portfolio.getPositionPnl(instrument);
   }
   
   public PositionPnl getPositionPnl(Instrument instrument) {
      return portfolios.get(Portfolio.DEFAULT_NAME).portfolio.getPositionPnl(instrument);
   }
   
   public Iterable<String> getPortfolioSymbols(String portfolio) {
      return portfolios.get(portfolio).portfolio.symbols();
   }
   
   public Iterable<String> getPortfolioSymbols() {
      return getPortfolioSymbols(Portfolio.DEFAULT_NAME);
   }
   
   public Series getPnlSeries(String portfolio, Instrument instrument) {
      return portfolios.get(portfolio).portfolio.getPnlSeries(instrument);
   }
   
   public Series getPnlSeries(Instrument instrument) {
      return getPnlSeries(Portfolio.DEFAULT_NAME, instrument);
   }
   
   public Pnl getPnl(String portfolio, Instrument instrument) {
      return portfolios.get(portfolio).portfolio.getPnl(instrument);
   }
   
   public Pnl getPnl(Instrument instrument) {
      return getPnl(Portfolio.DEFAULT_NAME, instrument);
   }
   
   public TradingResults getPortfolioTradingResults(String portfolio, Instrument instrument) {
      return portfolios.get(portfolio).portfolio.getTradingResults(instrument);
   }
   
   public TradingResults getPortfolioTradingResults(Instrument instrument) {
      return getPortfolioTradingResults(Portfolio.DEFAULT_NAME, instrument);
   }
   
   public Series getSummary() {
      Series result = new Series(11);
      for(Summary ss : summaries.values()) {
         result.append(ss.ts, ss.addition, ss.withdrawal, ss.interest,
               ss.realizedPnl, ss.unrealizedPnl, ss.grossPnl, ss.txnFees,
               ss.netPnl, ss.advisoryFees, ss.netPerformance, ss.endEq);
      }
      result.setNames("addition", "withdrawal", "interest",
                      "realized.pnl", "unrealized.pnl", "gross.pnl",
                      "fees", "net.pnl", "advisory.fees",
                      "net.performance", "end.equity");
      return result;
   }
   
   public Series getPortfolioSummary(String portfolio) {
      return portfolios.get(portfolio).portfolio.getSummary();
   }
   
   public Series getPortfolioSummary() {
      return getPortfolioSummary(Portfolio.DEFAULT_NAME);
   }
   
   public Series getPositionPnls(String portfolio, Instrument instrument) {
      return portfolios.get(portfolio).portfolio.getPositionPnls(instrument);
   }
   
   public Series getPositionPnls(Instrument instrument) {
      return getPositionPnls(Portfolio.DEFAULT_NAME, instrument);
   }
}
