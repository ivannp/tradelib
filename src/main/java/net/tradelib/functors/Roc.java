package net.tradelib.functors;

import com.google.common.collect.EvictingQueue;

public class Roc {
   private EvictingQueue<Double> cq;
   private double lastValue;
   private final int n;
   private int count;
   
   public Roc(int n) {
      this.n = n;
      this.count = 0;
      this.cq = EvictingQueue.create(this.n + 1);
   }
   
   public int getLength() { return n; }
   
   public double add(double value) {
      lastValue = value;
      cq.add(value);
      ++count;
      
      return last();
   }
   
   public double last() {
      if(count > n) return lastValue / cq.peek() - 1;
      else return Double.NaN;
   }
}
