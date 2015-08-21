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

package net.tradelib.misc;

import java.beans.PropertyEditorSupport;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class CustomLocalDateTimeEditor extends PropertyEditorSupport {
   
   public CustomLocalDateTimeEditor() {
   }
   
   private LocalDateTime parseText(String text) {
      LocalDateTime ldt;
      try {
         ldt = LocalDateTime.parse(text);
      } catch(Exception ee) {
         ldt = null;
      }
      
      if(ldt == null) {
         try {
            ldt = LocalDateTime.of(LocalDate.parse(text), LocalTime.of(0, 0));
         } catch(Exception ee) {
            ldt = null;
         }
      }
      
      return ldt;
   }
   
   @Override
   public void setAsText(String text) throws IllegalArgumentException {
      setValue(parseText(text));
   }

   @Override
   public String getAsText() {
       LocalDateTime value = parseText(String.valueOf(getValue()));
       return (value != null ? value.toString() : "");
   }

}
