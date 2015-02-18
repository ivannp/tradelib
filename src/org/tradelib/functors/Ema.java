package org.tradelib.functors;

public class Ema implements IMa {
   private final int n;
   private final double dn;
   private double previous;
   private int count;
   private final double k;
   
   public Ema(int n) {
      this.previous = 0.0;
      this.count = 0;
      this.n = n;
      this.dn = n;
      this.k = 2.0 / (this.dn + 1.0);
   }
   
   public Ema(int n, boolean wilder) {
      this.previous = 0.0;
      this.count = 0;
      this.n = n;
      this.dn = n;
      if(wilder) {
         this.k = 1.0 / dn;
      } else {
         this.k = 2.0 / (this.dn + 1.0);
      }
   }
   
   public int getLength() { return n; }
   public int getCount() { return count; }
   
   public double add(double value) {
      ++count;
      if(count > n) {
         previous = value * k + previous * (1.0 - k); 
      } else if(count == n) {
         previous = (previous + value) / dn;
      } else {
         previous += value;
      }
      return last();
   }
   
   public double last() { return count < n ? Double.NaN : previous; }
}
