package org.tradelib.events;

import org.tradelib.core.Bar;

/**
 * Generated when the a bar is closing. The bar is available,
 * yet, orders submitted within this event are going to be
 * considered for execution at the "close" of the bar.
 */
public class BarCloseEvent {
   public final Bar bar;
   public BarCloseEvent(Bar b) {
      bar = b;
   }
}
