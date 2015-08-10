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

import static org.junit.Assert.*;

/*
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
*/
import org.junit.Test;

public class TradeSummaryTest {

   @Test
   public void test() {
      /*
      Configuration configuration = new Configuration();
      StandardServiceRegistryBuilder builder = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties());
      SessionFactory factory = configuration.buildSessionFactory(builder.build());
      Session session = factory.openSession();
      Transaction txn = null;
      try {
         txn = session.beginTransaction();
         txn.commit();
      } catch(HibernateException e) {
         if(txn != null) txn.rollback();
         e.printStackTrace();
      } finally {
         session.close();
      }
      */
   }
}
