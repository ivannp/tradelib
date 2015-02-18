package org.tradelib.core;

import java.math.BigDecimal;
import java.util.Objects;

public class Instrument {
   public enum Type {
      NONE, FUTURE, INDEX, STOCK
   }

   private final Type type;
   private final String symbol;
   private final BigDecimal tick;
   private final BigDecimal bpv;
   private String name;
   
   public boolean equals(Object o) {
      if(this == o) return true;
      if(o == null) return false;
      if(this.getClass() != o.getClass()) return false;
      final Instrument ii = (Instrument)o;
      if(type != ii.type) return false;
      if(isFuture()) {
         // If the tick, or the bpv, of either object is ZERO,
         // we consider them equal according to this criteria.
         // This approach allows us to lookup a future using
         // a subset of the information.
         if(tick.compareTo(BigDecimal.ZERO) != 0 &&
            ii.tick.compareTo(BigDecimal.ZERO) != 0 &&
            tick.compareTo(ii.tick) != 0) {
            return false;
         }
         
         if(bpv.compareTo(BigDecimal.ZERO) != 0 &&
               ii.bpv.compareTo(BigDecimal.ZERO) != 0 &&
               bpv.compareTo(ii.bpv) != 0) {
               return false;
            }
      }
      return true;
   }
   
   public int hashCode() {
      return Objects.hash(type, symbol, tick, bpv, name);
   }
   
   private Instrument(Type type, String symbol, BigDecimal tick, BigDecimal bpv, String name) {
      this.type = type; this.symbol = symbol; this.tick = tick; this.bpv = bpv;
      if(name != null && name.length() > 0) this.name = name;
      else this.name = null;
   }
   
   public static Instrument makeFuture(String symbol, BigDecimal tick, BigDecimal bpv) {
      return new Instrument(Type.FUTURE, symbol, tick, bpv, "");
   }
   
   public static Instrument makeFuture(String symbol) {
      return new Instrument(Type.FUTURE, symbol, BigDecimal.ZERO, BigDecimal.ZERO, "");
   }
   
   public static Instrument makeFuture(String symbol, BigDecimal tick, BigDecimal bpv, String name) {
      return new Instrument(Type.FUTURE, symbol, tick, bpv, name);
   }
   
   public static Instrument makeStock(String symbol) {
      return new Instrument(Type.STOCK, symbol, BigDecimal.valueOf(0.01), BigDecimal.valueOf(1.0), "");
   }
   
   public static Instrument makeIndex(String symbol) {
      return new Instrument(Type.INDEX, symbol, BigDecimal.valueOf(0.01), BigDecimal.valueOf(1.0), "");
   }
   
   public double getTick() { return tick.doubleValue(); }
   public BigDecimal getTickPrecise() { return tick; }
   
   public double getBpv() { return bpv.doubleValue(); }
   public BigDecimal getBpvPrecise() { return bpv; }
   
   public String getSymbol() { return symbol; }
   
   public String getName() { return name; }
   public void setName(String s) { this.name = s; }
   
   public Type getType() { return type; }
   public boolean isFuture() { return getType() == Type.FUTURE; }
   public boolean isStock() { return getType() == Type.STOCK; }
   public boolean isIndex() { return getType() == Type.INDEX; }
   
   public double tickCeil(double value) { return Math.ceil(value/getTick())*getTick(); }
   public double tickFloor(double value) { return Math.floor(value/getTick())*getTick(); }
}
