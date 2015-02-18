package org.tradelib.functors;

public class Atr {
   enum MaType { SMA, EMA }
   private IMa ima;
   private int n;
   private double dn;
   private double close; // Previous close
   int count;
   
   public Atr(int n) {
      this.count = 0;
      this.ima = new Ema(n, true);
      this.n = n;
      this.dn = n;
   }
   
   public Atr(int n, boolean wilder) {
      this.count = 0;
      this.ima = new Ema(n, wilder);
      this.n = n;
      this.dn = n;
   }
   
   public Atr(int n, MaType mt) {
      this.count = 0;
      if(mt == MaType.SMA) {
         this.ima = new Sma(n);
      } else {
         this.ima = new Ema(n, true);
      }
      this.n = n;
      this.dn = n;
   }
   
   public int getLength() { return n; }
   public int getCount() { return count; }
   
   public double add(double high, double low, double close) {
      
      assert high >= low && high >= close && low <= close;
      
      if(count > 0) {
         double trueHigh = Math.max(high, this.close); 
         double trueLow = Math.min(low, this.close);
         ima.add(trueHigh - trueLow);
         this.close = close;
      } else {
         // No previous close - use high/low
         // ema_.add(high.subtract(low));
         this.close = close;
      }
      
      ++count;
      
      return last();
   }
   
   public double last() { return ima.last(); }
}
