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

import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import org.junit.Test;

public class SeriesTest {

   @Test
   public void testFromCsv() throws Exception {
      
      Series ss = Series.fromCsv(
                     Paths.get(getClass().getResource("/data/es.csv").toURI()).toString(),
                     false,
                     DateTimeFormatter.BASIC_ISO_DATE, LocalTime.of(17, 0));
      ss.setNames("open", "high", "low", "close", "volume", "interest");
      
      assertEquals(1829.5, ss.get(LocalDateTime.of(2013, 12, 26, 17, 0), 3), 1e-8);
      assertEquals(1829.5, ss.get(LocalDateTime.of(2013, 12, 26, 17, 0), "close"), 1e-8);
      
      assertEquals(1762.75, ss.get(LocalDateTime.of(2014, 2, 6, 17, 0), 1), 1e-8);
      assertEquals(1762.75, ss.get(LocalDateTime.of(2014, 2, 6, 17, 0), "high"), 1e-8);
      
      assertEquals(1079237.0, ss.get(LocalDateTime.of(2014, 5, 14, 17, 0), 4), 1e-8);
      assertEquals(1079237.0, ss.get(LocalDateTime.of(2014, 5, 14, 17, 0), "volume"), 1e-8);
   }
   
   @Test
   public void testHeadTail() throws Exception {
      Series ss = Series.fromCsv(
                     Paths.get(getClass().getResource("/data/oj.csv").toURI()).toString(),
                     false,
                     DateTimeFormatter.BASIC_ISO_DATE, LocalTime.of(17, 0));
      ss.setNames("open", "high", "low", "close", "volume", "interest");
      
      Series tt = ss.head(7);
      assertEquals(7, tt.size());
      assertEquals(145.75, tt.get(LocalDateTime.of(2013, 12, 24, 17, 0), "high"), 1e-8);
      
      tt = ss.head(-100);
      assertEquals(4, tt.size());
      assertEquals(148.05, tt.get(LocalDateTime.of(2013, 12, 18, 17, 0), "low"), 1e-8);
      
      tt = ss.tail(4);
      assertEquals(4, tt.size());
      assertEquals(168.60, tt.get(2, "high"), 1e-8);
      
      tt = ss.tail(-4);
      assertEquals(100, tt.size());
      assertEquals(168.60, tt.get(98, "high"), 1e-8);
   }
   
   @Test
   public void testToDaily() throws Exception {
      Series ss = Series.fromCsv(
                     Paths.get(getClass().getResource("/data/pnl.csv").toURI()).toString(),
                     true,
                     DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
      assertEquals(51, ss.size());
      // Accumulate using sum
      Series tt = ss.toDaily((Double x, Double y) -> x + y);
      // tt.head(10).print();
      assertEquals(186.37, tt.get(LocalDateTime.of(2014, 1, 2, 0, 0), 0), 1e-8);
      assertEquals(1846.61, tt.get(LocalDateTime.of(2014, 1, 7, 0, 0), 0), 1e-8);
      assertEquals(985.48, tt.get(LocalDateTime.of(2014, 1, 27, 0, 0), 0), 1e-8);
      assertEquals(0.0, tt.get(LocalDateTime.of(2014, 2, 18, 0, 0), 0), 1e-8);
      // tt.head(10).print("yyyy-MM-dd");
   }
}
