package org.tradelib.core;

import static org.junit.Assert.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import org.junit.Test;

public class SeriesTest {

   @Test
   public void testFromCsv() throws Exception {
      Series ss = Series.fromCsv("test/data/es.csv", false, DateTimeFormatter.BASIC_ISO_DATE, LocalTime.of(17, 0));
      
      ss.setNames("open", "high", "low", "close", "volume", "interest");
      
      assertEquals(1829.5, ss.get(LocalDateTime.of(2013, 12, 26, 17, 0), 3), 1e-8);
      assertEquals(1829.5, ss.get(LocalDateTime.of(2013, 12, 26, 17, 0), "close"), 1e-8);
      
      assertEquals(1762.75, ss.get(LocalDateTime.of(2014, 2, 6, 17, 0), 1), 1e-8);
      assertEquals(1762.75, ss.get(LocalDateTime.of(2014, 2, 6, 17, 0), "high"), 1e-8);
      
      assertEquals(1079237.0, ss.get(LocalDateTime.of(2014, 5, 14, 17, 0), 4), 1e-8);
      assertEquals(1079237.0, ss.get(LocalDateTime.of(2014, 5, 14, 17, 0), "volume"), 1e-8);
   }
}
