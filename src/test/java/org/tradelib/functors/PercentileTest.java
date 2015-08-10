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

public class PercentileTest {

   @Test
   public void test() {
      Double [] inputs = { 204.97, 205.45, 202.74, 200.14, 201.99, 199.45, 201.92, 204.84, 204.06, 206.12,
                           205.55, 204.63, 206.81, 206.93, 208.92, 209.78, 210.11, 210.13, 209.98, 211.24 };
      Double [] expected = { null, null, null, null, null, null, null, null, null, null, 205.21, 205.145,
                             205.195, 205.835, 206.465, 206.87, 207.925, 209.35, 209.88, 210.045 };

      Percentile percentile = new Percentile(11);
      for(int ii = 0; ii < inputs.length; ++ii) {
         percentile.add(inputs[ii]);
         double val = percentile.last(0.75);
         if(expected[ii] == null) {
            assertTrue(Double.isNaN(val));
         } else {
            assertEquals(val, expected[ii], 1e-6);
         }
      }
   }
}
