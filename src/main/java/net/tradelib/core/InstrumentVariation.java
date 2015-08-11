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

import java.math.BigDecimal;

public class InstrumentVariation {
   private String symbol;
   private double factor;
   private double tick;
   
   InstrumentVariation(String symbol, double factor, double tick) {
      this.symbol = symbol;
      this.factor = factor;
      this.tick = tick;
   }
   
   public double getTick() { return tick; }
   public double getFactor() { return factor; }
   public String getSymbol() { return symbol; }
   
   public double tickCeil(double value) { return Math.ceil(value/getTick())*getTick(); }
   public double tickFloor(double value) { return Math.floor(value/getTick())*getTick(); }
   public double price(double originalPrice) { return originalPrice/factor; }
   
   public Order transform(Order oo) {
      Order result = oo.clone();
      if(result.isLimit()) result.setLimit(price(result.getLimit()));
      if(result.isStop()) result.setStop(price(result.getStop()));
      return result;
   }
}
