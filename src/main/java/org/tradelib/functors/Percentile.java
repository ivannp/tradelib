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

import java.util.Arrays;

public class Percentile {
   private int length;
   private int count;
   private double [] buffer;
   private double [] sorted;
   private boolean isSorted;
   
   public Percentile(int length) {
      this.length = length;
      this.count = 0;
      this.buffer = new double [length];
      this.sorted = new double [length];
      this.isSorted = false;
   }
   
   public int getLength() { return length; }
   public int getCount() { return count; }
   
   public void add(double value) {
      buffer[count % length] = value;
      ++count;
      isSorted = false;
   }
   
   public double last(double percentile) {
      if(count < length) return Double.NaN;
      
      if(!isSorted) {
         System.arraycopy(buffer, 0, sorted, 0, length);
         Arrays.sort(sorted);
         isSorted = true;
      }
      
      double n = (length - 1.0)*percentile + 1.0;
      if(n == 1d) {
         return sorted[0];
      } else if(n == length) {
         return sorted[length - 1];
      } else {
         int k = (int)n;
         double d = n - k;
         return sorted[k - 1] + d * (sorted[k] - sorted[k - 1]);
      }
   }
}
