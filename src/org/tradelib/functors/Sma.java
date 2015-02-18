package org.tradelib.functors;

import com.google.common.collect.EvictingQueue;

public class Sma implements IMa {
   private double mean;
   private EvictingQueue<Double> cq;
   private final int n;
   private final double dn;
   private int count;
   
   public Sma(int n) {
      this.count = 0;
      this.n = n;
      this.dn = n;
      this.mean = 0.0;
      this.cq = EvictingQueue.create(this.n);
   }
   
   public int getLength() { return n; }
   public int getCount() { return count; }
   
   public double add(double value)
   {
      // The number of elements including the one added
      ++count;
      if(count > n) {
         mean += (value - cq.peek()) / dn; 
      } else if(count == n) {
         mean += (value - mean) / dn; 
      } else if(count > 1) {
         mean += (value - mean) / count;
      } else {
         mean = value;
      }

      // Add the value to the buffer
      cq.add(value);
      
      return last();
   }
   
   public double last() { return count >= n ? mean : Double.NaN; }
}
