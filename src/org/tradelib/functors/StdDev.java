package org.tradelib.functors;

import com.google.common.collect.EvictingQueue;

public class StdDev {
   private EvictingQueue<Double> eq;
   double mean;
   double var;
   int count;
   int n;
   double dn; // N as a double
   
   public StdDev(int n) {
      this.mean = 0.0;
      this.var = 0.0;
      this.count = 0;
      this.n = n;
      this.dn = n;
      this.eq = EvictingQueue.create(this.n);
   }
   
   public int getLength() { return n; }
   public int getCount() { return count; }
   
   public double add(double value)
   {
      // The number of elements including the one added
      ++count;
      if(count > n) {
         double oldMean = mean;
         mean += (value - eq.peek()) / dn;
         var += (value - mean + eq.peek() - oldMean)*(value - eq.peek()) / (dn - 1.0);
      } else if(count == n) {
         double newMean = mean + (value - mean) / dn;
         double newVar = var + (value - mean) * (value - newMean); 

         mean = newMean;
         var = newVar / (dn - 1.0);
      } else if(count > 1) {
         double dd = value - mean;
         double newMean = mean + dd / count;
         double newVar = var + dd * (value - newMean);

         mean = newMean;
         var = newVar;
      } else {
         mean = value;
         var = 0.0;
      }

      // Add the value to the buffer
      eq.add(value);
      
      return stdDev();
   }

   public double sma() { return count >= n ? mean : Double.NaN; }
   public double var() { return count >= n ? var : Double.NaN; }
   public double stdDev() { return count >= n ? Math.sqrt(var) : Double.NaN; }
}
