package org.tradelib.core;

import static org.junit.Assert.*;

import java.io.FileReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.junit.Test;

import com.google.common.collect.TreeBasedTable;
import com.opencsv.CSVReader;

public class PortfolioTest {
   
   private TimeSeries<Double> fromCsv(String path, boolean hasHeader) throws Exception {
      CSVReader csv = new CSVReader(new FileReader(path));
      String [] line = csv.readNext();
      if(line == null) return null;
      
      TimeSeries<Double> ts = new TimeSeries<Double>(line.length - 1);
      if(hasHeader) {
         line = csv.readNext();
         if(line == null) return null;
      }
      
      Double [] values = new Double[line.length - 1];
      
      for(; line != null; line = csv.readNext()) {
         for(int ii = 0; ii < values.length; ++ii) {
            values[ii] = Double.parseDouble(line[ii + 1]);
         }
         ts.add(LocalDate.parse(line[0], DateTimeFormatter.ofPattern("yyyyMMdd")).atTime(17, 0), values);
      }
      
      ts.setNames("open","high","low","close","volume","interest");
      
      return ts;
   }
   
   @Test
   public void testBasic() throws Exception {
      Instrument esInstrument = Instrument.makeFuture("ES", new BigDecimal("0.25"), new BigDecimal("50"));
      Portfolio portfolio = new Portfolio();
      portfolio.addInstrument(esInstrument);

      TimeSeries<Double> es = fromCsv("test/data/es.csv", false);
      
      double contractFees = -1.13;

      portfolio.addTransaction(esInstrument, LocalDateTime.of(2014, 1, 2, 17, 0, 0), 1, 1819.50, contractFees*1.0);
      portfolio.addTransaction(esInstrument, LocalDateTime.of(2014, 1, 9, 17, 0, 0), 2, 1826, contractFees*2.0);
      portfolio.addTransaction(esInstrument, LocalDateTime.of(2014, 1, 16, 17, 0, 0), -3, 1829.25, contractFees*3.0);
      
      portfolio.addTransaction(esInstrument, LocalDateTime.of(2014, 1, 24, 17, 0, 0), -2, 1775.00, contractFees*2.0);
      portfolio.addTransaction(esInstrument, LocalDateTime.of(2014, 1, 31, 17, 0, 0), 3, 1769.50, contractFees*3.0);
      portfolio.addTransaction(esInstrument, LocalDateTime.of(2014, 2, 7, 17, 0, 0), -2, 1786.50, contractFees*2.0);
      portfolio.addTransaction(esInstrument, LocalDateTime.of(2014, 2, 14, 17, 0, 0), 1, 1828.00, contractFees*1.0);
      
      TimeSeries<Double> pnl = portfolio.getPnlOld(esInstrument, es.subset("2013-12-31", "2014-02-18").columns("close"));
      for(int ii = 0; ii < pnl.size(); ++ii) {
         System.out.println(pnl.getTimestamp(ii).format(DateTimeFormatter.ISO_DATE_TIME) + " " + String.valueOf(pnl.get(ii)));
      }
      
      assertEquals(pnl.get("2014-01-02T17:00:00"), contractFees, 1e-8);
      assertEquals(pnl.get("2014-01-03T17:00:00"), -50.0, 1e-8);
      assertEquals(pnl.get("2014-01-06T17:00:00"), -237.5, 1e-8);
      assertEquals(pnl.get("2014-01-07T17:00:00"), 500.0, 1e-8);
      assertEquals(pnl.get("2014-01-08T17:00:00"), 87.5, 1e-8);
      assertEquals(pnl.get("2014-01-09T17:00:00"), 22.74, 1e-8);
      assertEquals(pnl.get("2014-01-10T17:00:00"), 712.5, 1e-8);
      assertEquals(pnl.get("2014-01-13T17:00:00"), -3412.5, 1e-8);
      assertEquals(pnl.get("2014-01-14T17:00:00"), 2700.0, 1e-8);
      assertEquals(pnl.get("2014-01-15T17:00:00"), 1275.0, 1e-8);
      assertEquals(pnl.get("2014-01-16T17:00:00"), -790.89, 1e-8);
      assertEquals(pnl.get("2014-01-17T17:00:00"), 0.0, 1e-8);
      assertEquals(pnl.get("2014-01-23T17:00:00"), 0.0, 1e-8);
      assertEquals(pnl.get("2014-01-24T17:00:00"), -2.26, 1e-8);
      assertEquals(pnl.get("2014-01-27T17:00:00"), 625.0, 1e-8);
      assertEquals(pnl.get("2014-01-28T17:00:00"), -1250, 1e-8);
      assertEquals(pnl.get("2014-01-29T17:00:00"), 1700.0, 1e-8);
      assertEquals(pnl.get("2014-01-30T17:00:00"), -1000.0, 1e-8);
      assertEquals(pnl.get("2014-01-31T17:00:00"), 472.74, 1e-8);
      assertEquals(pnl.get("2014-02-03T17:00:00"), -2187.5, 1e-8);
      assertEquals(pnl.get("2014-02-04T17:00:00"), 550.0, 1e-8);
      assertEquals(pnl.get("2014-02-05T17:00:00"), 12.5, 1e-8);
      assertEquals(pnl.get("2014-02-06T17:00:00"), 1125.0, 1e-8);
      assertEquals(pnl.get("2014-02-07T17:00:00"), 1348.87, 1e-8);
      assertEquals(pnl.get("2014-02-10T17:00:00"), -62.5, 1e-8);
      assertEquals(pnl.get("2014-02-11T17:00:00"), -937.5, 1e-8);
      assertEquals(pnl.get("2014-02-12T17:00:00"), -175.0, 1e-8);
      assertEquals(pnl.get("2014-02-13T17:00:00"), -362.5, 1e-8);
      assertEquals(pnl.get("2014-02-14T17:00:00"), -538.63, 1e-8);
      assertEquals(pnl.get("2014-02-18T17:00:00"), 0.0, 1e-8);
   }
   
   @Test
   public void testUpdatePositionPnl() throws Exception {
      Instrument esInstrument = Instrument.makeFuture("ES", new BigDecimal("0.25"), new BigDecimal("50"));
      Portfolio portfolio = new Portfolio();
      portfolio.addInstrument(esInstrument);

      TimeSeries<Double> es = fromCsv("test/data/es.csv", false);
      
      double contractFees = -1.13;

      portfolio.addTransaction(esInstrument, LocalDateTime.of(2014, 1, 2, 17, 0, 0), 1, 1819.50, contractFees*1.0);
      portfolio.addTransaction(esInstrument, LocalDateTime.of(2014, 1, 9, 17, 0, 0), 2, 1826, contractFees*2.0);
      portfolio.addTransaction(esInstrument, LocalDateTime.of(2014, 1, 16, 17, 0, 0), -3, 1829.25, contractFees*3.0);
      
      portfolio.addTransaction(esInstrument, LocalDateTime.of(2014, 1, 24, 17, 0, 0), -2, 1775.00, contractFees*2.0);
      portfolio.addTransaction(esInstrument, LocalDateTime.of(2014, 1, 31, 17, 0, 0), 3, 1769.50, contractFees*3.0);
      portfolio.addTransaction(esInstrument, LocalDateTime.of(2014, 2, 7, 17, 0, 0), -2, 1786.50, contractFees*2.0);
      portfolio.addTransaction(esInstrument, LocalDateTime.of(2014, 2, 14, 17, 0, 0), 1, 1828.00, contractFees*1.0);
      
      portfolio.updatePnl(esInstrument, es.subset("2013-12-31", "2014-02-18").columns("close"));
   }
}
