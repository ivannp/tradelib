package org.tradelib.core;

import static org.junit.Assert.*;

import java.io.FileReader;
import java.math.BigDecimal;
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
      
      TimeSeries<Double> es = fromCsv("test/data/es.csv", false);
      
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
      
      double initialEquity = 150000.0;
      double contractFees = -1.13;
      
      Instrument esInstrument = Instrument.makeFuture("ES", new BigDecimal("0.25"), new BigDecimal("50"));
      Instrument ojInstrument = Instrument.makeFuture("OJ", new BigDecimal("0.05"), new BigDecimal("150"));
      Instrument audInstrument = Instrument.makeFuture("AUD", new BigDecimal("0.01"), new BigDecimal("1000"));
      
      TimeSeries<Double> es = fromCsv("test/data/es.csv", false);
      TimeSeries<Double> oj = fromCsv("test/data/oj.csv", false);
      TimeSeries<Double> aud = fromCsv("test/data/aud.csv", false);
      
      Account account = new Account(LocalDateTime.of(2013, 12, 26, 0, 0, 0), initialEquity);
      
      // Mark the portfolio with a price - must happen before a transaction
      account.mark(esInstrument, LocalDateTime.of(2013, 12, 27, 17, 0, 0), 1829.50);
      account.mark(ojInstrument, LocalDateTime.of(2013, 12, 27, 17, 0, 0), 145.00);
      account.mark(audInstrument, LocalDateTime.of(2013, 12, 27, 17, 0, 0), 85.51);
      
      account.mark(esInstrument, LocalDateTime.of(2013, 12, 30, 17, 0, 0), 1827.75);
      account.mark(ojInstrument, LocalDateTime.of(2013, 12, 30, 17, 0, 0), 147.75);
      account.mark(audInstrument, LocalDateTime.of(2013, 12, 30, 17, 0, 0), 85.98);
      
      account.mark(esInstrument, LocalDateTime.of(2013, 12, 31, 17, 0, 0), 1834.00);
      account.mark(ojInstrument, LocalDateTime.of(2013, 12, 31, 17, 0, 0), 145.95);
      account.mark(audInstrument, LocalDateTime.of(2013, 12, 31, 17, 0, 0), 86.13);
      
      account.mark(esInstrument, LocalDateTime.of(2014, 1, 2, 17, 0, 0), 1819.50);
      account.mark(ojInstrument, LocalDateTime.of(2014, 1, 2, 17, 0, 0), 144.70);
      account.mark(audInstrument, LocalDateTime.of(2014, 1, 2, 17, 0, 0), 85.95);
   }
}

