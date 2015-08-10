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
