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

public class SortinoRatio {
   /**
    * @brief Computes the Sortino ratio for a list of returns.
    * 
    * @param returns The returns
    * @param rf The risk free average return
    * @param multiplier Mainly used to compute the Sortino ratio
    *                   for the opposite returns, i.e. short strategy.
    * 
    * @return The Sortino ratio. Double.MAX_VALUE is returned if there
    *         are no negative returns after applying the multiplier (i.e.
    *         the divisor is 0).
    */
   public static double value(List<Double> returns, double rf, double multiplier) {
      SummaryStatistics fullStats = new SummaryStatistics();
      SummaryStatistics downStats = new SummaryStatistics();
      for(int ii = 0; ii < returns.size(); ++ii) {
         double dd = (returns.get(ii) - rf) * multiplier;
         fullStats.addValue(dd);
         if(dd < rf) downStats.addValue(dd);
         else downStats.addValue(0);
      }
      
      if(downStats.getN() == 0) return Double.MAX_VALUE;
      
      return fullStats.getMean() / downStats.getStandardDeviation();
   }
   
   public static double value(List<Double> returns) {
      return value(returns, 0, 1);
   }
   
   public static double value(List<Double> returns, double rf) {
      return value(returns, rf, 1);
   }
   
   /**
    * @brief Computes the Sortino ratio for the short strategy.
    * 
    * @param returns The returns
    * 
    * @return The Sortino ratio. Double.MAX_VALUE is returned if there
    *         are no negative returns after applying the multiplier (i.e.
    *         the divisor is 0).
    */
   public static double reverseValue(List<Double> returns) {
      return value(returns, 0, -1);
   }
   
   /**
    * @brief Computes the Sortino ratio for the short strategy.
    * 
    * @param returns The returns
    * @param rf The risk free average return
    * 
    * @return The Sortino ratio. Double.MAX_VALUE is returned if there
    *         are no negative returns after applying the multiplier (i.e.
    *         the divisor is 0).
    */
   public static double reverseValue(List<Double> returns, double rf) {
      return value(returns, rf, -1);
   }
}