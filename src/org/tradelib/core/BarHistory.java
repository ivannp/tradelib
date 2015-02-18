package org.tradelib.core;

import java.util.ArrayList;
import java.util.List;

public class BarHistory {
   private List<Bar> bars;
   
   public BarHistory() {
      bars = new ArrayList<Bar>();
   }
   
   public double getOpen(int id) { return bars.get(bars.size() - id - 1).getOpen(); }
   public double getOpen() { return bars.get(bars.size() - 1).getOpen(); }
   
   public double getHigh(int id) { return bars.get(bars.size() - id - 1).getHigh(); }
   public double getHigh() { return bars.get(bars.size() - 1).getHigh(); }
   
   public double getLow(int id) { return bars.get(bars.size() - id - 1).getLow(); }
   public double getLow() { return bars.get(bars.size() - 1).getLow(); }
   
   public double getClose(int id) { return bars.get(bars.size() - id - 1).getClose(); }
   public double getClose() { return bars.get(bars.size() - 1).getClose(); }
   
   public long getVolume(int id) { return bars.get(bars.size() - id - 1).getVolume(); }
   public long getVolume() { return bars.get(bars.size() - 1).getVolume(); }
   
   public long getContractInterest(int id) { return bars.get(bars.size() - id - 1).getContractInterest(); }
   public long getContractInterest() { return bars.get(bars.size() - 1).getContractInterest(); }
   
   public long getTotalInterest(int id) { return bars.get(bars.size() - id - 1).getTotalInterest(); }
   public long getTotalInterest() { return bars.get(bars.size() - 1).getTotalInterest(); }
   
   public void add(Bar bar) { bars.add(bar); }
   
   public Bar getBar(int id) { return bars.get(bars.size() - id - 1); }
   public Bar getBar() { return bars.get(bars.size() - 1); }
   
   public TimeSeries<Double> getCloseSeries() {
      TimeSeries<Double> ts = new TimeSeries<Double>(1, bars.size());
      for(Bar bar : bars) {
         ts.add(bar.getDateTime(), bar.getClose());
      }
      return ts;
   }
   
   public long size() { return bars.size(); }
}
