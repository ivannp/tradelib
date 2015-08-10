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

import java.time.LocalDateTime;

public class PositionPnl {
   public LocalDateTime ts;
   public double positionQuantity;
   public double positionValue;
   public double positionAverageCost;
   public double transactionValue;
   public double realizedPnl;
   public double unrealizedPnl;
   public double grossPnl;
   public double netPnl;
   public double fees;
   
   public PositionPnl(LocalDateTime ldt) {
      positionQuantity = 0.0;
      positionValue = 0.0;
      positionAverageCost = 0.0;
      transactionValue = 0.0;
      realizedPnl = 0.0;
      unrealizedPnl = 0.0;
      grossPnl = 0.0;
      netPnl = 0.0;
      fees = 0.0;
      
      ts = ldt;
   }
   
   public PositionPnl() {
      this(LocalDateTime.MIN);
   }
}
