// Copyright 2015 Ivan Popivanov
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package net.tradelib.core;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Objects;

public class Instrument {
   public enum Type {
      NONE, FUTURE, INDEX, STOCK, FOREX
   }

   private final Type type;
   private final String symbol;
   private final BigDecimal tick;
   private final BigDecimal bpv;
   private String name;
   private final Currency currency;
   
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
      this(type, symbol, tick, bpv, name, Currency.getInstance("USD"));
   }
   
   private Instrument(Type type, String symbol, BigDecimal tick, BigDecimal bpv, String name, String currency) {
      this(type, symbol, tick, bpv, name, Currency.getInstance(currency));
   }
   
   private Instrument(Type type, String symbol, BigDecimal tick, BigDecimal bpv, String name, Currency currency) {
      this.type = type; this.symbol = symbol; this.tick = tick; this.bpv = bpv;
      if(name != null && name.length() > 0) this.name = name;
      else this.name = null;
      this.currency = currency;
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
   
   public static Instrument makeForex(String symbol) {
      return new Instrument(Type.FOREX, symbol, BigDecimal.valueOf(0.0001), BigDecimal.valueOf(1.0), "");
   }
   
   public static Instrument makeForex(String symbol, String currency) {
      return new Instrument(Type.FOREX, symbol, BigDecimal.valueOf(0.0001), BigDecimal.valueOf(1.0), "", currency);
   }
   
   public static Instrument makeForex(String symbol, BigDecimal tick, String currency) {
      return new Instrument(Type.FOREX, symbol, tick, BigDecimal.valueOf(1.0), "", currency);
   }
   
   public static Instrument makeForex(String symbol, BigDecimal tick) {
      return new Instrument(Type.FOREX, symbol, tick, BigDecimal.valueOf(1.0), "");
   }
   
   public double getTick() { return tick.doubleValue(); }
   public BigDecimal getTickPrecise() { return tick; }
   
   public double getBpv() { return bpv.doubleValue(); }
   public BigDecimal getBpvPrecise() { return bpv; }
   
   public String getSymbol() { return symbol; }
   
   public String getName() { return name; }
   public void setName(String s) { this.name = s; }

   public Currency getCurrency() { return currency; }
   public Currency getCounterCurrency() { return getCurrency(); }
   public Currency getQuoteCurrenty() { return getCurrency(); }
   
   public Currency getBaseCurrency() {
      if(!isForex() || name.length() != 6) return null;
      return Currency.getInstance(name.substring(0, 3));
   }
   
   public Type getType() { return type; }
   public boolean isFuture() { return getType() == Type.FUTURE; }
   public boolean isStock() { return getType() == Type.STOCK; }
   public boolean isIndex() { return getType() == Type.INDEX; }
   public boolean isForex() { return getType() == Type.FOREX; }
   
   public double tickCeil(double value) { return Math.ceil(value/getTick())*getTick(); }
   public double tickFloor(double value) { return Math.floor(value/getTick())*getTick(); }
}
