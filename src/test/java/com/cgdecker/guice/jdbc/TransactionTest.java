package com.cgdecker.guice.jdbc;

import com.google.inject.persist.Transactional;
import org.junit.Test;

import java.sql.Connection;

import javax.inject.Provider;

/**
 * @author cgdecker@gmail.com (Colin Decker)
 */
public class TransactionTest extends AbstractPersistTest {

  @Test public void unitOfWorkPerTransactionIfNotStartedManually() {

  }

  public static class TransactionConnectionGetter {
    private final Provider<Connection> connectionProvider;

    public TransactionConnectionGetter(Provider<Connection> connectionProvider) {
      this.connectionProvider = connectionProvider;
    }

    @Transactional
    public Connection getConnection() {
      return connectionProvider.get();
    }
  }
}
