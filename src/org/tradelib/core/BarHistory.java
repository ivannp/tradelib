package org.tradelib.core;

import java.util.ArrayList;
import java.util.List;

import org.tradelib.functors.Sma;

public class BarHistory {
   private List<Bar> bars = new ArrayList<Bar>();
   
   public interface ISeries {
      public double get(int id);
   }
   
   private class MutableSeries<T> implements ISeries {
      private List<T> list;

      public MutableSeries() {
         list = new ArrayList<T>();
      }
      
      public void add(T value) {
         list.add(value);
      }
      
      @Override
      public double get(int id) {
         return (double)list.get(list.size() - id -1);
      }
   }
   
   public class Series implements ISeries {
      
      private final ISeries container;
      
      public Series(ISeries container) {
         this.container = container;
      }

      @Override
      public double get(int id) {
         return container.get(id);
      }
      
      public double sma(int len) {
         Sma ff = new Sma(len);
         for(int ii = 0; ii < len; ++ii) {
            ff.add(get(ii));
         }
         return ff.last();
      }
   }
   
   private MutableSeries<Double> openContainer = new MutableSeries<Double>();
   private MutableSeries<Double> highContainer = new MutableSeries<Double>();
   private MutableSeries<Double> lowContainer = new MutableSeries<Double>();
   private MutableSeries<Double> closeContainer = new MutableSeries<Double>();
   private MutableSeries<Long> volumeContainer = new MutableSeries<Long>();
   private MutableSeries<Long> totalInterestContainer = new MutableSeries<Long>();
   private MutableSeries<Long> contractInterestContainer = new MutableSeries<Long>();
   
   public Series open = new Series(openContainer);
   public Series high = new Series(highContainer);
   public Series low = new Series(lowContainer);
   public Series close = new Series(closeContainer);
   public Series volume = new Series(volumeContainer);
   public Series totalInterest = new Series(totalInterestContainer);
   public Series contractInterest = new Series(contractInterestContainer);
   
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
   
   public void add(Bar bar) {
      bars.add(bar);
      
      openContainer.add(bar.getOpen());
      highContainer.add(bar.getHigh());
      lowContainer.add(bar.getLow());
      closeContainer.add(bar.getClose());
      volumeContainer.add(bar.getVolume());
      contractInterestContainer.add(bar.getContractInterest());
      totalInterestContainer.add(bar.getTotalInterest());
   }
   
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
