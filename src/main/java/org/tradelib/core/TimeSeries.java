// Copyright 2015 by Ivan Popivanov
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

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.xml.sax.HandlerBase;

public class TimeSeries<T> {
   private ArrayList<LocalDateTime> index;
   private ArrayList<ArrayList<T>> data;

   // The names of the data columns
   private HashMap<String, Integer> columnNames;
   
   private T defaultValue;
   
   public TimeSeries(int ncols) {
      index = new ArrayList<LocalDateTime>();
      data = new ArrayList<ArrayList<T>>();
      for(int ii = 0; ii < ncols; ++ii) {
         data.add(new ArrayList<T>());
      }
      
      columnNames = new HashMap<String, Integer>();
   }
   
   public TimeSeries(int ncols, int size) {
      index = new ArrayList<LocalDateTime>();
      data = new ArrayList<ArrayList<T>>();
      for(int ii = 0; ii < ncols; ++ii) {
         data.add(new ArrayList<T>(size));
      }
      
      columnNames = new HashMap<String, Integer>();
   }
   
   public TimeSeries() {
      this(1);
   }
   
   public TimeSeries(String ...columnNames) {
      this(columnNames.length);
      setNames(columnNames);
   }
   
   public TimeSeries(TimeSeries<T> ts) {
      index = new ArrayList<LocalDateTime>(ts.size());
      index.addAll(ts.getTimestamps());
      data = new ArrayList<ArrayList<T>>(ts.data.size());
      for(int ii = 0; ii < ts.data.size(); ++ii) {
         data.add(new ArrayList<T>(ts.data.get(ii)));
      }
      
      columnNames = new HashMap<String, Integer>();
      columnNames.putAll(ts.columnNames);
   }
   
   public TimeSeries(ArrayList<LocalDateTime> ts, ArrayList<T> ...args) {
      index = new ArrayList<LocalDateTime>(ts);
      data = new ArrayList<ArrayList<T>>(args.length);
      for(int ii = 0; ii < args.length; ++ii) {
         data.add(args[ii]);
      }
      
      columnNames = new HashMap<String, Integer>();
   }
   
   public T get(int id) { return get(id, 0); }
   public T get(int id, int colId) { return data.get(colId).get(id); }
   public T get(int id, String colName) { return data.get(columnNames.get(colName)).get(id); }
   
   public T get(LocalDateTime ts, int colId) {
      int rowId = Collections.binarySearch(index, ts);
      if(rowId < 0) {
         throw new BadIndexException(ts.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " is not in the index");
      }
      return data.get(colId).get(rowId);
   }
   public T get(LocalDateTime ts, String colName) {
      return get(ts, columnNames.get(colName));
   }
   
   public T get(String ts, int colId) {
      return get(dateTimeFromString(ts), colId);
   }
   
   public T get(String ts, String colName) {
      return get(ts, columnNames.get(colName));
   }
   
   public T get(String ts) {
      return get(ts, 0);
   }
   
   public T getLast(int colId) {
      return data.get(colId).get(index.size() - 1);
   }
   
   public T getLast(String colName) {
      return getLast(columnNames.get(colName));
   }
   
   public T getLast() {
      return getLast(0);
   }
   
   public T get(LocalDateTime ts) { return get(ts, 0); }
   
   public TimeSeries<T> getData(int colId) {
      return new TimeSeries<T>(index, data.get(colId));
   }
   
   public TimeSeries<T> columns(String colname) {
      return getData(columnNames.get(colname));
   }
   
   public void set(int id, int colId, T tt) { data.get(colId).set(id, tt); }
   public void set(int id, String colName, T tt) { set(id, columnNames.get(colName), tt); }
   public void set(int id, T tt) { set(id, 0, tt); }
   public void set(LocalDateTime ldt, int colId, T tt) {
      int rowId = Collections.binarySearch(index, ldt);
      if(rowId < 0) {
         throw new BadIndexException(ldt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " is not in the index");
      }
      set(rowId, colId, tt);
   }
   public void set(LocalDateTime ldt, T tt) { set(ldt, 0, tt); }

   public void add(LocalDateTime ts, T tt) {
      if(index.size() > 0 && !ts.isAfter(index.get(index.size()-1))) {
         throw new BadIndexException(ts.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " is not in chronological order " +
                     "last timestamp = " + index.get(index.size()-1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
      }
      index.add(ts);
      for(int ii = 0; ii < data.size(); ++ii) {
         data.get(ii).add(tt);
      }
   }
   
   public void add(LocalDateTime ts, T ... args) {
      if(index.size() > 0 && !ts.isAfter(index.get(index.size()-1))) {
         throw new BadIndexException(ts.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " is not in chronological order " +
                     "last timestamp = " + index.get(index.size()-1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
      }
      index.add(ts);
      for(int ii = 0; ii < data.size(); ++ii) {
         if(ii < args.length) data.get(ii).add(args[ii]);
         else data.get(ii).add(defaultValue);
      }
   }
   
   public void addAll(List<LocalDateTime> ldts, T value) {
      for(LocalDateTime ldt : ldts) {
         add(ldt, value);
      }
   }
   
   public int size() { return index.size(); }
   
   public List<LocalDateTime> getTimestamps() { return index; }
   // Returns the subList [0, end) - the "end" element is not included
   public List<LocalDateTime> getTimestamps(int end) { return index.subList(0, end); }
// Returns the subList [beg, end) - the "end" element is not included
   public List<LocalDateTime> getTimestamps(int beg, int end) { return index.subList(beg, end); }
   public LocalDateTime getTimestamp(int id) { return index.get(id); }
   public LocalDateTime getLastTimestamp() { return index.get(index.size() - 1); }
   
   public TimeSeries<T> subset(LocalDateTime from, LocalDateTime to) {
      int fromId = Collections.binarySearch(index, from);
      if(fromId < 0) {
         // The element is not in the list
         fromId = Math.abs(fromId + 1);
      }
      
      int toId = Collections.binarySearch(index, to);
      if(toId < 0) {
         // The element is not in the list
         toId = Math.abs(toId + 1);
      }
      
      TimeSeries<T> result = new TimeSeries<T>(data.size());
      for(int row = fromId; row <= toId; ++row) {
         result.index.add(index.get(row));
         for(int col = 0; col < data.size(); ++col) {
            result.data.get(col).add(data.get(col).get(row));
         }
      }
      
      result.columnNames.putAll(columnNames);
      
      return result;
   }
   
   private LocalDateTime dateTimeFromString(String str) {
      LocalDateTime ldt = null;
      try {
         ldt = LocalDateTime.parse(str);
      } catch(Exception e) {
         ldt = null;
      }
      
      if(ldt == null) {
         try {
            ldt = LocalDate.parse(str).atStartOfDay();
         } catch(Exception e) {
         }
      }
      
      return ldt;
   }
   
   public void setNames(String ...names) {
      columnNames.clear();
      for(int ii = 0; ii < names.length; ++ii) {
         columnNames.put(names[ii], ii);
      }
   }
   
   public TimeSeries<T> subset(String from, String to) {
      
      LocalDateTime ldtFrom = dateTimeFromString(from);
      LocalDateTime ldtTo = ldtFrom != null ? dateTimeFromString(to) : null;
      
      if(ldtTo == null || ldtFrom == null) return null;
      
      return subset(ldtFrom, ldtTo);
   }
   
   public TimeSeries<T> subset(int year) {
      TimeSeries<T> result = new TimeSeries<T>(data.size());
      for(int row = 0; row <= size(); ++row) {
         if(index.get(row).getYear() != year) continue;
         result.index.add(index.get(row));
         for(int col = 0; col < data.size(); ++col) {
            result.data.get(col).add(data.get(col).get(row));
         }
      }
      
      result.columnNames.putAll(columnNames);
      
      return result;
   }
}
