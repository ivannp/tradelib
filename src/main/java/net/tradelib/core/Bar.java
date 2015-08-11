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

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Bar {
   private String symbol;
   private LocalDateTime ts;
   private double open;
   private double high;
   private double low;
   private double close;
   private double adjusted;
   private long contractInterest;
   private long volume;
   private long totalInterest;
   // The bar's duration - daily/hourly/etc.
   private Duration duration;
   // "true" if this is the last bar in the data feed (historical data feeds for instance)
   private boolean last;
   
   public Bar(String symbol, Duration duration, LocalDateTime ts, double open, double high, double low, double close,
         long contractInterest, long volume, long totalInterest) {
      
      this.symbol = symbol; this.duration = duration; this.ts = ts;
      this.open = open; this.high = high; this.low = low; this.close = close;
      this.contractInterest = contractInterest; this.volume = volume; this.totalInterest = totalInterest;
   }
   
   public Bar(String symbol, LocalDateTime ts, double open, double high, double low, double close, long volume) {
      
      this.symbol = symbol; this.duration = Duration.ofDays(1); this.ts = ts;
      this.open = open; this.high = high; this.low = low; this.close = close;
      this.volume = volume;
   }
   
   public Bar(String symbol, LocalDateTime ts, double open, double high, double low, double close,
         long contractInterest, long volume, long totalInterest) {
      
      this.symbol = symbol; this.duration = Duration.ofDays(1); this.ts = ts;
      this.open = open; this.high = high; this.low = low; this.close = close;
      this.contractInterest = contractInterest; this.volume = volume; this.totalInterest = totalInterest;
   }
   
   public Bar(Bar bar) {
      symbol = bar.symbol;
      ts = bar.ts;
      open = bar.open; high = bar.high; low = bar.low; close = bar.close; adjusted = bar.adjusted;
      contractInterest = bar.contractInterest; volume = bar.volume; totalInterest = bar.totalInterest;
      duration = bar.duration;
      last = bar.last;
      
   }
   
   public String getSymbol() { return symbol; }
   public void setSymbol(String ss) { symbol = ss; }

   public LocalDateTime getDateTime() { return ts; }
   public void setDateTime(LocalDateTime ts) { this.ts = ts; }
   
   public double getOpen() { return open; }
   public void setOpen(double o) { open = o; }
   
   public double getHigh() { return high; }
   public void setHigh(double h) { high = h; }
   
   public double getLow() { return low; }
   public void setLow(double l) { low = l; }
   
   public double getClose() { return close; }
   public void setClose(double cl) { close = cl; }
   
   public double getAdjusted() { return adjusted; }
   public void setAdjusted(double a) { adjusted = a; }

   public long getContractInterest() { return contractInterest; }
   public void setContractInterest(long ci) { contractInterest = ci; }
   
   public long getVolume() { return volume; }
   public void setVolume(long vv) { volume = vv; }
   
   public long getTotalInterest() { return totalInterest; }
   public void setTotalInterest(long ti) { totalInterest = ti; }
   
   public Duration getDuration() { return duration; }
   public void setDuration(Duration d) { duration = d; }
   
   public double getTypicalPrice() { return (getHigh() + getClose() + getLow())/3.0; }
   
   public double getCongestionPrice() { return (getOpen() + getHigh() + getLow() + 2.0*getClose())/5.0; }
   
   public boolean isLast() { return last; }
   public void setLast(boolean b) { last = b; }
   public void makeLast() { last = true; }
   
   public String toString() {
      String result = getSymbol() + ": ";
      if(duration.compareTo(Duration.ofDays(1)) == 0) {
         result += ts.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ": ";
      } else {
         result += ts.toString() + ": ";  
      }
      
      result += Double.toString(open) + "," + Double.toString(high) + "," + Double.toString(low) +"," + Double.toString(close);
      return result;
   }
}
