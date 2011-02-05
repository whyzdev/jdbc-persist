package com.cgdecker.guice.jdbc;

import com.google.inject.Scopes;
import com.google.inject.persist.PersistModule;
import com.google.inject.persist.PersistService;
import com.google.inject.persist.Transactional;
import com.google.inject.persist.UnitOfWork;
import org.aopalliance.intercept.MethodInterceptor;

import java.sql.Connection;

import javax.sql.DataSource;

import static com.google.inject.matcher.Matchers.annotatedWith;
import static com.google.inject.matcher.Matchers.any;

/**
 * JDBC provider for guice-persist. Requires a binding for a {@link DataSource}.
 *
 * @author cgdecker@gmail.com (Colin Decker)
 */
public class JdbcPersistModule extends PersistModule {
  private final JdbcTransactionInterceptor transactionInterceptor = new JdbcTransactionInterceptor();

  @Override protected void configurePersistence() {
    requireBinding(DataSource.class);

    bind(JdbcPersistService.class).in(Scopes.SINGLETON);

    bind(PersistService.class).to(JdbcPersistService.class);
    bind(UnitOfWork.class).to(JdbcPersistService.class);
    bind(Connection.class).toProvider(JdbcPersistService.class);

    requestInjection(transactionInterceptor);

    // TODO: Remove this once issue 525 is fixed in Guice.
    bindInterceptor(annotatedWith(Transactional.class), any(), transactionInterceptor);
  }

  @Override protected MethodInterceptor getTransactionInterceptor() {
    return transactionInterceptor;
  }
}
