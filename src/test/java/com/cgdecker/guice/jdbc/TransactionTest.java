package com.cgdecker.guice.jdbc;

import com.google.inject.persist.Transactional;
import org.junit.Test;

import java.sql.Connection;

import javax.inject.Inject;
import javax.inject.Provider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * @author cgdecker@gmail.com (Colin Decker)
 */
public class TransactionTest extends AbstractPersistTest {

  @Test public void unitOfWorkPerTransactionIfNotStartedManually() {
    TransactionConnectionGetter transactionalObj = injector.getInstance(TransactionConnectionGetter.class);
    Connection conn =  transactionalObj.getConnection();
    Connection conn2 = transactionalObj.getConnection();
    assertFalse("conn from first transactional call same as conn from second", conn == conn2);
  }

  @Test public void unitOfWorkSharedIfStartedManually() {
    TransactionConnectionGetter transactionalObj = injector.getInstance(TransactionConnectionGetter.class);
    getUnitOfWork().begin();
    Connection conn =  transactionalObj.getConnection();
    Connection conn2 = transactionalObj.getConnection();
    getUnitOfWork().end();
    assertEquals("conn from first transactional call was not same as conn from second", conn, conn2);
  }

  public static class TransactionConnectionGetter {
    private final Provider<Connection> connectionProvider;

    @Inject public TransactionConnectionGetter(Provider<Connection> connectionProvider) {
      this.connectionProvider = connectionProvider;
    }

    @Transactional
    public Connection getConnection() {
      return connectionProvider.get();
    }
  }
}
