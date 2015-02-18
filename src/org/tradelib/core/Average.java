package org.tradelib.core;

public class Average {
   private int size;
   private double mean;
   
   public Average() {
      size = 0; mean = 0.0;
   }
   
   public void add(double d) {
      ++size;
      if(size > 1) {
         mean += (d - mean)/size; 
      } else {
         mean = d;
      }
   }
   
   public double get() { return mean; }
   public int getSize() { return size; }
}
