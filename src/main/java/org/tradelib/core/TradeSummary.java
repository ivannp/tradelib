// Copyright 2015 by Ivan Popivanov
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
   public double maxDD;
   public double maxDDPct;
   
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
      maxDD = Double.NaN;
      maxDDPct = Double.NaN;
   }
}
