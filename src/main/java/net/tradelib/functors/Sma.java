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

package net.tradelib.functors;

import com.google.common.collect.EvictingQueue;

public class Sma implements IMa {
   private double mean;
   private EvictingQueue<Double> cq;
   private final int n;
   private final double dn;
   private int count;
   
   public Sma(int n) {
      this.count = 0;
      this.n = n;
      this.dn = n;
      this.mean = 0.0;
      this.cq = EvictingQueue.create(this.n);
   }
   
   public int getLength() { return n; }
   public int getCount() { return count; }
   
   public double add(double value)
   {
      // The number of elements including the one added
      ++count;
      if(count > n) {
         mean += (value - cq.peek()) / dn; 
      } else if(count == n) {
         mean += (value - mean) / dn; 
      } else if(count > 1) {
         mean += (value - mean) / count;
      } else {
         mean = value;
      }

      // Add the value to the buffer
      cq.add(value);
      
      return last();
   }
   
   public double last() { return count >= n ? mean : Double.NaN; }
}
