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

public class EmaTest {

   @Test
   public void test() {
      String [] inputs = {"124.41","125.34","125.68","125.92","127.32","129.32","131.65","131.39","129.65","131.07",
            "131.82","131.82","132.82","134.91","133.19","134.96","133.4","132.92","134.77","134.36"};
      String [] expected = {null,null,null,null,null,null,null,null,null,"128.175","128.837727","129.379959",
            "130.005421","130.897162","131.314042","131.976943","132.235681","132.360103","132.798266","133.082217"};
      Ema ema = new Ema(10);
      for(int ii = 0; ii < inputs.length; ++ii) {
         ema.add(Double.parseDouble(inputs[ii]));
         double val = ema.last();
         if(expected[ii] == null) {
            assertTrue(Double.isNaN(val));
         } else {
            assertEquals(val, Double.parseDouble(expected[ii]), 1e-6);
         }
      }
   }
}
