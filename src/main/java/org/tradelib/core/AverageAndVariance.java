package org.tradelib.core;

public class AverageAndVariance {
   private int size;
   private double mean;
   private double variance;
   
   public AverageAndVariance() {
      size = 0; mean = 0.0; variance = 0.0;
   }
   
   public void add(double d) {
      ++size;
      if(size > 1) {
         double newMean = mean + (d - mean)/size;
         double newVariance = variance + (d - mean)*(d - newMean);
         
         mean = newMean;
         variance = newVariance;
      } else {
         mean = d;
         variance = 0.0;
      }
   }
   
   public double getAverage() { return mean; }
   public double getVariance() {
      switch(size) {
      case 0: return 0.0;
      case 1: return variance;
      default: return variance/(size - 1); 
      }
   }
   
   public double getStdDev() { return Math.sqrt(variance); }
   public int getSize() { return size; }
}
