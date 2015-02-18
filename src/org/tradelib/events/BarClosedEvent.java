package org.tradelib.events;

import org.tradelib.core.Bar;

/**
 * Generated when a bar is closed and is available.
 */
public class BarClosedEvent {
   public final Bar bar;
   public BarClosedEvent(Bar b) {
      bar = b;
   }
}
