package net.tradelib.core;

import java.sql.SQLException;
import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.LocalDate;

public class Calendar {

   /**
    * @brief Get a LocalDate which identifies the week.
    * 
    * @param ld The input date
    * @param anchor The day of the week which identifies the week. For example,
    *               if anchor is MONDAY, the week's unique date is the Monday's
    *               date.
    * 
    * @return A date which is the same for all days of the week.
    * 
    * @throws Exception 
    */
   public static LocalDate toWeek(LocalDate ld, DayOfWeek anchor) throws Exception {
      DayOfWeek dow = ld.getDayOfWeek();
      LocalDate result = null;
      switch(dow) {
      case MONDAY:
         switch(anchor) {
            case MONDAY: result = ld; break;
            case TUESDAY: result = ld.plusDays(1); break;
            case WEDNESDAY: result = ld.plusDays(2); break;
            case THURSDAY: result = ld.plusDays(3); break;
            case FRIDAY: result = ld.plusDays(4); break;
            case SATURDAY: result = ld.plusDays(5); break;
            case SUNDAY: result = ld.plusDays(6); break;
         }
         break;
      case TUESDAY:
         switch(anchor) {
            case MONDAY: result = ld.minusDays(1); break;
            case TUESDAY: result = ld; break;
            case WEDNESDAY: result = ld.plusDays(1); break;
            case THURSDAY: result = ld.plusDays(2); break;
            case FRIDAY: result = ld.plusDays(3); break;
            case SATURDAY: result = ld.plusDays(4); break;
            case SUNDAY: result = ld.plusDays(5); break;
         }
         break;
      case WEDNESDAY:
         switch(anchor) {
            case MONDAY: result = ld.minusDays(2); break;
            case TUESDAY: result = ld.minusDays(1); break;
            case WEDNESDAY: result = ld; break;
            case THURSDAY: result = ld.plusDays(1); break;
            case FRIDAY: result = ld.plusDays(2); break;
            case SATURDAY: result = ld.plusDays(3); break;
            case SUNDAY: result = ld.plusDays(4); break;
         }
         break;
      case THURSDAY:
         switch(anchor) {
            case MONDAY: result = ld.minusDays(3); break;
            case TUESDAY: result = ld.minusDays(2); break;
            case WEDNESDAY: result = ld.minusDays(1); break;
            case THURSDAY: result = ld; break;
            case FRIDAY: result = ld.plusDays(1); break;
            case SATURDAY: result = ld.plusDays(2); break;
            case SUNDAY: result = ld.plusDays(3); break;
         }
         break;
      case FRIDAY:
         switch(anchor) {
            case MONDAY: result = ld.minusDays(4); break;
            case TUESDAY: result = ld.minusDays(3); break;
            case WEDNESDAY: result = ld.minusDays(2); break;
            case THURSDAY: result = ld.minusDays(1); break;
            case FRIDAY: result = ld; break;
            case SATURDAY: result = ld.plusDays(1); break;
            case SUNDAY: result = ld.plusDays(2); break;
         }
         break;
      case SATURDAY:
         switch(anchor) {
            case MONDAY: result = ld.minusDays(5); break;
            case TUESDAY: result = ld.minusDays(4); break;
            case WEDNESDAY: result = ld.minusDays(3); break;
            case THURSDAY: result = ld.minusDays(2); break;
            case FRIDAY: result = ld.minusDays(1); break;
            case SATURDAY: result = ld; break;
            case SUNDAY: result = ld.plusDays(1); break;
         }
         break;
     case SUNDAY:
         switch(anchor) {
            case MONDAY: result = ld.minusDays(6); break;
            case TUESDAY: result = ld.minusDays(5); break;
            case WEDNESDAY: result = ld.minusDays(4); break;
            case THURSDAY: result = ld.minusDays(3); break;
            case FRIDAY: result = ld.minusDays(2); break;
            case SATURDAY: result = ld.minusDays(1); break;
            case SUNDAY: result = ld; break;
         }
         break;
         
      default:
         throw new DateTimeException("Bad day of week.");
      }
      
      return result;
   }
   
   /**
    * @brief Get a LocalDate which identifies the week.
    * 
    * The week is anchored to Monday.
    * 
    * @param ld The input date
    *
    * @return A date which is the same for all days of the week.
    * 
    * @throws SQLException 
    */
   public static LocalDate toWeek(LocalDate ld) throws Exception {
      return toWeek(ld, DayOfWeek.MONDAY);
   }
}
