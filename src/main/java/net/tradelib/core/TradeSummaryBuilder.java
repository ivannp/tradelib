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

import java.math.BigDecimal;
import java.util.logging.Logger;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

public class TradeSummaryBuilder {
   private long numTrades;

   private double grossProfits;
   private double grossLosses;

   private SummaryStatistics dailyPnlStats;
   private SummaryStatistics pnlStats;

   private long nonZero;
   private long positive;
   private long negative;

   private double maxWin;
   private double maxLoss;

   private Average averageWinTrade;
   private Average averageLossTrade;

   private Series pnl;
   private int pnlId;

   private double equity;
   private double minEquity;
   private double maxEquity;
   private double maxDD;
   private double maxDDPct;
   
   public TradeSummaryBuilder(Series pnl) {
      numTrades = 0;
      
      grossProfits = 0.0;
      grossLosses = 0.0;
      
      dailyPnlStats = new SummaryStatistics();
      pnlStats = new SummaryStatistics();
      
      nonZero = 0; positive = 0; negative = 0;
      
      maxWin = 0.0;
      maxLoss = 0.0;
      
      averageWinTrade = new Average();
      averageLossTrade = new Average();
      
      this.pnl = pnl;
      
      pnlId = 0;
      
      equity = 0.0;
      minEquity = Double.MAX_VALUE;
      maxEquity = Double.MIN_VALUE;
      maxDD = Double.MAX_VALUE;
      maxDDPct = Double.MAX_VALUE;
   }
   
   public void add(Trade ts) {
      ++numTrades;
      if(ts.pnl < 0.0) {
         ++nonZero;
         ++negative;
         averageLossTrade.add(ts.pnl);
         grossLosses += ts.pnl;
      } else if(ts.pnl > 0.0) {
         ++nonZero;
         ++positive;
         averageWinTrade.add(ts.pnl);
         grossProfits += ts.pnl;
      }

      pnlStats.addValue(ts.pnl);

      maxWin = Math.max(maxWin, ts.pnl);
      maxLoss = Math.min(maxLoss, ts.pnl);

      // Set the PnL to zero until the current trade begins
      while(pnlId < pnl.size() && pnl.getTimestamp(pnlId).isBefore(ts.start)) {
         pnl.set(pnlId, 0.0);
         ++pnlId;
      }

      // Keep the PnL as it is inside the current trade
      while(pnlId < pnl.size() && !pnl.getTimestamp(pnlId).isAfter(ts.end)) {
         equity += pnl.get(pnlId); 
         maxEquity = Math.max(maxEquity, equity);
         minEquity = Math.min(minEquity, equity);
         maxDD = Math.min(maxDD, equity - maxEquity);
         double prev = maxDDPct;
         maxDDPct = Math.min(maxDDPct, equity/maxEquity-1);
         if(Double.isNaN(maxDDPct) || !Double.isFinite(maxDDPct)) {
            maxDDPct = Double.MAX_VALUE;
         }

         if(pnl.get(pnlId) != 0.0) {
            dailyPnlStats.addValue(pnl.get(pnlId));
         }

         ++pnlId;
      }
   }
   
   public TradeSummary summarize() {
      TradeSummary summary = new TradeSummary();
      summary.numTrades = numTrades;

      if(numTrades > 0) {
         summary.grossLosses = grossLosses;
         summary.grossProfits = grossProfits;
         
         if(grossLosses != 0.0) {
            summary.profitFactor = Math.abs(grossProfits/grossLosses);
         } else {
            summary.profitFactor = Math.abs(grossProfits);
         }

         summary.averageTradePnl = pnlStats.getMax();
         summary.tradePnlStdDev = pnlStats.getStandardDeviation();
         if(numTrades > 0) {
            summary.pctNegative = (double)negative/numTrades*100.0; 
            summary.pctPositive = (double)positive/numTrades*100.0; 
         } else {
            summary.pctNegative = 0.0;
            summary.pctPositive = 0.0;
         }

         summary.maxLoss = maxLoss;
         summary.maxWin = maxWin;
         summary.averageLoss = averageLossTrade.get();
         summary.averageWin = averageWinTrade.get();
         
         if(summary.averageLoss != 0.0) {
            summary.averageWinLoss = summary.averageWin/(summary.averageLoss*(-1.0)); 
         } else {
            summary.averageWinLoss = summary.averageWin;
         }

         summary.equityMin = minEquity;
         summary.equityMax = maxEquity;
         summary.maxDD = maxDD;
         summary.maxDDPct = maxDDPct*100;
         if(Double.isNaN(summary.maxDDPct) || !Double.isFinite(summary.maxDDPct)) {
            Logger.getLogger("").warning(String.format("Fixing a bad maximum drawdown [%f]", summary.maxDDPct));
            summary.maxDDPct = Double.MAX_VALUE;
         }

         summary.averageDailyPnl = dailyPnlStats.getMean();
         summary.dailyPnlStdDev = dailyPnlStats.getStandardDeviation();
         if(summary.dailyPnlStdDev == 0) {
            summary.dailyPnlStdDev = 1e-10;
         }
         summary.sharpeRatio = Functions.sharpeRatio(summary.averageDailyPnl, summary.dailyPnlStdDev, 252); 
      }
      
      return summary;
   }
}
