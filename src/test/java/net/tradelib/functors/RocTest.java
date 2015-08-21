package net.tradelib.functors;

import static org.junit.Assert.*;

import org.junit.Test;

public class RocTest {

   @Test
   public void test() {
      double [] inputs = {124.41,125.34,125.68,125.92,127.32,129.32,131.65,131.39,129.65,131.07,
                          131.82,131.82,132.82,134.91,133.19,134.96,133.4,132.92,134.77,134.36};
      Roc roc = new Roc(3);
      for(int ii = 0; ii < inputs.length; ++ii) {
         roc.add(inputs[ii]);
         double val = roc.last();
         if(ii < 3) {
            assertTrue(Double.isNaN(val));
         } else {
            assertEquals(val, inputs[ii]/inputs[ii-3] - 1, 1e-6); 
         }
      }
   }
}
