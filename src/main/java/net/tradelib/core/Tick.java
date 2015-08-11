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

public class Tick {
   private String symbol;
   private LocalDateTime ts;
   private double price;
   private long volume;
   
   public Tick(String s, LocalDateTime ts, double price, long volume) {
      this.symbol = s;
      this.ts = ts;
      this.price = price;
      this.volume = volume;
   }
   
   public Tick(String s, LocalDateTime ts, double price) {
      this.symbol = s;
      this.ts = ts;
      this.price = price;
      this.volume = Long.MIN_VALUE;
   }
   
   public String getSymbol() { return symbol; }
   public void setSymbol(String ss) { this.symbol = ss; }

   public LocalDateTime getDateTime() { return ts; }
   public void setDateTime(LocalDateTime ts) { this.ts = ts; }
   
   public double getPrice() { return price; }
   public void setPrice(double p) { this.price = p; }
   
   public long getVolume() { return volume; }
   public void setVolume(long vv) { this.volume = vv; }
}
