package org.tradelib.core;

import static org.junit.Assert.*;

import java.io.FileReader;
import java.math.BigDecimal;
import java.net.URI;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.junit.Test;

import com.opencsv.CSVReader;

public class AccountTest {
   
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
   public void testAccount() throws Exception {
      Instrument esInstrument = Instrument.makeFuture("ES", new BigDecimal("0.25"), new BigDecimal("50"));
      
      TimeSeries<Double> es = fromCsv(
                                 Paths.get(getClass().getResource("/data/es.csv").toURI()).toString(),
                                 false);
      
      Account account = new Account();
      assertEquals(0.0, account.getEndEquity(), 1e-8);
      
      // The main usage scenario: Add initial equity to the account, afterwards,
      // record transactions, but mark the account at the end of every day. Once,
      // in a while, we also update the equity curve, to determine current capital. 
      
      double initialEquity = 15000.0;
      
      account = new Account(LocalDateTime.of(2013, 12, 31, 0, 0, 0), initialEquity);
      assertEquals(initialEquity, account.getEndEquity(), 1e-8);
      
      double contractFees = -1.13;

      // Mark the portfolio with a price - must happen before a transaction
      account.mark(esInstrument, LocalDateTime.of(2014, 12, 31, 17, 0, 0), 1834.00);
      
      // Add a transaction
      account.addTransaction(esInstrument, LocalDateTime.of(2014, 1, 2, 17, 0, 0), 1, 1819.50, contractFees*1.0);
      // Mark the portfolio
      account.mark(esInstrument, LocalDateTime.of(2014, 1, 2, 17, 0, 0), 1819.50);
      // Verify the PnL
      PositionPnl posPnl = account.getPositionPnl(esInstrument);
      assertEquals(contractFees, posPnl.fees, 1e-8);
      assertEquals(contractFees, posPnl.netPnl, 1e-8);
      assertEquals(0.0, posPnl.grossPnl, 1e-8);
      assertEquals(0.0, posPnl.unrealizedPnl, 1e-8);
      assertEquals(0.0, posPnl.realizedPnl, 1e-8);
      assertEquals(90975.0, posPnl.transactionValue, 1e-8);
      assertEquals(1819.5, posPnl.positionAverageCost, 1e-8);
      assertEquals(90975.0, posPnl.positionValue, 1e-8);
      assertEquals(1.0, posPnl.positionQuantity, 1e-8);
      
      // Update the end equity and verify the capital
      account.updateEndEquity(LocalDateTime.of(2014, 1, 2, 17, 0, 0));
      assertEquals(initialEquity + contractFees, account.getEndEquity(), 1e-8);
      
      // Mark the account on a daily basis
      account.mark(esInstrument, LocalDateTime.of(2014, 1, 3, 17, 0, 0), 1818.50);
      account.mark(esInstrument, LocalDateTime.of(2014, 1, 6, 17, 0, 0), 1813.75);
      
      // Update the end equity and verify the capital
      account.updateEndEquity(LocalDateTime.of(2014, 1, 6, 17, 0, 0));
      assertEquals(14711.37, account.getEndEquity(), 1e-8);

      // Mark the account on a daily basis
      account.mark(esInstrument, LocalDateTime.of(2014, 1, 7, 17, 0, 0), 1823.75);
      account.mark(esInstrument, LocalDateTime.of(2014, 1, 8, 17, 0, 0), 1825.50);
      
      account.addTransaction(esInstrument, LocalDateTime.of(2014, 1, 9, 17, 0, 0), 2, 1826, contractFees*2.0);
      account.mark(esInstrument, LocalDateTime.of(2014, 1, 9, 17, 0, 0), 1826.00);
      
      // Update the end equity and verify the capital
      account.updateEndEquity(LocalDateTime.of(2014, 1, 9, 17, 0, 0));
      assertEquals(15321.61, account.getEndEquity(), 1e-8);
      assertEquals(14711.37, account.getEndEquity(LocalDateTime.of(2014, 1, 6, 17, 0, 0)), 1e-8);
      
      account.addTransaction(esInstrument, LocalDateTime.of(2014, 1, 16, 17, 0, 0), -3, 1829.25, contractFees*3.0);
      account.mark(esInstrument, LocalDateTime.of(2014, 1, 16, 17, 0, 0), 1829.25);
      account.updateEndEquity(LocalDateTime.of(2014, 1, 16, 17, 0, 0));
      assertEquals(15805.72, account.getEndEquity(), 1e-8);
   }
   
   @Test
   public void testMultiInstrumentAccount() throws Exception {
      
      /** Generated using test.blotter2(-1.13) from blotter.test.r
       */
      double initialEquity = 150000.0;
      double contractFees = -1.13;
      
      Instrument esInstrument = Instrument.makeFuture("ES", new BigDecimal("0.25"), new BigDecimal("50"));
      Instrument ojInstrument = Instrument.makeFuture("OJ", new BigDecimal("0.05"), new BigDecimal("150"));
      Instrument audInstrument = Instrument.makeFuture("AUD", new BigDecimal("0.01"), new BigDecimal("1000"));
      
      TimeSeries<Double> es = fromCsv(
            Paths.get(getClass().getResource("/data/es.csv").toURI()).toString(),
            false);
      TimeSeries<Double> oj = fromCsv(
            Paths.get(getClass().getResource("/data/oj.csv").toURI()).toString(),
            false);
      TimeSeries<Double> aud = fromCsv(
            Paths.get(getClass().getResource("/data/aud.csv").toURI()).toString(),
            false);
      
      Account account = new Account(LocalDateTime.of(2013, 12, 26, 0, 0, 0), initialEquity);
      
      // Mark the portfolio with a price - must happen before a transaction
      
      // 2013-12-27
      account.mark(esInstrument, LocalDateTime.of(2013, 12, 27, 17, 0, 0), 1829.50);
      account.mark(ojInstrument, LocalDateTime.of(2013, 12, 27, 17, 0, 0), 145.00);
      account.mark(audInstrument, LocalDateTime.of(2013, 12, 27, 17, 0, 0), 85.51);
      
      // 2013-12-30
      account.mark(esInstrument, LocalDateTime.of(2013, 12, 30, 17, 0, 0), 1827.75);
      account.mark(ojInstrument, LocalDateTime.of(2013, 12, 30, 17, 0, 0), 147.75);
      account.mark(audInstrument, LocalDateTime.of(2013, 12, 30, 17, 0, 0), 85.98);
      
      // 2013-12-31
      account.mark(esInstrument, LocalDateTime.of(2013, 12, 31, 17, 0, 0), 1834.00);
      account.mark(ojInstrument, LocalDateTime.of(2013, 12, 31, 17, 0, 0), 145.95);
      account.mark(audInstrument, LocalDateTime.of(2013, 12, 31, 17, 0, 0), 86.13);
      
      // 2014-01-02
      account.addTransaction(esInstrument, LocalDateTime.of(2014, 1, 2, 11, 0, 1), 1, 1815.75, contractFees*1.0);
      
      account.mark(esInstrument, LocalDateTime.of(2014, 1, 2, 17, 0, 0), 1819.50);
      account.mark(ojInstrument, LocalDateTime.of(2014, 1, 2, 17, 0, 0), 144.70);
      account.mark(audInstrument, LocalDateTime.of(2014, 1, 2, 17, 0, 0), 85.95);
      
      account.updateEndEquity(LocalDateTime.of(2014, 1, 2, 17, 0, 0));
      double endEq = account.getEndEquity();
      assertEquals(150186.37, endEq, 1e-8);
      
      // 2014-01-03
      account.addTransaction(ojInstrument, LocalDateTime.of(2014, 1, 3, 11, 0, 1), 2, 144.25, contractFees*2.0);
      account.addTransaction(audInstrument, LocalDateTime.of(2014, 1, 3, 13, 0, 1), -2, 86.88, contractFees*2.0);
      
      account.mark(esInstrument, LocalDateTime.of(2014, 1, 3, 17, 0, 0), 1818.50);
      account.mark(ojInstrument, LocalDateTime.of(2014, 1, 3, 17, 0, 0), 146.5);
      account.mark(audInstrument, LocalDateTime.of(2014, 1, 3, 17, 0, 0), 86.64);
      
      account.updateEndEquity(LocalDateTime.of(2014, 1, 3, 17, 0, 0));
      endEq = account.getEndEquity();
      assertEquals(151286.85, endEq, 1e-8);
      
      // 2014-01-06
      account.addTransaction(audInstrument, LocalDateTime.of(2014, 1, 6, 14, 0, 1), -3, 86.34, contractFees*3.0);
      
      account.mark(esInstrument, LocalDateTime.of(2014, 1, 6, 17, 0, 0), 1813.75);
      account.mark(ojInstrument, LocalDateTime.of(2014, 1, 6, 17, 0, 0), 150.35);
      account.mark(audInstrument, LocalDateTime.of(2014, 1, 6, 17, 0, 0), 86.53);
      
      account.updateEndEquity(LocalDateTime.of(2014, 1, 6, 17, 0, 0));
      endEq = account.getEndEquity();
      assertEquals(151850.96, endEq, 1e-8);
      
      // 2014-01-07
      account.addTransaction(esInstrument, LocalDateTime.of(2014, 1, 7, 14, 0, 2), -3, 1819.25, contractFees*3.0);
      
      account.mark(esInstrument, LocalDateTime.of(2014, 1, 7, 17, 0, 0), 1823.75);
      account.mark(ojInstrument, LocalDateTime.of(2014, 1, 7, 17, 0, 0), 150.10);
      account.mark(audInstrument, LocalDateTime.of(2014, 1, 7, 17, 0, 0), 86.11);
      
      account.updateEndEquity(LocalDateTime.of(2014, 1, 7, 17, 0, 0));
      endEq = account.getEndEquity();
      assertEquals(153697.57, endEq, 1e-8);
      
      // 2014-01-08
      account.mark(esInstrument, LocalDateTime.of(2014, 1, 8, 17, 0, 0), 1825.50);
      account.mark(ojInstrument, LocalDateTime.of(2014, 1, 8, 17, 0, 0), 149.05);
      account.mark(audInstrument, LocalDateTime.of(2014, 1, 8, 17, 0, 0), 86.00);
      
      account.updateEndEquity(LocalDateTime.of(2014, 1, 8, 17, 0, 0));
      endEq = account.getEndEquity();
      assertEquals(153757.57, endEq, 1e-8);
      
      // 2014-01-09
      account.mark(esInstrument, LocalDateTime.of(2014, 1, 9, 17, 0, 0), 1826.00);
      account.mark(ojInstrument, LocalDateTime.of(2014, 1, 9, 17, 0, 0), 149.15);
      account.mark(audInstrument, LocalDateTime.of(2014, 1, 9, 17, 0, 0), 85.82);
      
      account.updateEndEquity(LocalDateTime.of(2014, 1, 9, 17, 0, 0));
      endEq = account.getEndEquity();
      assertEquals(154637.57, endEq, 1e-8);
      
      // 2014-01-10
      account.addTransaction(ojInstrument, LocalDateTime.of(2014, 1, 10, 14, 0, 3), -2, 151.20, contractFees*2.0);
      
      account.mark(esInstrument, LocalDateTime.of(2014, 1, 10, 17, 0, 0), 1830.75);
      account.mark(ojInstrument, LocalDateTime.of(2014, 1, 10, 17, 0, 0), 154.85);
      account.mark(audInstrument, LocalDateTime.of(2014, 1, 10, 17, 0, 0), 86.85);
      
      account.updateEndEquity(LocalDateTime.of(2014, 1, 10, 17, 0, 0));
      endEq = account.getEndEquity();
      assertEquals(149625.31, endEq, 1e-8);
      
      // 2014-01-13
      account.addTransaction(audInstrument, LocalDateTime.of(2014, 1, 13, 14, 0, 2), 1, 87.71, contractFees*1.0);
      
      account.mark(esInstrument, LocalDateTime.of(2014, 1, 13, 17, 0, 0), 1808.00);
      account.mark(ojInstrument, LocalDateTime.of(2014, 1, 13, 17, 0, 0), 155.15);
      account.mark(audInstrument, LocalDateTime.of(2014, 1, 13, 17, 0, 0), 87.52);
      
      account.updateEndEquity(LocalDateTime.of(2014, 1, 13, 17, 0, 0));
      endEq = account.getEndEquity();
      assertEquals(148359.18, endEq, 1e-8);
      
      // 2014-01-14
      account.mark(esInstrument, LocalDateTime.of(2014, 1, 14, 17, 0, 0), 1826.00);
      account.mark(ojInstrument, LocalDateTime.of(2014, 1, 14, 17, 0, 0), 152.10);
      account.mark(audInstrument, LocalDateTime.of(2014, 1, 14, 17, 0, 0), 86.52);
      
      account.updateEndEquity(LocalDateTime.of(2014, 1, 14, 17, 0, 0));
      endEq = account.getEndEquity();
      assertEquals(150559.18, endEq, 1e-8);
      
      // 2014-01-15
      account.mark(esInstrument, LocalDateTime.of(2014, 1, 15, 17, 0, 0), 1834.50);
      account.mark(ojInstrument, LocalDateTime.of(2014, 1, 15, 17, 0, 0), 151.90);
      account.mark(audInstrument, LocalDateTime.of(2014, 1, 15, 17, 0, 0), 86.04);
      
      account.updateEndEquity(LocalDateTime.of(2014, 1, 15, 17, 0, 0));
      endEq = account.getEndEquity();
      assertEquals(151629.18, endEq, 1e-8);
      
      // 2014-01-16
      account.mark(esInstrument, LocalDateTime.of(2014, 1, 16, 17, 0, 0), 1829.25);
      account.mark(ojInstrument, LocalDateTime.of(2014, 1, 16, 17, 0, 0), 148.15);
      account.mark(audInstrument, LocalDateTime.of(2014, 1, 16, 17, 0, 0), 85.13);
      
      account.updateEndEquity(LocalDateTime.of(2014, 1, 16, 17, 0, 0));
      endEq = account.getEndEquity();
      assertEquals(155794.18, endEq, 1e-8);
      
      // 2014-01-17
      account.mark(esInstrument, LocalDateTime.of(2014, 1, 17, 17, 0, 0), 1827.25);
      account.mark(ojInstrument, LocalDateTime.of(2014, 1, 17, 17, 0, 0), 148.60);
      account.mark(audInstrument, LocalDateTime.of(2014, 1, 17, 17, 0, 0), 84.68);
      
      account.updateEndEquity(LocalDateTime.of(2014, 1, 17, 17, 0, 0));
      endEq = account.getEndEquity();
      assertEquals(157794.18, endEq, 1e-8);
      
      // 2014-01-21
      account.addTransaction(audInstrument, LocalDateTime.of(2014, 1, 21, 14, 0, 1), 6, 84.57, contractFees*6.0);
      account.addTransaction(ojInstrument, LocalDateTime.of(2014, 1, 21, 14, 0, 2), 3, 148.35, contractFees*3.0);
      
      account.mark(ojInstrument, LocalDateTime.of(2014, 1, 21, 17, 0, 0), 150.15);
      account.mark(esInstrument, LocalDateTime.of(2014, 1, 21, 17, 0, 0), 1831.50);
      account.mark(audInstrument, LocalDateTime.of(2014, 1, 21, 17, 0, 0), 85.01);
      
      account.updateEndEquity(LocalDateTime.of(2014, 1, 21, 17, 0, 0));
      endEq = account.getEndEquity();
      assertEquals(159489.01, endEq, 1e-8);
      
      // 2014-01-22
      account.mark(ojInstrument, LocalDateTime.of(2014, 1, 22, 17, 0, 0), 150.95);
      account.mark(esInstrument, LocalDateTime.of(2014, 1, 22, 17, 0, 0), 1831.50);
      account.mark(audInstrument, LocalDateTime.of(2014, 1, 22, 17, 0, 0), 85.45);
      
      account.updateEndEquity(LocalDateTime.of(2014, 1, 22, 17, 0, 0));
      endEq = account.getEndEquity();
      assertEquals(160729.01, endEq, 1e-8);
      
      // 2014-01-23
      account.addTransaction(audInstrument, LocalDateTime.of(2014, 1, 23, 11, 0, 5), -2, 85.47, contractFees*2.0);
      account.addTransaction(esInstrument, LocalDateTime.of(2014, 1, 23, 11, 0, 5), -2, 1835.25, contractFees*2.0);
      
      account.mark(ojInstrument, LocalDateTime.of(2014, 1, 23, 17, 0, 0), 151.20);
      account.mark(esInstrument, LocalDateTime.of(2014, 1, 23, 17, 0, 0), 1817.25);
      account.mark(audInstrument, LocalDateTime.of(2014, 1, 23, 17, 0, 0), 84.62);
      
      account.updateEndEquity(LocalDateTime.of(2014, 1, 23, 17, 0, 0));
      endEq = account.getEndEquity();
      assertEquals(164101.99, endEq, 1e-8);
      
      // 2014-01-24
      account.mark(ojInstrument, LocalDateTime.of(2014, 1, 24, 17, 0, 0), 149.75);
      account.mark(esInstrument, LocalDateTime.of(2014, 1, 24, 17, 0, 0), 1775.00);
      account.mark(audInstrument, LocalDateTime.of(2014, 1, 24, 17, 0, 0), 84.15);
      
      account.updateEndEquity(LocalDateTime.of(2014, 1, 24, 17, 0, 0));
      endEq = account.getEndEquity();
      assertEquals(171899.49, endEq, 1e-8);
      
      // 2014-01-27
      account.addTransaction(esInstrument, LocalDateTime.of(2014, 1, 27, 11, 0, 5), 4, 1761.50, contractFees*4.0);
      
      account.mark(ojInstrument, LocalDateTime.of(2014, 1, 27, 17, 0, 0), 145.95);
      account.mark(esInstrument, LocalDateTime.of(2014, 1, 27, 17, 0, 0), 1768.75);
      account.mark(audInstrument, LocalDateTime.of(2014, 1, 27, 17, 0, 0), 84.54);
      
      account.updateEndEquity(LocalDateTime.of(2014, 1, 27, 17, 0, 0));
      endEq = account.getEndEquity();
      assertEquals(172884.97, endEq, 1e-8);
      
      // 2014-01-28
      account.addTransaction(audInstrument, LocalDateTime.of(2014, 1, 28, 11, 0, 5), -2, 85.13, contractFees*2.0);
      
      account.mark(ojInstrument, LocalDateTime.of(2014, 1, 28, 17, 0, 0), 145.45);
      account.mark(esInstrument, LocalDateTime.of(2014, 1, 28, 17, 0, 0), 1781.25);
      account.mark(audInstrument, LocalDateTime.of(2014, 1, 28, 17, 0, 0), 84.74);
      
      account.updateEndEquity(LocalDateTime.of(2014, 1, 28, 17, 0, 0));
      endEq = account.getEndEquity();
      assertEquals(173437.71, endEq, 1e-8);
      
      // 2014-01-29
      account.mark(ojInstrument, LocalDateTime.of(2014, 1, 29, 17, 0, 0), 144.75);
      account.mark(esInstrument, LocalDateTime.of(2014, 1, 29, 17, 0, 0), 1764.25);
      account.mark(audInstrument, LocalDateTime.of(2014, 1, 29, 17, 0, 0), 84.49);
      
      account.updateEndEquity(LocalDateTime.of(2014, 1, 29, 17, 0, 0));
      endEq = account.getEndEquity();
      assertEquals(173622.71, endEq, 1e-8);
      
      // 2014-01-30
      account.mark(ojInstrument, LocalDateTime.of(2014, 1, 30, 17, 0, 0), 146.45);
      account.mark(esInstrument, LocalDateTime.of(2014, 1, 30, 17, 0, 0), 1774.25);
      account.mark(audInstrument, LocalDateTime.of(2014, 1, 30, 17, 0, 0), 84.90);
      
      account.updateEndEquity(LocalDateTime.of(2014, 1, 30, 17, 0, 0));
      endEq = account.getEndEquity();
      assertEquals(173567.71, endEq, 1e-8);
      
      // 2014-01-31
      account.addTransaction(audInstrument, LocalDateTime.of(2014, 1, 31, 14, 0, 1), 1, 84.16, contractFees*1.0);
      
      account.mark(ojInstrument, LocalDateTime.of(2014, 1, 31, 17, 0, 0), 149.30);
      account.mark(esInstrument, LocalDateTime.of(2014, 1, 31, 17, 0, 0), 1769.50);
      account.mark(audInstrument, LocalDateTime.of(2014, 1, 31, 17, 0, 0), 84.52);
      
      account.updateEndEquity(LocalDateTime.of(2014, 1, 31, 17, 0, 0));
      endEq = account.getEndEquity();
      assertEquals(175969.08, endEq, 1e-8);
      
      // 2014-02-03
      account.mark(ojInstrument, LocalDateTime.of(2014, 2, 3, 17, 0, 0), 149.10);
      account.mark(esInstrument, LocalDateTime.of(2014, 2, 3, 17, 0, 0), 1725.75);
      account.mark(audInstrument, LocalDateTime.of(2014, 2, 3, 17, 0, 0), 84.61);
      
      account.updateEndEquity(LocalDateTime.of(2014, 2, 3, 17, 0, 0));
      endEq = account.getEndEquity();
      assertEquals(175789.08, endEq, 1e-8);
      
      // 2014-02-04
      account.mark(ojInstrument, LocalDateTime.of(2014, 2, 4, 17, 0, 0), 150.20);
      account.mark(esInstrument, LocalDateTime.of(2014, 2, 4, 17, 0, 0), 1736.75);
      account.mark(audInstrument, LocalDateTime.of(2014, 2, 4, 17, 0, 0), 86.39);
      
      account.updateEndEquity(LocalDateTime.of(2014, 2, 4, 17, 0, 0));
      endEq = account.getEndEquity();
      assertEquals(174504.08, endEq, 1e-8);
      
      // 2014-02-05
      account.mark(ojInstrument, LocalDateTime.of(2014, 2, 5, 17, 0, 0), 152.55);
      account.mark(esInstrument, LocalDateTime.of(2014, 2, 5, 17, 0, 0), 1737.00);
      account.mark(audInstrument, LocalDateTime.of(2014, 2, 5, 17, 0, 0), 86.15);
      
      account.updateEndEquity(LocalDateTime.of(2014, 2, 5, 17, 0, 0));
      endEq = account.getEndEquity();
      assertEquals(175801.58, endEq, 1e-8);
      
      // 2014-02-06
      account.mark(ojInstrument, LocalDateTime.of(2014, 2, 6, 17, 0, 0), 152.20);
      account.mark(esInstrument, LocalDateTime.of(2014, 2, 6, 17, 0, 0), 1759.50);
      account.mark(audInstrument, LocalDateTime.of(2014, 2, 6, 17, 0, 0), 86.72);
      
      account.updateEndEquity(LocalDateTime.of(2014, 2, 6, 17, 0, 0));
      endEq = account.getEndEquity();
      assertEquals(175074.08, endEq, 1e-8);
      
      // 2014-02-07
      account.addTransaction(audInstrument, LocalDateTime.of(2014, 2, 7, 11, 0, 5), 1, 87.01, contractFees*1.0);
      
      account.mark(ojInstrument, LocalDateTime.of(2014, 2, 7, 17, 0, 0), 154.00);
      account.mark(esInstrument, LocalDateTime.of(2014, 2, 7, 17, 0, 0), 1786.50);
      account.mark(audInstrument, LocalDateTime.of(2014, 2, 7, 17, 0, 0), 86.69);
      
      account.updateEndEquity(LocalDateTime.of(2014, 2, 7, 17, 0, 0));
      endEq = account.getEndEquity();
      assertEquals(175592.95, endEq, 1e-8);
      
      // 2014-02-10
      account.mark(ojInstrument, LocalDateTime.of(2014, 2, 10, 17, 0, 0), 153.75);
      account.mark(esInstrument, LocalDateTime.of(2014, 2, 10, 17, 0, 0), 1787.75);
      account.mark(audInstrument, LocalDateTime.of(2014, 2, 10, 17, 0, 0), 86.55);
      
      account.updateEndEquity(LocalDateTime.of(2014, 2, 10, 17, 0, 0));
      endEq = account.getEndEquity();
      assertEquals(175480.45, endEq, 1e-8);
      
      // 2014-02-11
      account.mark(ojInstrument, LocalDateTime.of(2014, 2, 11, 17, 0, 0), 152.20);
      account.mark(esInstrument, LocalDateTime.of(2014, 2, 11, 17, 0, 0), 1806.50);
      account.mark(audInstrument, LocalDateTime.of(2014, 2, 11, 17, 0, 0), 87.47);
      
      account.updateEndEquity(LocalDateTime.of(2014, 2, 11, 17, 0, 0));
      endEq = account.getEndEquity();
      assertEquals(174782.95, endEq, 1e-8);
      
      // 2014-02-12
      account.mark(ojInstrument, LocalDateTime.of(2014, 2, 12, 17, 0, 0), 153.00);
      account.mark(esInstrument, LocalDateTime.of(2014, 2, 12, 17, 0, 0), 1810.00);
      account.mark(audInstrument, LocalDateTime.of(2014, 2, 12, 17, 0, 0), 87.43);
      
      account.updateEndEquity(LocalDateTime.of(2014, 2, 12, 17, 0, 0));
      endEq = account.getEndEquity();
      assertEquals(175142.95, endEq, 1e-8);
      
      // 2014-02-13
      account.mark(ojInstrument, LocalDateTime.of(2014, 2, 13, 17, 0, 0), 153.20);
      account.mark(esInstrument, LocalDateTime.of(2014, 2, 13, 17, 0, 0), 1817.25);
      account.mark(audInstrument, LocalDateTime.of(2014, 2, 13, 17, 0, 0), 87.02);
      
      account.updateEndEquity(LocalDateTime.of(2014, 2, 13, 17, 0, 0));
      endEq = account.getEndEquity();
      assertEquals(175232.95, endEq, 1e-8);
      
      // 2014-02-14
      account.addTransaction(ojInstrument, LocalDateTime.of(2014, 2, 14, 11, 0, 5), -3, 153.10, contractFees*3.0);
      
      account.mark(ojInstrument, LocalDateTime.of(2014, 2, 14, 17, 0, 0), 151.10);
      account.mark(esInstrument, LocalDateTime.of(2014, 2, 14, 17, 0, 0), 1828.00);
      account.mark(audInstrument, LocalDateTime.of(2014, 2, 14, 17, 0, 0), 87.49);
      
      account.updateEndEquity(LocalDateTime.of(2014, 2, 14, 17, 0, 0));
      endEq = account.getEndEquity();
      assertEquals(175184.56, endEq, 1e-8);
      
      // 2014-02-18
      account.mark(ojInstrument, LocalDateTime.of(2014, 2, 18, 17, 0, 0), 150.40);
      account.mark(esInstrument, LocalDateTime.of(2014, 2, 18, 17, 0, 0), 1830.50);
      account.mark(audInstrument, LocalDateTime.of(2014, 2, 18, 17, 0, 0), 87.48);
      
      account.updateEndEquity(LocalDateTime.of(2014, 2, 18, 17, 0, 0));
      endEq = account.getEndEquity();
      assertEquals(175184.56, endEq, 1e-8);
      
      // 2014-02-19
      account.mark(ojInstrument, LocalDateTime.of(2014, 2, 19, 17, 0, 0), 150.15);
      account.mark(esInstrument, LocalDateTime.of(2014, 2, 19, 17, 0, 0), 1818.50);
      account.mark(audInstrument, LocalDateTime.of(2014, 2, 19, 17, 0, 0), 87.19);
      
      account.updateEndEquity(LocalDateTime.of(2014, 2, 19, 17, 0, 0));
      endEq = account.getEndEquity();
      assertEquals(175184.56, endEq, 1e-8);
      
      // Load the expected account values
      DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
      Series eas = Series.fromCsv(
            Paths.get(getClass().getResource("/data/account.summary.csv").toURI()).toString(),
            true,
            dtf);
      // If we have repetitive days - these are fractions
      for(int ii = 1; ii < eas.size(); ++ii) {
         if(eas.getTimestamp(ii).equals(eas.getTimestamp(ii-1))) {
            eas.set(ii, eas.getTimestamp(ii).plusNanos(1000));
         }
      }
            
      Series as = account.getSummary();
      for(int ii = 0; ii < as.size(); ++ii) {
         LocalDateTime ldt = as.getTimestamp(ii);
         if(ldt.isBefore(LocalDateTime.of(2013, 12, 31, 0, 0))) continue;
         if(ldt.isAfter(LocalDateTime.of(2014, 2, 14, 0, 0))) continue;
         assertEquals(eas.get(ldt, "Additions"), as.get(ii, "addition"), 1e-8);
         assertEquals(eas.get(ldt, "Withdrawals"), as.get(ii, "withdrawal"), 1e-8);
         assertEquals(eas.get(ldt, "Interest"), as.get(ii, "interest"), 1e-8);
         assertEquals(eas.get(ldt, "Realized.PL"), as.get(ii, "realized.pnl"), 1e-8);
         assertEquals(eas.get(ldt, "Unrealized.PL"), as.get(ii, "unrealized.pnl"), 1e-8);
         assertEquals(eas.get(ldt, "Gross.Trading.PL"), as.get(ii, "gross.pnl"), 1e-8);
         assertEquals(eas.get(ldt, "Net.Trading.PL"), as.get(ii, "net.pnl"), 1e-8);
         assertEquals(eas.get(ldt, "Txn.Fees"), as.get(ii, "fees"), 1e-8);
         assertEquals(eas.get(ldt, "Advisory.Fees"), as.get(ii, "advisory.fees"), 1e-8);
         assertEquals(eas.get(ldt, "Net.Performance"), as.get(ii, "net.performance"), 1e-8);
         assertEquals(eas.get(ldt, "End.Eq"), as.get(ii, "end.equity"), 1e-8);
      }
      
      Series eps = Series.fromCsv(
            Paths.get(getClass().getResource("/data/portfolio.summary.csv").toURI()).toString(),
            true,
            dtf);
      // If we have repetitive days - these are fractions
      for(int ii = 1; ii < eps.size(); ++ii) {
         if(eps.getTimestamp(ii).equals(eps.getTimestamp(ii-1))) {
            eps.set(ii, eps.getTimestamp(ii).plusNanos(1000));
         }
      }
      
      Series ps = account.getPortfolioSummary();
      for(int ii = 0; ii < ps.size(); ++ii) {
         LocalDateTime ldt = ps.getTimestamp(ii);
         if(ldt.isBefore(LocalDateTime.of(2013, 12, 31, 0, 0))) continue;
         if(ldt.isAfter(LocalDateTime.of(2014, 2, 14, 0, 0))) continue;
         assertEquals(eps.get(ldt, "Long.Value"), ps.get(ii, "long.value"), 1e-8);
         assertEquals(eps.get(ldt, "Short.Value"), ps.get(ii, "short.value"), 1e-8);
         assertEquals(eps.get(ldt, "Net.Value"), ps.get(ii, "net.value"), 1e-8);
         assertEquals(eps.get(ldt, "Gross.Value"), ps.get(ii, "gross.value"), 1e-8);
         assertEquals(eps.get(ldt, "Realized.PL"), ps.get(ii, "realized.pnl"), 1e-8);
         assertEquals(eps.get(ldt, "Unrealized.PL"), ps.get(ii, "unrealized.pnl"), 1e-8);
         assertEquals(eps.get(ldt, "Gross.Trading.PL"), ps.get(ii, "gross.pnl"), 1e-8);
         assertEquals(eps.get(ldt, "Txn.Fees"), ps.get(ii, "fees"), 1e-8);
         assertEquals(eps.get(ldt, "Net.Trading.PL"), ps.get(ii, "net.pnl"), 1e-8);
      }
      
      Series expected = Series.fromCsv(
            Paths.get(getClass().getResource("/data/portfolio.oj.csv").toURI()).toString(),
            true,
            dtf);
      // If we have repetitive days - these are fractions
      for(int ii = 1; ii < expected.size(); ++ii) {
         if(expected.getTimestamp(ii).equals(expected.getTimestamp(ii-1))) {
            expected.set(ii, expected.getTimestamp(ii).plusNanos(1000));
         }
      }
      
      Series actual = account.getPositionPnls(ojInstrument);
      for(int ii = 0; ii < actual.size(); ++ii) {
         LocalDateTime ldt = actual.getTimestamp(ii);
         if(ldt.isBefore(LocalDateTime.of(2013, 12, 31, 0, 0))) continue;
         if(ldt.isAfter(LocalDateTime.of(2014, 2, 14, 0, 0))) continue;
         assertEquals(expected.get(ldt, "Pos.Qty"), actual.get(ii, "quantity"), 1e-8);
         assertEquals(expected.get(ldt, "Pos.Value"), actual.get(ii, "value"), 1e-8);
         assertEquals(expected.get(ldt, "Pos.Avg.Cost"), actual.get(ii, "avg.cost"), 1e-8);
         assertEquals(expected.get(ldt, "Pos.Value"), actual.get(ii, "value"), 1e-8);
         assertEquals(expected.get(ldt, "Txn.Value"), actual.get(ii, "txn.value"), 1e-8);
         assertEquals(expected.get(ldt, "Pos.Value"), actual.get(ii, "value"), 1e-8);
         assertEquals(expected.get(ldt, "Period.Realized.PL"), actual.get(ii, "realized.pnl"), 1e-8);
         assertEquals(expected.get(ldt, "Period.Unrealized.PL"), actual.get(ii, "unrealized.pnl"), 1e-8);
         assertEquals(expected.get(ldt, "Gross.Trading.PL"), actual.get(ii, "gross.pnl"), 1e-8);
         assertEquals(expected.get(ldt, "Net.Trading.PL"), actual.get(ii, "net.pnl"), 1e-8);
         assertEquals(expected.get(ldt, "Txn.Fees"), actual.get(ii, "fees"), 1e-8);
      }
   }
}
