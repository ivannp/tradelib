package org.tradelib.functors;

import com.google.common.collect.EvictingQueue;

public class Max {
   private int length;
   private int count;
   private EvictingQueue<Double> eq;
   
   public Max(int length) {
      this.length = length;
      this.count = 0;
      this.eq = EvictingQueue.create(this.length);
   }
   
   public int getLength() { return length; }
   public int getCount() { return count; }
   
   public void add(double value) {
      ++count;
      eq.add(value);
   }
   
   public double last() {
      return count < length ? Double.NaN : eq.stream().max(Double::compare).get();
   } 
}
