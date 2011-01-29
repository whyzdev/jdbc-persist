package com.cgdecker.guice.jdbc;

import com.google.inject.Provider;
import com.google.inject.ProvisionException;
import com.google.inject.persist.Transactional;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

/**
 * Manages a single {@link Connection} and its transaction state. This class is not thread-safe and
 * each instance is intended to be used from a single thread. The primary reason for this wrapper is
 * so that we can "start" a transaction without having to actually retrieve the {@code Connection}
 * from the {@link DataSource}. In cases where a high level class is annotated with
 * {@code @Transactional}, a method might start a transaction but no work be done
 *
 * @author cgdecker@gmail.com (Colin Decker)
 */
class JdbcConnectionManager implements Provider<Connection> {
  private final DataSource dataSource;
  private Connection connection;
  private final JdbcTransaction transaction = new JdbcTransactionImpl();

  JdbcConnectionManager(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  /**
   * Gets the {@code Connection} object this is managing. The {@link java.sql.Connection#close()}
   * method should not be called on this connection, nor should any transaction-related methods.
   * Transactions should be managed through the use of the
   * {@link com.google.inject.persist.Transactional @Transactional} annotation.
   *
   * @return the managed connection.
   * @throws JdbcException if an error occurs getting the connection from the data source.
   */
  public Connection get() {
    System.out.println("get connection");
    if (connection == null) {
      try {
        connection = dataSource.getConnection();
        if (transaction.isActive() && connection.getAutoCommit())
          connection.setAutoCommit(false);
      } catch (SQLException e) {
        throw new JdbcException(e);
      }
    }
    return connection;
  }

  /**
   * Closes the managed connection, if it was ever used.
   */
  public void close() {
    if (connection != null) {
      try {
        connection.close();
      } catch (SQLException e) {
        throw new JdbcException(e);
      }
    }
  }

  /**
   * Gets a transaction on the managed connection. The transaction is initially not active.
   *
   * @return an object through which the connection's transaction state can be queried and controlled.
   */
  public JdbcTransaction getTransaction() {
    return transaction;
  }

  private class JdbcTransactionImpl implements JdbcTransaction {
    private boolean active;

    public void begin() {
      active = true;
      if (connection != null) {
        try {
          connection.setAutoCommit(false);
        } catch (SQLException e) {
          throw new JdbcException(e);
        }
      }
    }

    public void commit() {
      active = false;
      if (connection != null) {
        try {
          connection.commit();
          connection.setAutoCommit(true);
        } catch (SQLException e) {
          throw new JdbcException(e);
        }
      }
    }

    public void rollback() {
      active = false;
      if (connection != null) {
        try {
          connection.rollback();
          connection.setAutoCommit(true);
        } catch (SQLException e) {
          throw new JdbcException(e);
        }
      }
    }

    public boolean isActive() {
      return active;
    }
  }
}
