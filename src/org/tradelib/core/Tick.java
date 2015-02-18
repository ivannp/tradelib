package org.tradelib.core;

import java.time.LocalDateTime;

public class Tick {
   private String symbol;
   private LocalDateTime ts;
   private double price;
   private long volume;
   
   public Tick(String s, LocalDateTime ts, double price, long volume) {
      this.symbol = s;
      this.ts = ts;
      this.price = price;
      this.volume = volume;
   }
   
   public Tick(String s, LocalDateTime ts, double price) {
      this.symbol = s;
      this.ts = ts;
      this.price = price;
      this.volume = Long.MIN_VALUE;
   }
   
   public String getSymbol() { return symbol; }
   public void setSymbol(String ss) { this.symbol = ss; }

   public LocalDateTime getDateTime() { return ts; }
   public void setDateTime(LocalDateTime ts) { this.ts = ts; }
   
   public double getPrice() { return price; }
   public void setPrice(double p) { this.price = p; }
   
   public long getVolume() { return volume; }
   public void setVolume(long vv) { this.volume = vv; }
}
