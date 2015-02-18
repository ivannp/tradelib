package org.tradelib.core;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class TimeSeries<T> {
   private ArrayList<LocalDateTime> index;
   private ArrayList<ArrayList<T>> data;
   
   private T defaultValue;
   
   public TimeSeries(int ncols) {
      index = new ArrayList<LocalDateTime>();
      data = new ArrayList<ArrayList<T>>();
      for(int ii = 0; ii < ncols; ++ii) {
         data.add(new ArrayList<T>());
      }
   }
   
   public TimeSeries(int ncols, int size) {
      index = new ArrayList<LocalDateTime>();
      data = new ArrayList<ArrayList<T>>();
      for(int ii = 0; ii < ncols; ++ii) {
         data.add(new ArrayList<T>(size));
      }
   }
   
   public TimeSeries() {
      this(1);
   }
   
   public TimeSeries(TimeSeries<T> ts) {
      index = new ArrayList<LocalDateTime>(ts.size());
      index.addAll(ts.getTimestamps());
      data = new ArrayList<ArrayList<T>>(ts.data.size());
      for(int ii = 0; ii < ts.data.size(); ++ii) {
         data.add(new ArrayList<T>(ts.data.get(ii)));
      }
   }
   
   public T get(int id) { return get(id, 0); }
   public T get(int id, int colId) { return data.get(colId).get(id); }
   
   public T get(LocalDateTime ts, int colId) {
      int rowId = Collections.binarySearch(index, ts);
      if(rowId < 0) {
         throw new BadIndexException(ts.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " is not in the index");
      }
      return data.get(colId).get(rowId);
   }
   
   public T get(LocalDateTime ts) { return get(ts, 0); }
   
   public void set(int id, int colId, T tt) { data.get(colId).set(id, tt); }
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
}
