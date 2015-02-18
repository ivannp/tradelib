package org.tradelib.core;

import java.time.LocalDateTime;

public class Trade {
   public String symbol;
   public LocalDateTime start;
   public LocalDateTime end;
   
   public long initialPosition;
   public long maxPosition;
   public long numTransactions;
   
   public double maxNotionalCost;
   
   public double pnl;
   public double pctPnl;
   public double tickPnl;
   
   public double fees;
}
