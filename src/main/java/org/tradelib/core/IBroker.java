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
