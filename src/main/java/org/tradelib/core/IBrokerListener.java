package org.tradelib.core;

public interface IBrokerListener {
   public void barOpenHandler(Bar bar) throws Exception;
   public void barCloseHandler(Bar bar) throws Exception;
   public void barClosedHandler(Bar bar) throws Exception;
   
   public void orderExecutedHandler(OrderNotification on) throws Exception;
}
