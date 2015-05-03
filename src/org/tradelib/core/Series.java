package org.tradelib.core;

import java.io.FileReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;

import com.opencsv.CSVReader;

public class Series {
   private List<LocalDateTime> index;
   List<List<Double>> data;
   
   // The names of the data columns
   private HashMap<String, Integer> columnNames;
   
   static public Series fromDailyCsv(String path, boolean header) throws Exception {
      return fromCsv(path, header, DateTimeFormatter.ofPattern("yyyy-MM-dd"), LocalTime.of(17, 0));
   }

   static public Series fromCsv(String path, boolean header, DateTimeFormatter dtf) throws Exception {
      return fromCsv(path, header, dtf, null);
   }
   
   static public Series fromCsv(String path, boolean header, DateTimeFormatter dtf, LocalTime lt) throws Exception {
      CSVReader csv = new CSVReader(new FileReader(path));
      String [] line = csv.readNext();
      if(line == null) return null;
      
      int ncols = line.length - 1;
      
      Series result = new Series(ncols);
      
      if(header) {
         result.setNames(Arrays.copyOfRange(line, 1, line.length));
         line = csv.readNext();
         if(line == null) {
            // Only a header
            return result;
         }
      }
      
      if(dtf == null) {
         if(lt == null) dtf = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
         else dtf = DateTimeFormatter.ISO_DATE;
      }
      
      double [] values = new double[ncols];
      for(; line != null; line = csv.readNext()) {
         for(int ii = 0; ii < ncols; ++ii) {
            values[ii] = Double.parseDouble(line[ii + 1]);
         }

         LocalDateTime ldt;
         if(lt != null) {
            ldt = LocalDate.parse(line[0], dtf).atTime(lt);
         } else {
            ldt = LocalDateTime.parse(line[0], dtf);
         }
         
         result.append(ldt, values);
      }
      
      return result;
   }
   
   public Series(int ncols) {
      index = new ArrayList<LocalDateTime>();
      data = new ArrayList<List<Double>>(ncols);
      for(int ii = 0; ii < ncols; ++ii) {
         data.add(new ArrayList<Double>());
      }
      columnNames = new HashMap<String, Integer>();
   }
   
   public double get(int rowId, int colId) {
      return data.get(colId).get(rowId);
   }
   
   public double get(int rowId) {
      return get(rowId, 0);
   }
   
   public double get(int rowId, String colName) {
      return get(rowId, columnNames.get(colName));
   }
   
   public LocalDateTime getTimestamp(int rowId) {
      return index.get(rowId);
   }
   
   public double get(LocalDateTime ts, int colId) {
      int rowId = Collections.binarySearch(index, ts);
      if(rowId < 0) {
         throw new BadIndexException(ts.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " is not in the index");
      }
      return data.get(colId).get(rowId);
   }
   
   public double get(LocalDateTime ts, String colName) {
      return get(ts, columnNames.get(colName));
   }
   
   public void set(int row, double value) {
      data.get(0).set(row, value);
   }
   
   public void set(int row, int col, double value) {
      data.get(col).set(row, value);
   }
   
   public void set(int row, LocalDateTime ts) {
      index.set(row, ts);
   }
   
   public void set(int row, LocalDateTime ts, double ...args) {
      set(row, ts);
      for(int ii = 0; ii < args.length; ++ii) {
         set(row, ii, args[ii]);
      }
   }
   
   public void setNames(String ...names) {
      columnNames.clear();
      for(int ii = 0; ii < names.length; ++ii) {
         columnNames.put(names[ii], ii);
      }
   }
   
   public int size() {
      return index.size();
   }
   
   public void append(LocalDateTime ts, double ...args) {
      index.add(ts);
      int endLoop = Math.min(args.length, data.size());
      for(int ii = 0; ii < endLoop; ++ii) {
         data.get(ii).add(args[ii]);
      }
      for(int ii = endLoop; ii < data.size(); ++ii) {
         data.get(ii).add(args[args.length - 1]);
      }
   }
   
   public Series head(int rows) {
      if(rows < 0) {
         if(Math.abs(rows) > size()) return null;
         rows = size() + rows;
      } else if(rows > size()){
         return this;
      }
      Series result = new Series();
      result.index = index.subList(0, rows);
      result.data = new ArrayList<List<Double>>(data.size());
      for(int ii = 0; ii < data.size(); ++ii) {
         result.data.add(data.get(ii).subList(0, rows));
      }
      if(columnNames != null) {
         result.columnNames = new HashMap<String, Integer>(columnNames);
      }
      return result;
   }
   
   public Series tail(int rows) {
      if(rows < 0) rows = size() + rows;
      Series result = new Series();
      result.index = index.subList(size() - rows, size());
      result.data = new ArrayList<List<Double>>(data.size());
      for(int ii = 0; ii < data.size(); ++ii) {
         result.data.add(data.get(ii).subList(size() - rows, size()));
      }
      if(columnNames != null) {
         result.columnNames = new HashMap<String, Integer>(columnNames);
      }
      return result;
   }
   
   private TreeMap<LocalDateTime,Integer> buildDailyIndex() {
      TreeMap<LocalDateTime,Integer> result = new TreeMap<LocalDateTime, Integer>();
      int ii = 0;
      while(ii < size()) {
         LocalDate ld = index.get(ii).toLocalDate();
         int jj = ii + 1;
         for(; jj < size(); ++jj) {
            if(!index.get(jj).toLocalDate().equals(ld)) {
               break;
            }
         }
         --jj;
         
         result.put(ld.atStartOfDay(), jj - ii + 1);
         
         ii = jj + 1;
      }
         
      return result;
   }
   
   public Series toDaily(BinaryOperator<Double> accumulator) {
      return toDaily(0.0, accumulator);
   }
   
   public Series toDaily(double identity, BinaryOperator<Double> accumulator) {
      TreeMap<LocalDateTime,Integer> newIndex = buildDailyIndex();
      Series result = new Series();
      int numUnique = newIndex.size();
      result.index = new ArrayList<LocalDateTime>(numUnique);
      result.data = new ArrayList<List<Double>>(data.size());
      for(int ii = 0; ii < data.size(); ++ii) {
         result.data.add(new ArrayList<Double>(newIndex.size()));
      }
      
      int currentIndex = 0;
      Iterator<Map.Entry<LocalDateTime, Integer>> iter = newIndex.entrySet().iterator();
      while(iter.hasNext()) {
         Map.Entry<LocalDateTime, Integer> entry = iter.next();
         result.append(entry.getKey(), 0.0);
         int lastIndex = result.size() - 1;
         for(int jj = 0; jj < data.size(); ++jj) {
            result.set(lastIndex, data.get(jj).subList(currentIndex, currentIndex + entry.getValue()).stream().reduce(identity, accumulator));
         }
         currentIndex += entry.getValue();
//         result.append(entry.getKey(), 0.0);
//         int lastIndex = result.size() - 1;
//         for(int ii = 0; ii < entry.getValue(); ++ii, ++currentIndex) {
//            for(int jj = 0; jj < data.size(); ++jj) {
//               result.set(lastIndex, jj, result.get(lastIndex, jj) + get(currentIndex, jj));
//            }
//         }
      }
      
      if(columnNames != null) {
         result.columnNames = new HashMap<String, Integer>(columnNames);
      }
      
      return result;
   }
   
   public void print(DateTimeFormatter dtf) {
      for(int ii = 0; ii < size(); ++ii) {
         System.out.print(index.get(ii).format(dtf) + ": ");
         System.out.format("%,.2f", data.get(0).get(ii));
         for(int jj = 1; jj < data.size(); ++jj) {
            System.out.format(", %,.2f", data.get(jj).get(ii));
         }
         System.out.println();
      }
   }
   
   public void print() {
      print(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
   }
   
   public void print(String pattern) {
      print(DateTimeFormatter.ofPattern(pattern));
   }
   
   private Series() {
   }
   
   public int columns() {
      return data.size();
   }
}
 