package org.tradelib.core;

import java.time.Duration;
import java.util.HashMap;

public class BarHierarchy {
   private HashMap<String, HashMap<Duration, BarHistory>> historiesMap;
   
   public BarHierarchy() {
      historiesMap = new HashMap<String, HashMap<Duration,BarHistory>>();
   }

   public BarHistory getHistory(String symbol, Duration duration) {
      HashMap<Duration,BarHistory> symbolHistories = historiesMap.get(symbol);
      if(symbolHistories == null) {
         return null;
      }
      
      BarHistory barHistory = symbolHistories.get(duration);
      if(barHistory == null) {
         barHistory = new BarHistory();
         symbolHistories.put(duration, barHistory);
      }
      
      return barHistory;
   }
   
   public void addSymbol(String symbol) {
      HashMap<Duration,BarHistory> symbolHistories = historiesMap.get(symbol);
      if(symbolHistories == null) {
         symbolHistories = new HashMap<Duration, BarHistory>();
         historiesMap.put(symbol, symbolHistories);
      }
   }
   
   public BarHistory getHistory(Bar bar) {
      return getHistory(bar.getSymbol(), bar.getDuration());
   }
}
