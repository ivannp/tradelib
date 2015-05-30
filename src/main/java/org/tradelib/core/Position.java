package org.tradelib.core;

import java.time.LocalDateTime;

public class Position {
   public long quantity;
   public LocalDateTime since;
   
   public Position(long qq, LocalDateTime ldt) {
      quantity = qq;
      since = ldt;
   }
   
   public Position() {
      quantity = 0;
      since = LocalDateTime.MIN;
   }
}
