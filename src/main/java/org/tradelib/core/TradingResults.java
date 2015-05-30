package org.tradelib.core;

import java.util.ArrayList;
import java.util.List;

public class TradingResults {
   public Series pnl;
   public List<Trade> stats;
   public TradeSummary all;
   public TradeSummary longs;
   public TradeSummary shorts;
   
   public TradingResults() {
      pnl = null;
      stats = new ArrayList<Trade>();
      all = new TradeSummary();
      longs = new TradeSummary();
      shorts = new TradeSummary();
   }
}
