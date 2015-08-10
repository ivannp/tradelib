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

package org.tradelib.core;

public class AverageAndVariance {
   private int size;
   private double mean;
   private double variance;
   
   public AverageAndVariance() {
      size = 0; mean = 0.0; variance = 0.0;
   }
   
   public void add(double d) {
      ++size;
      if(size > 1) {
         double newMean = mean + (d - mean)/size;
         double newVariance = variance + (d - mean)*(d - newMean);
         
         mean = newMean;
         variance = newVariance;
      } else {
         mean = d;
         variance = 0.0;
      }
   }
   
   public double getAverage() { return mean; }
   public double getVariance() {
      switch(size) {
      case 0: return 0.0;
      case 1: return variance;
      default: return variance/(size - 1); 
      }
   }
   
   public double getStdDev() { return Math.sqrt(variance); }
   public int getSize() { return size; }
}
