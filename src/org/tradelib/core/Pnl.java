package org.tradelib.core;

public class Pnl {
   public double realized;
   public double unrealized;
   
   public Pnl() {
      realized = 0.0;
      unrealized = 0.0;
   }
   
   public void add(double realized, double unrealized) {
      this.realized += realized;
      this.unrealized += unrealized;
   }
}
