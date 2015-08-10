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

package org.tradelib.functors;

public class Atr {
   public enum MaType { SMA, EMA }
   private IMa ima;
   private int n;
   private double dn;
   private double close; // Previous close
   int count;
   
   public Atr(int n) {
      this.count = 0;
      this.ima = new Ema(n, true);
      this.n = n;
      this.dn = n;
   }
   
   public Atr(int n, boolean wilder) {
      this.count = 0;
      this.ima = new Ema(n, wilder);
      this.n = n;
      this.dn = n;
   }
   
   public Atr(int n, MaType mt) {
      this.count = 0;
      if(mt == MaType.SMA) {
         this.ima = new Sma(n);
      } else {
         this.ima = new Ema(n, true);
      }
      this.n = n;
      this.dn = n;
   }
   
   public int getLength() { return n; }
   public int getCount() { return count; }
   
   public double add(double high, double low, double close) {
      
      assert high >= low && high >= close && low <= close;
      
      if(count > 0) {
         double trueHigh = Math.max(high, this.close); 
         double trueLow = Math.min(low, this.close);
         ima.add(trueHigh - trueLow);
         this.close = close;
      } else {
         // No previous close - use high/low
         // ema_.add(high.subtract(low));
         this.close = close;
      }
      
      ++count;
      
      return last();
   }
   
   public double last() { return ima.last(); }
}
