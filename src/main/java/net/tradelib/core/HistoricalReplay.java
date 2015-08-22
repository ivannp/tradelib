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

package net.tradelib.core;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

public class HistoricalReplay implements IBroker, IBarListener {
   
   private static final Logger logger = Logger.getLogger(HistoricalReplay.class.getName());
   
   private HashMap<String,InstrumentCB> instrumentCBMap;
   private HistoricalDataFeed dataFeed = null;

   public HistoricalDataFeed getDataFeed() {
      return dataFeed;
   }

   public void setDataFeed(HistoricalDataFeed dataFeed) {
      this.dataFeed = dataFeed;
      this.dataFeed.addBarListener(this);
   }

   private Portfolio portfolio = null;
   
   private LocalDateTime lastBarTimestamp = null;
   // The bars belonging to this period (a day)
   private List<Bar> periodsBars = new ArrayList<Bar>();

   // The order notifications
   private List<OrderNotification> orderNotifications = new ArrayList<OrderNotification>();
   
   // The executions
   private List<Execution> executions = new ArrayList<Execution>();
   
   protected IBrokerListener handler;
   
   public HistoricalReplay() {
      instrumentCBMap = new HashMap<String, InstrumentCB>();
      portfolio = new Portfolio("default");
   }
   
   public HistoricalReplay(Context context) {
      this();
      
      assert context.historicalDataFeed != null;
      setDataFeed(context.historicalDataFeed);
   }
   
   public void start() throws Exception {
      if(dataFeed != null) dataFeed.start();
      
      // Process the final set of bars
      processPeriodBars();
   }
   
   public void subscribe(String symbol) throws Exception {
      if(dataFeed != null) dataFeed.subscribe(symbol);
   }
   
   public void unsubscribe(String symbol) throws Exception {
      if(dataFeed != null) dataFeed.unsubscribe(symbol);
   }
   
   public void submitOrder(Order order) throws Exception {
      getInstrumentCB(order.getSymbol()).newOrders.add(order);
   }
   
   public void reset() throws Exception {
      // Remove all per instrument runtime data
      instrumentCBMap.clear();
      
      // Reset the data feed
      dataFeed.reset();
   }
   
   public Portfolio getPortfolio(String name) {
      return portfolio;
   }
   
   @Override
   public Instrument getInstrument(String symbol) throws Exception {
      return dataFeed.getInstrument(symbol);
   }

   @Override
   public InstrumentVariation getInstrumentVariation(String provider, String symbol) throws Exception {
      return dataFeed.getInstrumentVariation(provider, symbol);
   }

   @Override
   public Position getPosition(Instrument instrument) throws Exception {
      return getInstrumentCB(instrument.getSymbol()).position;
   }

   private class InstrumentCB {
      // The instrument
      public Instrument instrument;
      // The position
      Position position;
      // The orders
      List<Order> orders;
      // The new orders. All orders are registered into this list first. Later
      // on they are moved to the "orders" list.
      List<Order> newOrders;
      
      InstrumentCB(Instrument i) {
         instrument = i;
         position = new Position();
         orders = new ArrayList<Order>();
         newOrders = new ArrayList<Order>();
      }
   }
   
   private InstrumentCB getInstrumentCB(String symbol) throws Exception {
      InstrumentCB icb = instrumentCBMap.get(symbol);
      if(icb == null) {
         icb = new InstrumentCB(dataFeed.getInstrument(symbol));
         instrumentCBMap.put(symbol, icb);
      }
      return icb;
   }
   
   private void addNewOrders(InstrumentCB icb) {
      icb.orders.addAll(icb.newOrders);
      icb.newOrders.clear();
   }
   
   private void processOrders(InstrumentCB icb, Tick tick, boolean executeOnLimitOrStop) {
      // Scan all orders and check for a fill against the current tick. The execution
      // time must be different, hence we add a microsecond to the tick at each step.
      LocalDateTime ldt = tick.getDateTime();
      for(Order order : icb.orders) {
         long previousPosition = icb.position.quantity;
         OrderFill of = order.tryFill(tick, previousPosition, executeOnLimitOrStop);
         if(of != null) {
            // We have an execution, bump up the timestamp
            ldt = ldt.plusNanos(1000);

            // Currently we only support single-entry and single-exit positions.
            if(previousPosition != 0 && of.getPosition() != 0) {
               if(previousPosition > of.getPosition()) {
                  throw new UnsupportedOperationException(
                     "Order partial position close at " +
                     tick.getDateTime().format(DateTimeFormatter.ISO_INSTANT) + ": " +
                     String.valueOf(previousPosition) + " -> " + String.valueOf(of.getPosition()));
               }
               else
               {
                  throw new UnsupportedOperationException(
                        "Order consecutive position opening at " +
                        tick.getDateTime().format(DateTimeFormatter.ISO_INSTANT) + ": " +
                        String.valueOf(previousPosition) + " -> " + String.valueOf(of.getPosition()));
               }
            }

            // Update the position
            icb.position = new Position(of.getPosition(), ldt);

            // Check whether we are closing a position. If so, any other exit order are cancelled.
            boolean removeExits = (previousPosition > 0 && of.getPosition() <= 0) ||
                                  (previousPosition < 0 && of.getPosition() >= 0);

            // Cancel orders if necessary
            if(order.isOca()) {
               for(Order oo : icb.orders) {
                  // Skip the current order
                  if(oo == order) continue;

                  // Cancel active, which are either exit or oca
                  if(oo.isActive()) {
                     if(oo.isExit() || oo.isOca() == order.isOca()) {
                        oo.cancel();
                     }
                  }
               }
            } else if (removeExits) {
               for(Order oo : icb.orders) {
                  // Skip the current order
                  if(oo == order) continue;
                  
                  // Cancel active, exit orders
                  if(oo.isExit() && oo.isActive()) {
                     oo.cancel();
                  }
               }
            }
            
//            logger_.info("appending transaction: " + icb.instrument.getSymbol() +
//                  ": " + tick.getDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + 
//                  ": " + ldt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) +
//                  ": " + Long.toString(previousPosition) +
//                  ", " + Long.toString(of.getPosition()) +
//                  ": " + Long.toString(of.getTransactionQuantity()) +
//                  ", " + Long.toString(of.getFilledQuantity()) +
//                  ": " + of.getFillPrice().toString());

            // Mark the current order as filled
            order.fill();
            // Add a transaction to the portfolio
            portfolio.addTransaction(icb.instrument, ldt, of.getTransactionQuantity(), of.getFillPrice(), 0.0);
            // Add an execution
            executions.add(new Execution(icb.instrument, ldt, of.getFillPrice(), of.getTransactionQuantity(), order.getSignal()));
            // Add a notification (posted after the order processing loop finishes)
            orderNotifications.add(new OrderNotification(order, executions.get(executions.size()-1)));
         }
      }
   }
   
   private void postOrderNotifications() throws Exception {
      for(OrderNotification on : orderNotifications) {
         if(handler != null) {
            handler.orderExecutedHandler(on);
         }
      }
      orderNotifications.clear();
   }
   
   private void cleanupOrders(InstrumentCB icb, Bar bar) {
      // While improving performance by removing inactive orders
      // from the list, we lose the order history.
      Iterator<Order> it = icb.orders.iterator();
      while(it.hasNext()) {
         Order order = it.next();
         order.updateState(bar);
         if(!order.isActive()) it.remove();
      }
   }
   
   @Override
   public void barNotification(Bar bar) throws Exception {
      
      assert lastBarTimestamp == null || bar.getDateTime().compareTo(lastBarTimestamp) >= 0 : "The feed must deliver bars in chronological order.";
      
      // Received a bar. If its timestamp is different than the 
      // current period, we need to process all bars for the period.
      // Otherwise, the bar is simply added to the collection.
      if(lastBarTimestamp == null) {
         lastBarTimestamp = bar.getDateTime();
      } else if(bar.getDateTime().compareTo(lastBarTimestamp) != 0) {
         processPeriodBars();
      }
      
      periodsBars.add(bar);
   }

//   public void barNotification(Bar bar) throws Exception {
//      InstrumentCB icb = getInstrumentCB(bar.getSymbol());
//
//      // 1. All orders are eligible for execution at this point.
//      addNewOrders(icb);
//
//      // 2. Process orders at open. At the open the limit and stop orders
//      // are executed on the tick (using false for executeOnLimitOrStop).
//      LocalDateTime ldt = LocalDateTime.of(
//                              bar.getDateTime().getYear(),
//                              bar.getDateTime().getMonthValue(),
//                              bar.getDateTime().getDayOfMonth(),
//                              9, 0, 1);
//      processOrders(icb, new Tick(bar.getSymbol(), ldt, bar.getOpen()), false);
//
//      // 3. Send notifications for the executed trades
//      postOrderNotifications(icb);
//
//      // 4. Notify for the opening of the bar. We use a bar, not a Tick object,
//      // so that the callee can use (symbol, duration) to identify the bar set
//      // this bar belongs to. The callee may use only the open price from the bar.
//      Bar openBar = new Bar(bar);
//      openBar.setDateTime(ldt);
//      BigDecimal nan = BigDecimal.valueOf(Double.MIN_VALUE);
//      openBar.setHigh(nan); openBar.setLow(nan); openBar.setClose(nan);
//      openBar.setContractInterest(Long.MIN_VALUE);
//      openBar.setVolume(Long.MIN_VALUE);
//      openBar.setTotalInterest(Long.MIN_VALUE);
//      handler_.barOpenHandler(openBar);
//
//      // 5. Pick up any new orders submitted during steps 3. and 4.
//      addNewOrders(icb);
//
//      // 6. Process orders at high (assume at 11:00:01)
//      ldt = LocalDateTime.of(
//                  bar.getDateTime().getYear(),
//                  bar.getDateTime().getMonthValue(),
//                  bar.getDateTime().getDayOfMonth(),
//                  11, 0, 1);
//      processOrders(icb, new Tick(bar.getSymbol(), ldt, bar.getHigh()), true);
//
//      // No new orders are added here. Orders submitted during the *high*
//      // processing are not eligible for execution during the *low* processing.
//
//      // 7. Send notifications for the executed trades
//      postOrderNotifications(icb);
//
//      // 8. Process orders at low (assume at 13:00:01)
//      ldt = LocalDateTime.of(
//            bar.getDateTime().getYear(),
//            bar.getDateTime().getMonthValue(),
//            bar.getDateTime().getDayOfMonth(),
//            13, 0, 1);
//      processOrders(icb, new Tick(bar.getSymbol(), ldt, bar.getLow()), true);
//
//      // 9. Send notifications for the executed trades
//      postOrderNotifications(icb);
//
//      // 10. Publish the bar, but it's not closed yet - this is to accommodate trading
//      // where the signal is computed at the close and the trading takes place at the close.
//      ldt = LocalDateTime.of(
//            bar.getDateTime().getYear(),
//            bar.getDateTime().getMonthValue(),
//            bar.getDateTime().getDayOfMonth(),
//            15, 59, 59);
//      Bar closeBar = new Bar(bar);
//      closeBar.setDateTime(ldt);
//      handler_.barCloseHandler(closeBar);
//
//      // 11. Pick up any new orders submitted during the previous two steps. Everything
//      // is eligible to be processed at the close.
//      addNewOrders(icb);
//
//      // 12. Process orders at close
//      processOrders(icb, new Tick(bar.getSymbol(), ldt, bar.getClose()), false);
//
//      // 13. Send notifications for the executed trades
//      postOrderNotifications(icb);
//
//      // 14. The bar is closed
//      ldt = LocalDateTime.of(
//            bar.getDateTime().getYear(),
//            bar.getDateTime().getMonthValue(),
//            bar.getDateTime().getDayOfMonth(),
//            16, 0, 0);
//      closeBar.setDateTime(ldt);
//      handler_.barClosedHandler(closeBar);
//
//      // 15. Make all orders eligible
//      addNewOrders(icb);
//
//      // 16. It's not safe to cleanup the order vectors earlier, since notifications
//      // point straight into the order vector. So all order updates (expiration and/or
//      // removal from the list) had to be postponed til now.
//      cleanupOrders(icb, closeBar);
//   }
   
   protected void processPeriodBars() throws Exception {
      // Process orders at the open
      for(Bar bar : periodsBars) {
         InstrumentCB icb = getInstrumentCB(bar.getSymbol());
   
         // All orders are eligible for execution at this point.
         addNewOrders(icb);
   
         // Process orders at open. At the open the limit and stop orders
         // are executed on the tick (using false for executeOnLimitOrStop).
         LocalDateTime ldt = LocalDateTime.of(
                                 bar.getDateTime().getYear(),
                                 bar.getDateTime().getMonthValue(),
                                 bar.getDateTime().getDayOfMonth(),
                                 9, 0, 1);
         processOrders(icb, new Tick(bar.getSymbol(), ldt, bar.getOpen()), false);
      }
      
      // Send notifications order notifications
      postOrderNotifications();
      
      // Notify for the opening of the bar. We use a bar, not a Tick object,
      // so that the callee can use (symbol, duration) to identify the bar set
      // this bar belongs to. The callee may use only the open price from the bar.
      for(Bar bar : periodsBars) {
         LocalDateTime ldt = LocalDateTime.of(
               bar.getDateTime().getYear(),
               bar.getDateTime().getMonthValue(),
               bar.getDateTime().getDayOfMonth(),
               9, 0, 1);

         Bar openBar = new Bar(bar);
         openBar.setDateTime(ldt);
         openBar.setHigh(Double.NaN); openBar.setLow(Double.NaN); openBar.setClose(Double.NaN);
         openBar.setContractInterest(Long.MIN_VALUE);
         openBar.setVolume(Long.MIN_VALUE);
         openBar.setTotalInterest(Long.MIN_VALUE);
         if(handler != null) {
            handler.barOpenHandler(openBar);
         }
         
         // Pick up any new orders submitted during the previous steps.
         addNewOrders(getInstrumentCB(bar.getSymbol()));
      }
      
      // Process orders at low (assume at 11:00:01)
      for(Bar bar : periodsBars) {
         LocalDateTime ldt = LocalDateTime.of(
                                 bar.getDateTime().getYear(),
                                 bar.getDateTime().getMonthValue(),
                                 bar.getDateTime().getDayOfMonth(),
                                 11, 0, 1);
         processOrders(getInstrumentCB(bar.getSymbol()), new Tick(bar.getSymbol(), ldt, bar.getLow()), true);
      }

      // No new orders are added here. Orders submitted during the *high*
      // processing are not eligible for execution during the *low* processing.
      
      // Send notifications for the executed trades
      postOrderNotifications();

      // Process orders at high (assume at 13:00:01)
      for(Bar bar : periodsBars) {
         LocalDateTime ldt = LocalDateTime.of(
                                 bar.getDateTime().getYear(),
                                 bar.getDateTime().getMonthValue(),
                                 bar.getDateTime().getDayOfMonth(),
                                 13, 0, 1);
         processOrders(getInstrumentCB(bar.getSymbol()), new Tick(bar.getSymbol(), ldt, bar.getHigh()), true);
      }
      
   
      // Publish the bar, but it's not closed yet - this is to accommodate
      // trading where the signal is computed at the close and the trading
      // takes place at the close.
      for(Bar bar : periodsBars) {
         LocalDateTime ldt = LocalDateTime.of(
                                 bar.getDateTime().getYear(),
                                 bar.getDateTime().getMonthValue(),
                                 bar.getDateTime().getDayOfMonth(),
                                 15, 59, 59);
         Bar closeBar = new Bar(bar);
         closeBar.setDateTime(ldt);
         if(handler != null) {
            handler.barCloseHandler(closeBar);
         }
   
         // Pick up any new orders submitted during the previous two steps.
         // Everything is eligible to be processed at the close.
         addNewOrders(getInstrumentCB(bar.getSymbol()));
      }
   
      for(Bar bar : periodsBars) {
         // Process orders at close
         LocalDateTime ldt = LocalDateTime.of(
               bar.getDateTime().getYear(),
               bar.getDateTime().getMonthValue(),
               bar.getDateTime().getDayOfMonth(),
               15, 59, 59);
         processOrders(getInstrumentCB(bar.getSymbol()), new Tick(bar.getSymbol(), ldt, bar.getClose()), false);
      }
   
      // Send notifications for the executed trades
      postOrderNotifications();
   
      // The bar is closed
      for(Bar bar : periodsBars) {
         LocalDateTime ldt = LocalDateTime.of(
               bar.getDateTime().getYear(),
               bar.getDateTime().getMonthValue(),
               bar.getDateTime().getDayOfMonth(),
               16, 0, 0);
         Bar closeBar = new Bar(bar);
         closeBar.setDateTime(ldt);
         if(handler != null) {
            handler.barClosedHandler(closeBar);
         }
   
         // Make all orders eligible
         InstrumentCB icb = getInstrumentCB(bar.getSymbol()); 
         addNewOrders(icb);
         
         // It's not safe to cleanup the order vectors earlier, since notifications
         // point straight into the order vector. So all order updates (expiration
         // and/or removal from the list) had to be postponed till now.
         cleanupOrders(icb, closeBar);
      }
      
      // Clear the bar list
      periodsBars.clear();
   }

   @Override
   public void addBrokerListener(IBrokerListener listener) throws Exception {
      handler = listener;
   }

   @Override
   public void cancelAllOrders() throws Exception {
      for(InstrumentCB icb : instrumentCBMap.values()) {
         for(Order oo : icb.orders) {
            if(!oo.isCancelled()) oo.cancel();
         }
      }
   }

   @Override
   public void cancelAllOrders(String symbol) throws Exception {
      InstrumentCB icb = getInstrumentCB(symbol);
      if(icb == null) return;
      for(Order oo : icb.orders) {
         if(!oo.isCancelled()) oo.cancel();
      }
   }
}
