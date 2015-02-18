package org.tradelib.core;

public class OrderNotification {
   public Order order;
   public Execution execution;
   
   public OrderNotification() {
      order = null;
      execution = null;
   }
   
   public OrderNotification(Order o, Execution e) {
      order = o;
      execution = e;
   }
}