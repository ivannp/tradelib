package org.tradelib.core;

public interface IBroker {
   public void start() throws Exception;
   
   public void subscribe(String symbol) throws Exception;
   public void unsubscribe(String symbol) throws Exception;
   
   public void submitOrder(Order order) throws Exception;
   
   // Resets all runtime data, but leaves the configuration
   public void reset() throws Exception;
   
   public void addBrokerListener(IBrokerListener listener) throws Exception;
   
   public Instrument getInstrument(String symbol) throws Exception;
   public InstrumentVariation getInstrumentVariation(String provider, String symbol) throws Exception;
   public Position getPosition(Instrument instrument) throws Exception;
}
