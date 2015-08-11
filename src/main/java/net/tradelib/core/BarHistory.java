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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import net.tradelib.functors.Sma;

public class BarHistory {
   private List<Bar> bars = new ArrayList<Bar>();
   
   public interface IHistoryData {
      public double get(int id);
   }
   
   private class MutableData<T> implements IHistoryData {
      private List<T> list;

      public MutableData() {
         list = new ArrayList<T>();
      }
      
      public void add(T value) {
         list.add(value);
      }
      
      @Override
      public double get(int id) {
         return (Double)list.get(list.size() - id -1);
      }
   }
   
   public class HistoryData implements IHistoryData {
      
      private final IHistoryData container;
      
      public HistoryData(IHistoryData container) {
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
   
   private MutableData<Double> openContainer = new MutableData<Double>();
   private MutableData<Double> highContainer = new MutableData<Double>();
   private MutableData<Double> lowContainer = new MutableData<Double>();
   private MutableData<Double> closeContainer = new MutableData<Double>();
   private MutableData<Long> volumeContainer = new MutableData<Long>();
   private MutableData<Long> totalInterestContainer = new MutableData<Long>();
   private MutableData<Long> contractInterestContainer = new MutableData<Long>();
   
   public HistoryData open = new HistoryData(openContainer);
   public HistoryData high = new HistoryData(highContainer);
   public HistoryData low = new HistoryData(lowContainer);
   public HistoryData close = new HistoryData(closeContainer);
   public HistoryData volume = new HistoryData(volumeContainer);
   public HistoryData totalInterest = new HistoryData(totalInterestContainer);
   public HistoryData contractInterest = new HistoryData(contractInterestContainer);

   public LocalDateTime getDateTime(int id) { return bars.get(bars.size() - id - 1).getDateTime(); }
   public LocalDateTime getDateTime() { return bars.get(bars.size() - 1).getDateTime(); }
   
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
