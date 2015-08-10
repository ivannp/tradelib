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

import java.math.BigDecimal;
import java.math.MathContext;

public class Functions {
   public static double sharpeRatio(double mean, double sd, int period) {
      return mean/sd*Math.sqrt(period);
   }
   
   public static boolean isZero(BigDecimal bd) {
      return bd.compareTo(BigDecimal.ZERO) == 0;
   }
   
   public static BigDecimal divide(BigDecimal aa, BigDecimal bb) {
      return aa.divide(bb,MathContext.DECIMAL128);
   }
}
