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

public class Average {
   private int size;
   private double mean;
   
   public Average() {
      size = 0; mean = 0.0;
   }
   
   public void add(double d) {
      ++size;
      if(size > 1) {
         mean += (d - mean)/size; 
      } else {
         mean = d;
      }
   }
   
   public double get() { return mean; }
   public int getSize() { return size; }
}
