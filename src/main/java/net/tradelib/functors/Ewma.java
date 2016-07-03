// Copyright 2016 Ivan Popivanov
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

public class Ewma implements IMovingAverage {
   
   private final int n;
   private double previous;
   private int count;
   private final double k;
   
   public Ewma(int n) {
      this.previous = 0.0;
      this.count = 0;
      this.n = n;
      this.k = 2.0 / (this.n + 1.0);
   }

   @Override
   public double add(double value) {
      ++count;
      if(count > 1) {
         previous = value*k + previous*(1-k);
      } else {
         previous = value;
      }
      return last();
   }

   @Override
   public double last() {
      return previous;
   }

   @Override
   public int getLength() {
      return n;
   }

   @Override
   public int getCount() {
      return count;
   }
}
