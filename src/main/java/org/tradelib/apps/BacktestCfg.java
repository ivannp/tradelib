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

package org.tradelib.apps;

import java.io.FileInputStream;
import java.nio.file.Paths;
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
