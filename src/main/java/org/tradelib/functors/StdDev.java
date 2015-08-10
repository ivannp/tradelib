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

import com.google.common.collect.EvictingQueue;

public class StdDev {
   private EvictingQueue<Double> eq;
   private double mean;
   private double var;
   private int count;
   private int n;
   private double dn; // N as a double
   
   public StdDev(int n) {
      this.mean = 0.0;
      this.var = 0.0;
      this.count = 0;
      this.n = n;
      this.dn = n;
      this.eq = EvictingQueue.create(this.n);
   }
   
   public int getLength() { return n; }
   public int getCount() { return count; }
   
   public double add(double value)
   {
      // The number of elements including the one added
      ++count;
      if(count > n) {
         double oldMean = mean;
         mean += (value - eq.peek()) / dn;
         var += (value - mean + eq.peek() - oldMean)*(value - eq.peek()) / (dn - 1.0);
      } else if(count == n) {
         double newMean = mean + (value - mean) / dn;
         double newVar = var + (value - mean) * (value - newMean); 

         mean = newMean;
         var = newVar / (dn - 1.0);
      } else if(count > 1) {
         double dd = value - mean;
         double newMean = mean + dd / count;
         double newVar = var + dd * (value - newMean);

         mean = newMean;
         var = newVar;
      } else {
         mean = value;
         var = 0.0;
      }

      // Add the value to the buffer
      eq.add(value);
      
      return stdDev();
   }

   public double sma() { return count >= n ? mean : Double.NaN; }
   public double var() { return count >= n ? var : Double.NaN; }
   public double stdDev() { return count >= n ? Math.sqrt(var) : Double.NaN; }
}
