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

import static org.junit.Assert.*;

import java.time.LocalDateTime;

import org.junit.Test;

public class OrderTest {
   @Test
   public void testFills() {
      // enterLongLimit
      Order oo = Order.enterLongLimit("ES", 1, 2000.25);
      OrderFill of = oo.tryFill(new Tick("ES", LocalDateTime.of(2015, 3, 3, 0, 0), 2000.50), 0, true);
      assertNull(of);
      
      of = oo.tryFill(new Tick("ES", LocalDateTime.of(2015, 3, 3, 0, 0), 1999.75), 0, true);
      assertEquals(of.getFilledQuantity(), 1);
      assertEquals(of.getFillPrice(), 2000.25, 0.0);
      assertEquals(of.getPosition(), 1);
      assertEquals(of.getTransactionQuantity(), 1);
      
      of = oo.tryFill(new Tick("ES", LocalDateTime.of(2015, 3, 3, 0, 0), 1999.75), 0, false);
      assertEquals(of.getFilledQuantity(), 1);
      assertEquals(of.getFillPrice(), 1999.75, 0.0);
      assertEquals(of.getPosition(), 1);
      assertEquals(of.getTransactionQuantity(), 1);
      
      of = oo.tryFill(new Tick("ES", LocalDateTime.of(2015, 3, 3, 0, 0), 2000.25), 0, true);
      assertEquals(of.getFilledQuantity(), 1);
      assertEquals(of.getFillPrice(), 2000.25, 0.0);
      
      // enterShortLimit
      oo = Order.enterShortLimit("ES", 1, 2000.25);
      of = oo.tryFill(new Tick("ES", LocalDateTime.of(2015, 3, 3, 0, 0), 1999.75), 0, true);
      assertNull(of);
      
      of = oo.tryFill(new Tick("ES", LocalDateTime.of(2015, 3, 3, 0, 0), 2000.50), 0, true);
      assertEquals(of.getFilledQuantity(), 1);
      assertEquals(of.getFillPrice(), 2000.25, 0.0);
      assertEquals(of.getPosition(), -1);
      assertEquals(of.getTransactionQuantity(), -1);
      
      of = oo.tryFill(new Tick("ES", LocalDateTime.of(2015, 3, 3, 0, 0), 2000.50), 0, false);
      assertEquals(of.getFilledQuantity(), 1);
      assertEquals(of.getFillPrice(), 2000.50, 0.0);
      assertEquals(of.getPosition(), -1);
      assertEquals(of.getTransactionQuantity(), -1);
      
      of = oo.tryFill(new Tick("ES", LocalDateTime.of(2015, 3, 3, 0, 0), 2000.25), 0, true);
      assertEquals(of.getFilledQuantity(), 1);
      assertEquals(of.getFillPrice(), 2000.25, 0.0);
      assertEquals(of.getTransactionQuantity(), -1);
      
      // enterShortStop
      oo = Order.enterShortStop("ES", 1, 2000.25);
      of = oo.tryFill(new Tick("ES", LocalDateTime.of(2015, 3, 3, 0, 0), 2000.50), 0, true);
      assertNull(of);
      
      of = oo.tryFill(new Tick("ES", LocalDateTime.of(2015, 3, 3, 0, 0), 1999.75), 0, true);
      assertEquals(of.getFilledQuantity(), 1);
      assertEquals(of.getFillPrice(), 2000.25, 0.0);
      assertEquals(of.getPosition(), -1);
      assertEquals(of.getTransactionQuantity(), -1);
      
      of = oo.tryFill(new Tick("ES", LocalDateTime.of(2015, 3, 3, 0, 0), 1999.75), 0, false);
      assertEquals(of.getFilledQuantity(), 1);
      assertEquals(of.getFillPrice(), 1999.75, 0.0);
      assertEquals(of.getPosition(), -1);
      assertEquals(of.getTransactionQuantity(), -1);
      
      of = oo.tryFill(new Tick("ES", LocalDateTime.of(2015, 3, 3, 0, 0), 2000.25), 0, true);
      assertEquals(of.getFilledQuantity(), 1);
      assertEquals(of.getFillPrice(), 2000.25, 0.0);
      assertEquals(of.getTransactionQuantity(), -1);
      
      // enterLongStop
      oo = Order.enterLongStop("ES", 1, 2000.25);
      of = oo.tryFill(new Tick("ES", LocalDateTime.of(2015, 3, 3, 0, 0), 2000), 0, true);
      assertNull(of);
      
      of = oo.tryFill(new Tick("ES", LocalDateTime.of(2015, 3, 3, 0, 0), 2000.50), 0, true);
      assertEquals(of.getFilledQuantity(), 1);
      assertEquals(of.getFillPrice(), 2000.25, 0.0);
      assertEquals(of.getPosition(), 1);
      assertEquals(of.getTransactionQuantity(), 1);
      
      of = oo.tryFill(new Tick("ES", LocalDateTime.of(2015, 3, 3, 0, 0), 2000.50), 0, false);
      assertEquals(of.getFilledQuantity(), 1);
      assertEquals(of.getFillPrice(), 2000.50, 0.0);
      assertEquals(of.getPosition(), 1);
      assertEquals(of.getTransactionQuantity(), 1);
      
      of = oo.tryFill(new Tick("ES", LocalDateTime.of(2015, 3, 3, 0, 0), 2000.25), 0, true);
      assertEquals(of.getFilledQuantity(), 1);
      assertEquals(of.getFillPrice(), 2000.25, 0.0);
      assertEquals(of.getTransactionQuantity(), 1);
      
      // enterLongStopLimit
      oo = Order.enterLongStopLimit("ES", 1, 2000.25, 2000.50);
      of = oo.tryFill(new Tick("ES", LocalDateTime.of(2015, 3, 3, 0, 0), 2000), 0, true);
      assertNull(of);
      
      oo = Order.enterLongStopLimit("ES", 1, 2000.25, 2000.50);
      of = oo.tryFill(new Tick("ES", LocalDateTime.of(2015, 3, 3, 0, 0), 2000.60), 0, false);
      assertNull(of);
      assertTrue(oo.isStopped());
      of = oo.tryFill(new Tick("ES", LocalDateTime.of(2015, 3, 3, 0, 1), 2000.3), 0, false);
      assertEquals(of.getFilledQuantity(), 1);
      assertEquals(of.getFillPrice(), 2000.3, 0.0);
      assertEquals(of.getPosition(), 1);
      assertEquals(of.getTransactionQuantity(), 1);
      
      oo = Order.enterLongStopLimit("ES", 1, 2000.25, 2000.50);
      of = oo.tryFill(new Tick("ES", LocalDateTime.of(2015, 3, 3, 0, 0), 2000.50), 0, true);
      assertEquals(of.getFilledQuantity(), 1);
      assertEquals(of.getFillPrice(), 2000.25, 0.0);
      assertEquals(of.getPosition(), 1);
      assertEquals(of.getTransactionQuantity(), 1);
      
      oo = Order.enterLongStopLimit("ES", 1, 2000.25, 2000.50);
      of = oo.tryFill(new Tick("ES", LocalDateTime.of(2015, 3, 3, 0, 0), 2000.50), 0, false);
      assertEquals(of.getFilledQuantity(), 1);
      assertEquals(of.getFillPrice(), 2000.50, 0.0);
      assertEquals(of.getPosition(), 1);
      assertEquals(of.getTransactionQuantity(), 1);
      
      oo = Order.enterLongStopLimit("ES", 1, 2000.25, 2000.50);
      of = oo.tryFill(new Tick("ES", LocalDateTime.of(2015, 3, 3, 0, 0), 2000.25), 0, true);
      assertEquals(of.getFilledQuantity(), 1);
      assertEquals(of.getFillPrice(), 2000.25, 0.0);
      assertEquals(of.getTransactionQuantity(), 1);
      
      // enterShortStopLimit
      oo = Order.enterShortStopLimit("ES", 1, 2001.25, 2000.50);
      of = oo.tryFill(new Tick("ES", LocalDateTime.of(2015, 3, 3, 0, 0), 2002), 0, true);
      assertNull(of);
      
      oo = Order.enterShortStopLimit("ES", 1, 2001.25, 2000.50);
      of = oo.tryFill(new Tick("ES", LocalDateTime.of(2015, 3, 3, 0, 0), 2000.25), 0, false);
      assertNull(of);
      assertTrue(oo.isStopped());
      of = oo.tryFill(new Tick("ES", LocalDateTime.of(2015, 3, 3, 0, 1), 2000.75), 0, false);
      assertEquals(of.getFilledQuantity(), 1);
      assertEquals(of.getFillPrice(), 2000.75, 0.0);
      assertEquals(of.getPosition(), -1);
      assertEquals(of.getTransactionQuantity(), -1);
      
      oo = Order.enterShortStopLimit("ES", 1, 2001.25, 2000.50);
      of = oo.tryFill(new Tick("ES", LocalDateTime.of(2015, 3, 3, 0, 0), 2000.75), 0, true);
      assertEquals(of.getFilledQuantity(), 1);
      assertEquals(of.getFillPrice(), 2001.25, 0.0);
      assertEquals(of.getPosition(), -1);
      assertEquals(of.getTransactionQuantity(), -1);
      
      oo = Order.enterShortStopLimit("ES", 1, 2001.25, 2000.50);
      of = oo.tryFill(new Tick("ES", LocalDateTime.of(2015, 3, 3, 0, 0), 2000.75), 0, false);
      assertEquals(of.getFilledQuantity(), 1);
      assertEquals(of.getFillPrice(), 2000.75, 0.0);
      assertEquals(of.getPosition(), -1);
      assertEquals(of.getTransactionQuantity(), -1);
      
      oo = Order.enterShortStopLimit("ES", 1, 2001.25, 2000.50);
      of = oo.tryFill(new Tick("ES", LocalDateTime.of(2015, 3, 3, 0, 0), 2000.75), 0, true);
      assertEquals(of.getFilledQuantity(), 1);
      assertEquals(of.getFillPrice(), 2001.25, 0.0);
      assertEquals(of.getTransactionQuantity(), -1);
      
      // enterLong
      oo = Order.enterLong("ES", 1);
      of = oo.tryFill(new Tick("ES", LocalDateTime.of(2015, 3, 3, 0, 0), 2000.75), 0, false);
      assertEquals(of.getFilledQuantity(), 1);
      assertEquals(of.getFillPrice(), 2000.75, 0.0);
      assertEquals(of.getPosition(), 1);
      assertEquals(of.getTransactionQuantity(), 1);
      
      // enterShort
      oo = Order.enterShort("ES", 3);
      of = oo.tryFill(new Tick("ES", LocalDateTime.of(2015, 3, 3, 0, 0), 2000.75), 0, false);
      assertEquals(of.getFilledQuantity(), 3);
      assertEquals(of.getFillPrice(), 2000.75, 0.0);
      assertEquals(of.getPosition(), -3);
      assertEquals(of.getTransactionQuantity(), -3);
   }
}
