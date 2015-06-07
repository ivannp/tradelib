package org.tradelib.functors;

public class ZigZag {

   private double deviation = 3;
   private int n = 21;
   
   private int count = 0;
   
   private Sma returns;
   
   private int age = 0;
   private double correction = 0;
   private double target = 0;
   private double anchor = 0;
   private int trend = 0;
   
   private double prevPrice = 0;
   
   // The interface
   public int getAge() { return age; }
   public double getCorrection() { return correction; }
   public int getTrend() { return trend; }
   public double getTarget() { return target; }
   
   public ZigZag(int n, double deviation) {
      this.n = n;
      this.deviation = deviation;
      this.returns = new Sma(n);
   }
   
   public void add(double price) {
      if(count > 0) {
         returns.add(Math.abs(price/prevPrice - 1));
      }
      
      if(count >= n) {
         double newTarget = returns.last()*deviation;
         if(count == n) {
            target = newTarget;
            anchor = price;
         } else {
            if(trend == 1) {
               double pct = 1 - price/anchor;
               boolean newTrend = pct > target;
               
               if(newTrend) {
                  age = 0;
                  correction = 0;
                  target = newTarget;
                  trend = -1;
                  anchor = price;
               } else {
                  ++age;

                  if(price >= anchor) {
                     correction = 0;
                     target = newTarget;
                     anchor = price;
                  } else {
                     correction = 1 - pct/target;
                  }
               }
            } else if(trend == -1) {
               double pct = price/anchor - 1;
               boolean newTrend = pct > target;
               
               if(newTrend) {
                  age = 0;
                  correction = 1;
                  target = newTarget;
                  trend = 1;
                  anchor = price;
               } else {
                  ++age;

                  if(price <= anchor) {
                     correction = 0;
                     target = newTarget;
                     anchor = price;
                  } else {
                     correction = pct/target;
                  }
               }
            }
            else {
               // No trend, the initial state
               if((price/anchor - 1) > target) {
                  trend = 1;
                  target = newTarget;
                  anchor = price;
               } else if((1 - price/anchor) > target) {
                  trend = -1;
                  target = newTarget;
                  anchor = price;
               }
            }
         }
      }
      
      ++count;
      prevPrice = price;
   }
}
