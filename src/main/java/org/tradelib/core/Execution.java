package org.tradelib.core;

import java.time.LocalDateTime;

public class Execution {
   private Instrument instrument;
   private LocalDateTime ts;
   private double price;
   private long quantity;
   private String signal;
   private double fees;
   
   public Execution(Instrument instrument, LocalDateTime ts, double price, long qq) {
      this.instrument = instrument;
      this.ts = ts;
      this.price = price;
      this.quantity = qq;
      this.fees = 0.0;
   }
   
   public Execution(Instrument instrument, LocalDateTime ts, double price, long qq, double fees) {
      this.instrument = instrument;
      this.ts = ts;
      this.price = price;
      this.quantity = qq;
      this.fees = fees;
   }
   
   public Execution(Instrument instrument, LocalDateTime ts, double price, long qq, String sig) {
      this.instrument = instrument;
      this.ts = ts;
      this.price = price;
      this.quantity = qq;
      this.signal = sig;
      this.fees = 0.0;
   }
   
   public Execution(Instrument instrument, LocalDateTime ts, double price, long qq, double fees, String sig) {
      this.instrument = instrument;
      this.ts = ts;
      this.price = price;
      this.quantity = qq;
      this.signal = sig;
      this.fees = fees;
   }
   
   public String getSymbol() { return instrument.getSymbol(); }
   
   public Instrument getInstrument() { return instrument; }
   public void setInstrument(Instrument i) { this.instrument = i; }
   
   public LocalDateTime getDateTime() { return ts; }
   public void setDateTime(LocalDateTime ts) { this.ts = ts; }
   
   public double getPrice() { return price; }
   public void setPrice(double price) { this.price = price; }
   
   public long getQuantity() { return quantity; }
   public void setQuantity(long quantity) { this.quantity = quantity; }
   
   public String getSignal() { return signal; }
   public void setSignal(String signal) { this.signal = signal; }
   
   public double getFees() { return fees; }
   public void setFees(double fees) { this.fees = fees; }
}
