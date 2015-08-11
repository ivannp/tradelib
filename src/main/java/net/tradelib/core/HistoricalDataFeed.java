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
import java.util.HashSet;

import com.google.common.eventbus.EventBus;

/**
 * @class DataFeed
 *
 * @brief The abstract base class for a data feed
 *
 * The use-case scenario is:
 *
 *    1. "configure" - configures the DataFeed using a single
 *    string (config file for instance) as input
 *    
 *    2. "subscribe" a few times
 *    
 *    3. "start" - kicks off the processing. This method
 *    returns when the feed is exhausted.
 *
 * Afterwards "reset" must return the data feed at step 2 -
 * configured without any subscriptions.
 */
public abstract class HistoricalDataFeed {
   
   protected LocalDateTime feedStart;
   protected LocalDateTime feedStop;
   
   protected HashSet<String> subscriptions;
   
   protected HashSet<IBarListener> barListeners;
   
   public HistoricalDataFeed(Context context) {
      subscriptions = new HashSet<String>();
      barListeners = new HashSet<IBarListener>();
   }
   
   public abstract void configure(String config) throws Exception;
   
   public abstract void start() throws Exception;

   public void subscribe(String symbol) {
	   subscriptions.add(symbol);
   }
   
   public void unsubscribe(String symbol) {
	   subscriptions.remove(symbol);
   }
   
   public void reset() {
	   subscriptions.clear();
   }
   
   public void addBarListener(IBarListener listener) { barListeners.add(listener); }
   
   public abstract Instrument getInstrument(String symbol) throws Exception;
   public abstract InstrumentVariation getInstrumentVariation(String provider, String symbol) throws Exception;
   
   public LocalDateTime getFeedStart() { return feedStart; }
   public void setFeedStart(LocalDateTime ldt) { this.feedStart = ldt; }
   public void setFeedStop(LocalDateTime ldt) { this.feedStop = ldt; }
   public LocalDateTime getFeedStop() { return feedStop; }
}
