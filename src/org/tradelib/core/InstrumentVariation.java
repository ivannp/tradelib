package org.tradelib.core;

import java.math.BigDecimal;

public class InstrumentVariation {
   private String symbol;
   private double factor;
   private double tick;
   
   InstrumentVariation(String symbol, double factor, double tick) {
      this.symbol = symbol;
      this.factor = factor;
      this.tick = tick;
   }
   
   public double getTick() { return tick; }
   public double getFactor() { return factor; }
   public String getSymbol() { return symbol; }
   
   public double tickCeil(double value) { return Math.ceil(value/getTick())*getTick(); }
   public double tickFloor(double value) { return Math.floor(value/getTick())*getTick(); }
   public double price(double originalPrice) { return originalPrice/factor; }
}
