package org.tradelib.core;

public class TradeSummaryBuilder {
   private long numTrades;

   private double grossProfits;
   private double grossLosses;

   private AverageAndVariance dailyPnlStats;
   private AverageAndVariance pnlStats;

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
   private double maxDrawdown;
   
   public TradeSummaryBuilder(Series pnl) {
      numTrades = 0;
      
      grossProfits = 0.0;
      grossLosses = 0.0;
      
      dailyPnlStats = new AverageAndVariance();
      pnlStats = new AverageAndVariance();
      
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
      maxDrawdown = Double.MAX_VALUE;
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

      pnlStats.add(ts.pnl);

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
         maxDrawdown = Math.min(maxDrawdown, equity - maxEquity);

         if(pnl.get(pnlId) != 0.0) {
            dailyPnlStats.add(pnl.get(pnlId));
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

         summary.averageTradePnl = pnlStats.getAverage();
         summary.tradePnlStdDev = pnlStats.getStdDev();
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
         summary.maxDrawdown = maxDrawdown;

         summary.averageDailyPnl = dailyPnlStats.getAverage();
         summary.dailyPnlStdDev = dailyPnlStats.getStdDev();
         summary.sharpeRatio = Functions.sharpeRatio(summary.averageDailyPnl, summary.dailyPnlStdDev, 252); 
      }
      
      return summary;
   }
}
