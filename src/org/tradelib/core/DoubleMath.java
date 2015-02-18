package org.tradelib.core;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class DoubleMath {
   public static double round(double value, int places) {
      if(Double.isNaN(value) || Double.isInfinite(value)) {
         return value;
      }

      if (places < 0) {
         throw new IllegalArgumentException();
      }

      BigDecimal bd = new BigDecimal(value);
      bd = bd.setScale(places, RoundingMode.HALF_UP);
      return bd.doubleValue();
   }
}
