package com.cgdecker.guice.jdbc;

import com.google.inject.persist.PersistModule;
import org.aopalliance.intercept.MethodInterceptor;

/**
 * @author cgdecker@gmail.com (Colin Decker)
 */
public class JdbcPersistModule extends PersistModule {
  @Override protected void configurePersistence() {
  }

  @Override protected MethodInterceptor getTransactionInterceptor() {
    return null;
  }
}
