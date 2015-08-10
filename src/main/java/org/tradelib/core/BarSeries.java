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

import java.time.LocalDateTime;
import java.util.ArrayList;

public class BarSeries {
   private ArrayList<LocalDateTime> time = new ArrayList<LocalDateTime>();
   private ArrayList<Double> open = new ArrayList<Double>();
   private ArrayList<Double> high = new ArrayList<Double>();
   private ArrayList<Double> low = new ArrayList<Double>();
   private ArrayList<Double> close = new ArrayList<Double>();
   private ArrayList<Long> volume = new ArrayList<Long>();
   
   public void add(Bar bar) {
      time.add(bar.getDateTime());
      open.add(bar.getOpen());
      high.add(bar.getHigh());
      low.add(bar.getLow());
      close.add(bar.getClose());
      volume.add(bar.getVolume());
   }
   
   public double getOpen(int id) { return open.get(open.size() - id - 1); }
   public double getOpen() { return open.get(open.size() - 1); }
   
   public double getHigh(int id) { return high.get(high.size() - id - 1); }
   public double getHigh() { return high.get(high.size() - 1); }
   
   public double getLow(int id) { return low.get(low.size() - id - 1); }
   public double getLow() { return low.get(low.size() - 1); }
   
   public double getClose(int id) { return close.get(close.size() - id - 1); }
   public double getClose() { return close.get(close.size() - 1); }
   
   public long getVolume(int id) { return volume.get(volume.size() - id - 1); }
   public long getVolume() { return volume.get(volume.size() - 1); }
}
