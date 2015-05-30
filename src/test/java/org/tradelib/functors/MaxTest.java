package org.tradelib.functors;

import static org.junit.Assert.*;
import org.junit.Test;

public class MaxTest {

   @Test
   public void test() {
      String [] inputs = { "2024","1990.5","1983.25","1965","2008.25","2060","2067","2072.5","2079","2078.75","2084.25",
            "2085.75","2076.75","2052.5","2046.25","2016","1994.5","2019.5","2055","2035.25","2022.5","2016","2007.5",
            "1989","2013","2016.75","2026.5","2056.5","2044","2053.5","2030","1991.5","2018.5","1988.5","2017","2042",
            "2030","2055","2053","2042.5"};
      String [] expected = { null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
            "2085.75","2085.75","2085.75","2085.75","2085.75","2085.75","2085.75","2085.75","2085.75","2085.75","2085.75",
            "2085.75","2076.75","2056.5","2056.5","2056.5","2056.5","2056.5","2056.5","2056.5","2056.5"};
      Max mm = new Max(20);
      for(int ii = 0; ii < inputs.length; ++ii) {
         mm.add(Double.parseDouble(inputs[ii]));
         double val = mm.last();
         if(expected[ii] == null) {
            assertTrue(Double.isNaN(val));
         } else {
            assertEquals(val, Double.parseDouble(expected[ii]), 1e-6);
         }
      }
   }

}
