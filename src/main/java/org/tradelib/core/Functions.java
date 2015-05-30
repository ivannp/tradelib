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
