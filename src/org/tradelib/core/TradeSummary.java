package org.tradelib.core;

public class TradeSummary {
   public int id;
   public int stratedyId;
   public String symbol;
   public String type;
   
   public long numTrades;
   public double grossProfits;
   public double grossLosses;
   public double profitFactor;

   public double averageDailyPnl;
   public double dailyPnlStdDev;
   public double sharpeRatio;

   public double averageTradePnl;
   public double tradePnlStdDev;

   public double pctPositive;
   public double pctNegative;

   public double maxWin;
   public double maxLoss;
   public double averageWin;
   public double averageLoss;
   public double averageWinLoss;

   public double equityMin;
   public double equityMax;
   public double maxDrawdown;
   
   public TradeSummary() {
      numTrades = Long.MIN_VALUE;
      
      grossProfits = Double.NaN;
      grossLosses = Double.NaN;
      profitFactor = Double.NaN;

      averageDailyPnl = Double.NaN;
      dailyPnlStdDev = Double.NaN;
      sharpeRatio = Double.NaN;

      averageTradePnl = Double.NaN;
      tradePnlStdDev = Double.NaN;

      pctPositive = Double.NaN;
      pctNegative = Double.NaN;

      maxWin = Double.NaN;
      maxLoss = Double.NaN;
      averageWin = Double.NaN;
      averageLoss = Double.NaN;
      averageWinLoss = Double.NaN;

      equityMin = Double.NaN;
      equityMax = Double.NaN;
      maxDrawdown = Double.NaN;
   }
}
