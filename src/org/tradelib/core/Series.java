package org.tradelib.core;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.nd4j.linalg.api.buffer.DataBuffer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.NDArrayFactory;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.jblas.NDArray;

import com.opencsv.CSVReader;

public class Series {
   private List<LocalDateTime> index;
   private INDArray data;
   
   private static final int MAX_ROWS = 1000;
   
   // The names of the data columns
   private HashMap<String, Integer> columnNames;
   
   private INDArray array(int nrows, int ncols) {
      Nd4j.dtype = DataBuffer.DOUBLE;
      Nd4j.ORDER = NDArrayFactory.FORTRAN;
      return Nd4j.create(nrows, ncols);
   }
   
   static public Series fromCsv(String path, boolean header, DateTimeFormatter dtf, LocalTime lt) throws Exception {
      CSVReader csv = new CSVReader(new FileReader(path));
      String [] line = csv.readNext();
      if(line == null) return null;
      
      int ncols = line.length - 1;
      
      String [] colNames = null;
      
      if(header) {
         colNames = Arrays.copyOfRange(line, 1, line.length);
         line = csv.readNext();
         if(line == null) {
            // Only a header
            Series result = new Series(ncols);
            if(colNames != null) result.setNames(colNames);
            return result;
         }
      }
      
      if(dtf == null) {
         if(lt == null) dtf = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
         else dtf = DateTimeFormatter.ISO_DATE;
      }
      
      ArrayList<LocalDateTime> index = new ArrayList<LocalDateTime>();
      ArrayList<ArrayList<Double>> data = new ArrayList<ArrayList<Double>>();
      for(int ii = 0; ii < ncols; ++ii) {
         data.add(new ArrayList<Double>());
      }
      
      for(; line != null; line = csv.readNext()) {
         for(int ii = 0; ii < ncols; ++ii) {
            data.get(ii).add(Double.parseDouble(line[ii + 1]));
         }
         if(lt != null) {
            index.add(LocalDate.parse(line[0], dtf).atTime(lt));
         } else {
            index.add(LocalDateTime.parse(line[0], dtf));
         }
      }
      
      Series result = new Series(index.size(), data.size());
      for(int row = 0; row < index.size(); ++row) {
         for(int col = 0; col < data.size(); ++col) {
            result.set(row, col, data.get(col).get(row));
         }
         result.set(row, index.get(row));
      }
      
      if(colNames != null) result.setNames(colNames);
      
      return result;
   }
   
   public Series(int ncols) {
      index = new ArrayList<LocalDateTime>();
      data = array(0, ncols);
      columnNames = new HashMap<String, Integer>();
   }
   
   public Series(int nrows, int ncols) {
      index = new ArrayList<LocalDateTime>();
      for(int ii = 0; ii < nrows; ++ii) {
         index.add(LocalDateTime.MIN);
      }

      data = array(nrows, ncols);
      
      columnNames = new HashMap<String, Integer>();
   }
   
   public double get(LocalDateTime ts, int colId) {
      int rowId = Collections.binarySearch(index, ts);
      if(rowId < 0) {
         throw new BadIndexException(ts.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " is not in the index");
      }
      return data.getDouble(rowId, colId);
   }
   
   public double get(LocalDateTime ts, String colName) {
      return get(ts, columnNames.get(colName));
   }
   
   public void set(int row, int col, double value) {
      data.put(row, col, value);
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
   
   public void rbindi(LocalDateTime ts, INDArray row) {
      index.add(ts);
      data = Nd4j.vstack(data, row);
   }
   
   public Series rbindi(List<LocalDateTime> ts, INDArray rows) {
      index.addAll(ts);
      data = Nd4j.vstack(data, rows);
      return this;
   }
   
   public void rbindi(Series other) {
      index.addAll(other.index);
      data = Nd4j.vstack(data, other.data);
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
   
   public Series headi(int rows) {
      index.subList(rows, index.size()).clear();
      data = data.reshape(rows, data.columns());
      return this;
   }
}
