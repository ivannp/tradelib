package net.tradelib.misc;

import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public final class Utils {
   
   public static void setupDefaultLogging() throws Exception {
      setupLogging(true, "diag.log");
   }

   public static void setupLogging(boolean logToConsole, String logFile) throws Exception {
      // Setup the logging
      System.setProperty(
               "java.util.logging.SimpleFormatter.format",
               "%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS: %4$s: %5$s%n%6$s%n");
      LogManager.getLogManager().reset();
      Logger rootLogger = Logger.getLogger("");
      if(logFile != null) {
         FileHandler logHandler = new FileHandler(logFile, 8*1024*1024, 2, true);
         logHandler.setFormatter(new SimpleFormatter()); 
         logHandler.setLevel(Level.FINEST); 
         rootLogger.addHandler(logHandler);
      }
      
      if(logToConsole) {
         ConsoleHandler consoleHandler = new ConsoleHandler();
         consoleHandler.setFormatter(new SimpleFormatter());
         consoleHandler.setLevel(Level.INFO);
         rootLogger.addHandler(consoleHandler);
      }
       
      rootLogger.setLevel(Level.INFO);
   }
   
}
