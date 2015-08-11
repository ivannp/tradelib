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

package net.tradelib.core;

import java.time.LocalDateTime;

public class Position {
   public long quantity;
   public LocalDateTime since;
   
   public Position(long qq, LocalDateTime ldt) {
      quantity = qq;
      since = ldt;
   }
   
   public Position() {
      quantity = 0;
      since = LocalDateTime.MIN;
   }
}
