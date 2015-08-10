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

package org.tradelib.functors;

public class Ema implements IMa {
   private final int n;
   private final double dn;
   private double previous;
   private int count;
   private final double k;
   
   public Ema(int n) {
      this.previous = 0.0;
      this.count = 0;
      this.n = n;
      this.dn = n;
      this.k = 2.0 / (this.dn + 1.0);
   }
   
   public Ema(int n, boolean wilder) {
      this.previous = 0.0;
      this.count = 0;
      this.n = n;
      this.dn = n;
      if(wilder) {
         this.k = 1.0 / dn;
      } else {
         this.k = 2.0 / (this.dn + 1.0);
      }
   }
   
   public int getLength() { return n; }
   public int getCount() { return count; }
   
   public double add(double value) {
      ++count;
      if(count > n) {
         previous = value * k + previous * (1.0 - k); 
      } else if(count == n) {
         previous = (previous + value) / dn;
      } else {
         previous += value;
      }
      return last();
   }
   
   public double last() { return count < n ? Double.NaN : previous; }
}
