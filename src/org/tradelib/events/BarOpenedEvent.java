package org.tradelib.events;

import org.tradelib.core.Bar;

public class BarOpenedEvent {
   public final Bar bar;
   public BarOpenedEvent(Bar b) {
      bar = b;
   }
}
