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

import static org.junit.Assert.*;

import org.junit.Test;
import org.tradelib.functors.Atr.MaType;

public class AtrTest {

   @Test
   public void test() {
      String [] close = {"124.41","125.34","125.68","125.92","127.32","129.32","131.65","131.39","129.65","131.07",
                         "131.82","131.82","132.82","134.91","133.19","134.96","133.4","132.92","134.77","134.36"};
      String [] high = {"124.59","125.66","126.21","126.17","127.66","129.63","132.49","131.97","130.37","131.11",
                        "132.31","132.61","134.12","135.03","134.68","135.11","135.59","134.18","135.12","135.19"};
      String [] low = {"124.02","124.81","125.53","125.65","126.15","127.96","130.19","130.17","129.42","129.04",
                       "130.92","131.14","132.7","132.68","133.03","134.07","132.96","132.28","134.15","134.05"};
      String [] expected = {null,null,null,null,null,null,null,null,null,null,"1.709","1.6851","1.74659","1.806931",
                            "1.814238","1.824814","1.905333","1.904799","1.934319","1.854888"};

      Atr atr = new Atr(10);
      for(int ii = 0; ii < close.length; ++ii) {
         atr.add(Double.parseDouble(high[ii]), Double.parseDouble(low[ii]), Double.parseDouble(close[ii]));
         double val = atr.last();
         if(expected[ii] == null) {
            assertTrue(Double.isNaN(val));
         } else {
            assertEquals(val, Double.parseDouble(expected[ii]), 1e-6);
         }
      }
      
      String [] expectedRegular = {null,null,null,null,null,null,null,null,null,null,
            "1.709","1.665545","1.780901","1.884373","1.883578","1.8902","2.024709","2.002035","2.038029","1.874751"};

      atr = new Atr(10, false);
      for(int ii = 0; ii < close.length; ++ii) {
         atr.add(Double.parseDouble(high[ii]), Double.parseDouble(low[ii]), Double.parseDouble(close[ii]));
         double val = atr.last();
         if(expectedRegular[ii] == null) {
            assertTrue(Double.isNaN(val));
         } else {
            assertEquals(val, Double.parseDouble(expectedRegular[ii]), 1e-6);
         }
      }
      
      String [] expectedSma = {null,null,null,null,null,null,null,null,null,null,
            "1.709","1.731","1.874","2.057","2.071","2.032","1.978","1.988","2.011","1.918"};
      atr = new Atr(10, MaType.SMA);
      for(int ii = 0; ii < close.length; ++ii) {
         atr.add(Double.parseDouble(high[ii]), Double.parseDouble(low[ii]), Double.parseDouble(close[ii]));
         double val = atr.last();
         if(expectedSma[ii] == null) {
            assertTrue(Double.isNaN(val));
         } else {
            assertEquals(val, Double.parseDouble(expectedSma[ii]), 1e-6);
         }
      }
   }
}
