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

package org.tradelib.core;

public interface IBrokerListener {
   public void barOpenHandler(Bar bar) throws Exception;
   public void barCloseHandler(Bar bar) throws Exception;
   public void barClosedHandler(Bar bar) throws Exception;
   
   public void orderExecutedHandler(OrderNotification on) throws Exception;
}
