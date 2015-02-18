package org.tradelib.functors;

import static org.junit.Assert.*;

import org.junit.Test;

public class StdDevTest {

   @Test
   public void test() {
      String [] inputs = {"124.41","125.34","125.68","125.92","127.32","129.32","131.65","131.39","129.65","131.07",
                          "131.82","131.82","132.82","134.91","133.19","134.96","133.4","132.92","134.77","134.36"};
      String [] expectedSma = {null,null,null,null,null,null,null,null,null,"128.175","128.916","129.564","130.278",
                               "131.177","131.764","132.328","132.503","132.656","133.168","133.497"};
      String [] expectedStdDev = {null,null,null,null,null,null,null,null,null,"2.75949","2.627907","2.440388",
                                  "2.211524","2.065559","1.637371","1.67296","1.685639","1.642269","1.377799","1.202858"};
      
      StdDev sd = new StdDev(10);
      for(int ii = 0; ii < inputs.length; ++ii) {
         sd.add(Double.parseDouble(inputs[ii]));
         double val = sd.sma();
         if(expectedSma[ii] == null) {
            assertTrue(Double.isNaN(val));
         } else {
            assertEquals(val, Double.parseDouble(expectedSma[ii]), 1e-6);
         }
         val = sd.stdDev();
         if(expectedStdDev[ii] == null) {
            assertTrue(Double.isNaN(val));
         } else {
            assertEquals(val, Double.parseDouble(expectedStdDev[ii]), 1e-6);
         }
      }
   }
}
