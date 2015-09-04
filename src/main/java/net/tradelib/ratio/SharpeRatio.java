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

package net.tradelib.ratio;

import java.util.List;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

public class SharpeRatio {
   /**
    * @brief Computes the Sharpe ratio for a list of returns.
    * 
    * @param returns The returns
    * @param rf The risk free average return
    * 
    * @return The Sharpe ratio
    */
   public static double value(List<Double> returns, double rf) {
      SummaryStatistics ss = new SummaryStatistics();
      returns.forEach((xx) -> ss.addValue(xx - rf));
      
      return ss.getMean() / ss.getStandardDeviation();
   }
   
   public static double value(List<Double> returns) {
      return value(returns, 0);
   }
}
