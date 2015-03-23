package org.tradelib.core;

import java.time.LocalDateTime;

import com.google.gson.JsonObject;

public class Order {
   // Use the position quantity when processing this order
   public static final long POSITION_QUANTITY = -1;
   
   private String symbol;
   private long quantity;
   private double limitPrice;
   private double stopPrice;
   private String signal;
   // One Cancels All identifier
   private String oca;
   private Type type;
   private State state;
   private int barsValidFor;
   private LocalDateTime lastBar;
   
   private enum Type {
      ENTER_LONG, ENTER_LONG_STOP, ENTER_LONG_LIMIT, ENTER_LONG_STOP_LIMIT,
      EXIT_LONG, EXIT_LONG_STOP, EXIT_LONG_LIMIT, EXIT_LONG_STOP_LIMIT,
      ENTER_SHORT, ENTER_SHORT_STOP, ENTER_SHORT_LIMIT, ENTER_SHORT_STOP_LIMIT,
      EXIT_SHORT, EXIT_SHORT_STOP, EXIT_SHORT_LIMIT, EXIT_SHORT_STOP_LIMIT
   }
   
   private enum State {
      ACTIVE, CANCELLED, FILLED
   }
   
   private boolean stopWasTriggered_;
   
   public static Order enterLong(String s, long q) { return new Order(s, q, Double.NaN, Double.NaN, Type.ENTER_LONG); }
   public static Order enterLong(String s, long q, String sig) { return new Order(s, q, Double.NaN, Double.NaN, Type.ENTER_LONG, sig); }
   public static Order enterLongLimit(String s, long q, double lp) { return new Order(s, q, lp, Double.NaN, Type.ENTER_LONG_LIMIT); }
   public static Order enterLongLimit(String s, long q, double lp, String sig) { return new Order(s, q, lp, Double.NaN, Type.ENTER_LONG_LIMIT, sig); }
   public static Order enterLongStop(String s, long q, double sp) { return new Order(s, q, Double.NaN, sp, Type.ENTER_LONG_STOP); }
   public static Order enterLongStop(String s, long q, double sp, String sig) { return new Order(s, q, Double.NaN, sp, Type.ENTER_LONG_STOP, sig); }
   public static Order enterLongStopLimit(String s, long q, double sp, double lp) { return new Order(s, q, lp, sp, Type.ENTER_LONG_STOP_LIMIT); }
   public static Order enterLongStopLimit(String s, long q, double sp, double lp, String sig) { return new Order(s, q, lp, sp, Type.ENTER_LONG_STOP_LIMIT, sig); }
   
   public static Order enterShort(String s, long q) { return new Order(s, q, Double.NaN, Double.NaN, Type.ENTER_SHORT); }
   public static Order enterShort(String s, long q, String sig) { return new Order(s, q, Double.NaN, Double.NaN, Type.ENTER_SHORT, sig); }
   public static Order enterShortLimit(String s, long q, double lp) { return new Order(s, q, lp, Double.NaN, Type.ENTER_SHORT_LIMIT); }
   public static Order enterShortLimit(String s, long q, double lp, String sig) { return new Order(s, q, lp, Double.NaN, Type.ENTER_SHORT_LIMIT, sig); }
   public static Order enterShortStop(String s, long q, double sp) { return new Order(s, q, Double.NaN, sp, Type.ENTER_SHORT_STOP); }
   public static Order enterShortStop(String s, long q, double sp, String sig) { return new Order(s, q, Double.NaN, sp, Type.ENTER_SHORT_STOP, sig); }
   public static Order enterShortStopLimit(String s, long q, double sp, double lp) { return new Order(s, q, lp, sp, Type.ENTER_SHORT_STOP_LIMIT); }
   public static Order enterShortStopLimit(String s, long q, double sp, double lp, String sig) { return new Order(s, q, lp, sp, Type.ENTER_SHORT_STOP_LIMIT, sig); }

   public static Order exitLong(String s, long q) { return new Order(s, q, Double.NaN, Double.NaN, Type.EXIT_LONG); }
   public static Order exitLong(String s, long q, String sig) { return new Order(s, q, Double.NaN, Double.NaN, Type.EXIT_LONG, sig); }
   public static Order exitLongLimit(String s, long q, double lp) { return new Order(s, q, lp, Double.NaN, Type.EXIT_LONG_LIMIT); }
   public static Order exitLongLimit(String s, long q, double lp, String sig) { return new Order(s, q, lp, Double.NaN, Type.EXIT_LONG_LIMIT, sig); }
   public static Order exitLongStop(String s, long q, double sp) { return new Order(s, q, Double.NaN, sp, Type.EXIT_LONG_STOP); }
   public static Order exitLongStop(String s, long q, double sp, String sig) { return new Order(s, q, Double.NaN, sp, Type.EXIT_LONG_STOP, sig); }
   public static Order exitLongStopLimit(String s, long q, double sp, double lp) { return new Order(s, q, lp, sp, Type.EXIT_LONG_STOP_LIMIT); }
   public static Order exitLongStopLimit(String s, long q, double sp, double lp, String sig) { return new Order(s, q, lp, sp, Type.EXIT_LONG_STOP_LIMIT, sig); }

   public static Order exitShort(String s, long q) { return new Order(s, q, Double.NaN, Double.NaN, Type.EXIT_SHORT); }
   public static Order exitShort(String s, long q, String sig) { return new Order(s, q, Double.NaN, Double.NaN, Type.EXIT_SHORT, sig); }
   public static Order exitShortLimit(String s, long q, double lp) { return new Order(s, q, lp, Double.NaN, Type.EXIT_SHORT_LIMIT); }
   public static Order exitShortLimit(String s, long q, double lp, String sig) { return new Order(s, q, lp, Double.NaN, Type.EXIT_SHORT_LIMIT, sig); }
   public static Order exitShortStop(String s, long q, double sp) { return new Order(s, q, Double.NaN, sp, Type.EXIT_SHORT_STOP); }
   public static Order exitShortStop(String s, long q, double sp, String sig) { return new Order(s, q, Double.NaN, sp, Type.EXIT_SHORT_STOP, sig); }
   public static Order exitShortStopLimit(String s, long q, double sp, double lp) { return new Order(s, q, lp, sp, Type.EXIT_SHORT_STOP_LIMIT); }
   public static Order exitShortStopLimit(String s, long q, double sp, double lp, String sig) { return new Order(s, q, lp, sp, Type.EXIT_SHORT_STOP_LIMIT, sig); }
   
   public Order(String ss, long qq, double lp, double sp, Type tt) {
      this.symbol = ss;
      this.quantity = qq;
      this.limitPrice = lp; this.stopPrice = sp;
      this.type = tt;
      
      this.oca = "";
      
      this.state = State.ACTIVE;
      this.stopWasTriggered_ = false;
      
      this.barsValidFor = -1;
   }
   
   public Order(String ss, long qq, double lp, double sp, Type tt, String sig) {
      this.symbol = ss;
      this.quantity = qq;
      this.limitPrice = lp; stopPrice = sp;
      this.type = tt;
      
      this.oca = "";
      
      this.signal = sig;
      
      this.state = State.ACTIVE;
      this.stopWasTriggered_ = false;
      
      this.barsValidFor = -1;
   }
   
   public String getSymbol() { return symbol; }
   public void setSymbol(String symbol) { this.symbol = symbol; }
   
   public double getLimit() { return limitPrice; }
   public void setLimit(double price) { this.limitPrice = price; }
   
   public double getStop() { return stopPrice; }
   public void setStop(double price) { this.stopPrice = price; }
   
   public long getQuantity() { return quantity; }
   public void setQuantity(long quantity) { this.quantity = quantity; }
   
   public String getSignal() { return signal; }
   public void setSignal(String signal) { this.signal = signal; }
   
   public void activate() { state = State.ACTIVE; }
   public void fill() { state = State.FILLED; }
   public void cancel() { state = State.CANCELLED; }

   public boolean isActive() { return state == State.ACTIVE; }
   public boolean isFilled() { return state == State.FILLED; }
   public boolean isCancelled() { return state == State.CANCELLED; }

   public boolean isStopped() { return stopWasTriggered_; }
   public void makeStopped() { stopWasTriggered_ = true; }

   public boolean isBuy() {
      switch(type) {
      case ENTER_LONG:
      case ENTER_LONG_LIMIT:
      case ENTER_LONG_STOP:
      case ENTER_LONG_STOP_LIMIT:
      case EXIT_SHORT:
      case EXIT_SHORT_LIMIT:
      case EXIT_SHORT_STOP:
      case EXIT_SHORT_STOP_LIMIT:
         return true;
      default:
         return false;
      }
   }

   public boolean isLongEntry() {
      switch(type) {
      case ENTER_LONG:
      case ENTER_LONG_LIMIT:
      case ENTER_LONG_STOP:
      case ENTER_LONG_STOP_LIMIT:
         return true;
      default:
         return false;
      }
   }
   
   public boolean isLongExit() {
      switch(type) {
      case EXIT_LONG:
      case EXIT_LONG_LIMIT:
      case EXIT_LONG_STOP:
      case EXIT_LONG_STOP_LIMIT:
         return true;
      default:
         return false;
      }
   }
   
   public boolean isShortEntry() {
      switch(type) {
      case ENTER_SHORT:
      case ENTER_SHORT_LIMIT:
      case ENTER_SHORT_STOP:
      case ENTER_SHORT_STOP_LIMIT:
         return true;
      default:
         return false;
      }
   }
   
   public boolean isShortExit() {
      switch(type) {
      case EXIT_SHORT:
      case EXIT_SHORT_LIMIT:
      case EXIT_SHORT_STOP:
      case EXIT_SHORT_STOP_LIMIT:
         return true;
      default:
         return false;
      }
   }

   public boolean isSell() { return !isBuy(); }

   public boolean isEntry() { return isLongEntry() || isShortEntry(); }
   public boolean isExit() { return isLongExit() || isShortExit(); }

   // Is this a One-Cancels-All order
   public boolean isOca() { return !oca.isEmpty(); }
   
   public void updateState(Bar bar) {
      // Check whether the order requires processing (bar expiration is set and is active)
      if(!isActive() || barsValidFor < 0) return;

      assert barsValidFor > 0 || isCancelled();
      if(bar.getDateTime() != lastBar) {
         --barsValidFor;
         if(barsValidFor == 0) {
            cancel();
         } else {
            lastBar = bar.getDateTime();
         }
      }
   }

   /** 
    * @brief Make the order valid for numBars including the bar on which it is submitted.
    *
    * To account for the bar on which the order is submitted (and to be independent of the
    * current bar), "lastBar_" is initialized to LocalDateTime.MIN. Thus, at the end of that
    * bar, the code below will perform the first decrement and consider for canceling.
    *  
    * @param numBars The number of bars this order is valid for
    */
   public void setExpiration(int numBars) {
      barsValidFor = numBars;
      lastBar = LocalDateTime.MIN;
   }
   
   private long computeFilledQuantity(long position) {
      long result = Long.MIN_VALUE;
      assert getQuantity() > 0 || getQuantity() == POSITION_QUANTITY;
      if(getQuantity() > 0) {
         result = Math.min(getQuantity(), Math.abs(position));
      } else if(getQuantity() == POSITION_QUANTITY) {
         result = Math.abs(position);
      }
      return result;
   }
   
   public OrderFill tryFill(Tick tick, long position, boolean executeOnLimitOrStop)
   {
      long filledQuantity = Long.MIN_VALUE;
      OrderFill orderFill = null;
      
      // Only active orders are filled
      if(isActive()) {
         switch(type) {
         // Market orders
         case ENTER_LONG:
            if(position == 0) {
               orderFill = new OrderFill(tick.getPrice(), getQuantity(), getQuantity(), getQuantity());
            }
            break;
   
         case ENTER_SHORT:
            if(position == 0) {
               orderFill = new OrderFill(tick.getPrice(), getQuantity(), -getQuantity(), -getQuantity());
            }
            break;
   
         case EXIT_LONG:
            if(position > 0) {
               filledQuantity = computeFilledQuantity(position);
               orderFill = new OrderFill(tick.getPrice(), filledQuantity, -filledQuantity, 0);
            }
            break;
   
         case EXIT_SHORT:
            filledQuantity = computeFilledQuantity(position);
            orderFill = new OrderFill(tick.getPrice(), filledQuantity, filledQuantity, 0);
            break;
   
            // limit orders
         case ENTER_LONG_LIMIT:
            if(position == 0) {
               if(tick.getPrice() <= limitPrice) {
                  double fillPrice = executeOnLimitOrStop ? limitPrice : tick.getPrice();
                  orderFill = new OrderFill(fillPrice, getQuantity(), getQuantity(), getQuantity());
               }
            }
            break;
   
         case EXIT_SHORT_LIMIT:
            if(position < 0) {
               if(tick.getPrice() <= limitPrice) {
                  double fillPrice = executeOnLimitOrStop ? limitPrice : tick.getPrice();
                  filledQuantity = computeFilledQuantity(position);
                  orderFill = new OrderFill(fillPrice, filledQuantity, filledQuantity, 0);
               }
            }
            break;
   
         // Limit orders
         case ENTER_SHORT_LIMIT:
            if(position == 0) {
               if(limitPrice <= tick.getPrice()) {
                  double fillPrice = executeOnLimitOrStop ? limitPrice : tick.getPrice();
                  orderFill = new OrderFill(fillPrice, getQuantity(), -getQuantity(), -getQuantity());
               }
            }
            break;
   
         case EXIT_LONG_LIMIT:
            if(position > 0) {
               if(limitPrice <= tick.getPrice()) {
                  double fillPrice = executeOnLimitOrStop ? limitPrice : tick.getPrice();
                  filledQuantity = computeFilledQuantity(position);
                  orderFill = new OrderFill(fillPrice, filledQuantity, -filledQuantity, 0);
               }
            }
            break;
   
         // Stop orders
         case ENTER_LONG_STOP:
            if(position == 0) {
               if(stopPrice <= tick.getPrice()) {
                  double fillPrice = executeOnLimitOrStop ? stopPrice : tick.getPrice();
                  orderFill = new OrderFill(fillPrice, getQuantity(), getQuantity(), getQuantity());
               }
            }
            break;
   
         case EXIT_SHORT_STOP:
            if(position < 0) {
               if(stopPrice <= tick.getPrice()) {
                  double fillPrice = executeOnLimitOrStop ? stopPrice : tick.getPrice();
                  filledQuantity = computeFilledQuantity(position);
                  orderFill = new OrderFill(fillPrice, filledQuantity, filledQuantity, 0);
               }
            }
            break;
   
         case EXIT_LONG_STOP:
            if(position > 0) {
               if(stopPrice >= tick.getPrice()) {
                  double fillPrice = executeOnLimitOrStop ? stopPrice : tick.getPrice();
                  filledQuantity = computeFilledQuantity(position);
                  orderFill = new OrderFill(fillPrice, filledQuantity, -filledQuantity, 0);
               }
            }
            break;
   
         case ENTER_SHORT_STOP:
            if(position == 0) {
               if(stopPrice >= tick.getPrice()) {
                  double fillPrice = executeOnLimitOrStop ? stopPrice : tick.getPrice();
                  orderFill = new OrderFill(fillPrice, getQuantity(), -getQuantity(), -getQuantity());
               }
            }
            break;
   
         case ENTER_LONG_STOP_LIMIT:
            if(position == 0) {
               if(isStopped()) {
                  if(limitPrice >= tick.getPrice()) {
                     double fillPrice = executeOnLimitOrStop ? limitPrice : tick.getPrice();
                     orderFill = new OrderFill(fillPrice, getQuantity(), getQuantity(), getQuantity());
                  }
               } else if(stopPrice <= tick.getPrice()) {
                  if((limitPrice >= tick.getPrice()) || (executeOnLimitOrStop && stopPrice <= limitPrice)) {
                     double fillPrice = executeOnLimitOrStop ? stopPrice : tick.getPrice();
                     orderFill = new OrderFill(fillPrice, getQuantity(), getQuantity(), getQuantity());
                  } else {
                     makeStopped();
                  }
               }
            }
            break;
   
         case EXIT_LONG_STOP_LIMIT:
            if(isStopped()) {
               if(limitPrice <= tick.getPrice()) {
                  double fillPrice = executeOnLimitOrStop ? limitPrice : tick.getPrice();
                  filledQuantity = computeFilledQuantity(position);
                  orderFill = new OrderFill(fillPrice, filledQuantity, -filledQuantity, 0);
               }
            }
            else if(stopPrice >= tick.getPrice()) {
               if((limitPrice <= tick.getPrice()) || (executeOnLimitOrStop && limitPrice <= stopPrice)) {
                  double fillPrice = executeOnLimitOrStop ? stopPrice : tick.getPrice();
                  filledQuantity = computeFilledQuantity(position);
                  orderFill = new OrderFill(fillPrice, filledQuantity, -filledQuantity, 0);
               } else {
                  makeStopped();
               }
            }
            break;
   
         case ENTER_SHORT_STOP_LIMIT:
            if(position == 0) {
               if(isStopped()) {
                  if(limitPrice <= tick.getPrice()) {
                     double fillPrice = executeOnLimitOrStop ? limitPrice : tick.getPrice();
                     orderFill = new OrderFill(fillPrice, getQuantity(), -getQuantity(), -getQuantity());
                  }
               } else if(stopPrice >= tick.getPrice()) {
                  if((limitPrice <= tick.getPrice()) || (executeOnLimitOrStop && stopPrice >= limitPrice)) {
                     double fillPrice = executeOnLimitOrStop ? stopPrice : tick.getPrice();
                     orderFill = new OrderFill(fillPrice, getQuantity(), -getQuantity(), -getQuantity());
                  } else {
                     makeStopped();
                  }
               }
            }
            break;
   
         case EXIT_SHORT_STOP_LIMIT:
            if(position < 0) {
               if(isStopped()) {
                  if(limitPrice >= tick.getPrice()) {
                     double fillPrice = executeOnLimitOrStop ? limitPrice : tick.getPrice();
                     filledQuantity = computeFilledQuantity(position);
                     orderFill = new OrderFill(fillPrice, filledQuantity, -filledQuantity, 0);
                  }
               }
               else if(stopPrice <= tick.getPrice()) {
                  if((limitPrice >= tick.getPrice()) || (executeOnLimitOrStop && stopPrice <= limitPrice)) {
                     double fillPrice = executeOnLimitOrStop ? stopPrice : tick.getPrice();
                     filledQuantity = computeFilledQuantity(position);
                     orderFill = new OrderFill(fillPrice, filledQuantity, -filledQuantity, 0);
                  } else {
                     makeStopped();
                  }
               }
            }
            break;
         }
      }
   
      return orderFill;
   }
   
   public JsonObject toJsonString() {
      JsonObject jo = new JsonObject();
      jo.addProperty("type", type.name());
      if(getQuantity() != POSITION_QUANTITY) {
         jo.addProperty("quantity", getQuantity());
      }
      if(getLimit() != Double.NaN) {
         jo.addProperty("limit_price", getLimit());
      }
      if(getStop() != Double.NaN) {
         jo.addProperty("stop_price", getStop());
      }
      return jo;
   }
}
