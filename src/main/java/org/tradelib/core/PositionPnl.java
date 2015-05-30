package org.tradelib.core;

import java.time.LocalDateTime;

public class PositionPnl {
   public LocalDateTime ts;
   public double positionQuantity;
   public double positionValue;
   public double positionAverageCost;
   public double transactionValue;
   public double realizedPnl;
   public double unrealizedPnl;
   public double grossPnl;
   public double netPnl;
   public double fees;
   
   public PositionPnl(LocalDateTime ldt) {
      positionQuantity = 0.0;
      positionValue = 0.0;
      positionAverageCost = 0.0;
      transactionValue = 0.0;
      realizedPnl = 0.0;
      unrealizedPnl = 0.0;
      grossPnl = 0.0;
      netPnl = 0.0;
      fees = 0.0;
      
      ts = ldt;
   }
   
   public PositionPnl() {
      this(LocalDateTime.MIN);
   }
}
