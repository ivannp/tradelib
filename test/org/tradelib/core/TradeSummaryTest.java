package org.tradelib.core;

import static org.junit.Assert.*;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.junit.Test;

public class TradeSummaryTest {

   @Test
   public void test() {
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
   }
}
