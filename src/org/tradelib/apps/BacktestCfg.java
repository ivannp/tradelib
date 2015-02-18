package org.tradelib.apps;

import java.io.FileInputStream;
import java.util.Properties;

public class BacktestCfg {
   private static BacktestCfg instance = null;

   private Properties properties = null; 
   
   public synchronized static BacktestCfg instance() {
      if(instance == null) {
         instance = new BacktestCfg();
      }
      return instance;
   }
   
   public BacktestCfg() {
      properties = new Properties();
      try {
         FileInputStream in = new FileInputStream("config/application.properties");
         properties.load(in);
         in.close();
         in = new FileInputStream("config/strategy.properties");
         properties.load(in);
         in.close();
      } catch (Exception e) {
         e.printStackTrace();
      }
   }
   
   public String getProperty(String key) {
      return properties.getProperty(key);
   }
   
   public String getProperty(String key, String defaultValue) {
      return properties.getProperty(key, defaultValue);
   }
}
