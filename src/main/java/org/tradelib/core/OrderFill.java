package org.tradelib.core;

import java.math.BigDecimal;

public class OrderFill {
   private double fillPrice;
   private long filledQuantity;
   private long transactionQuantity;
   private long newPosition;
   
   public OrderFill(double price, long filledQuantity, long transactionQuantity, long newPosition) {
      this.fillPrice = price;
      this.filledQuantity = filledQuantity;
      this.transactionQuantity = transactionQuantity;
      this.newPosition = newPosition;
   }

   public double getFillPrice() { return fillPrice; }
   public long getFilledQuantity() { return filledQuantity; }
   public long getTransactionQuantity() { return transactionQuantity; }
   public long getPosition() { return newPosition; }
}
