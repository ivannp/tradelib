package net.tradelib.ratio;

import java.util.List;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

public class SharpeRatio {
   /**
    * @brief Computes the Sharpe ratio for a list of returns.
    * 
    * @param returns The returns
    * @param rf The risk free average return
    * 
    * @return The Sharpe ratio
    */
   public static double value(List<Double> returns, double rf) {
      SummaryStatistics ss = new SummaryStatistics();
      returns.forEach((xx) -> ss.addValue(xx - rf));
      
      return ss.getMean() / ss.getStandardDeviation();
   }
   
   public static double value(List<Double> returns) {
      return value(returns, 0);
   }
}
