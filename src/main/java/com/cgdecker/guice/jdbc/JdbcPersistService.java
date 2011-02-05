package com.cgdecker.guice.jdbc;

import com.google.inject.Provider;
import com.google.inject.persist.PersistService;
import com.google.inject.persist.UnitOfWork;

import java.sql.Connection;
import java.sql.SQLException;

import javax.inject.Inject;
import javax.sql.DataSource;

/**
 * @author cgdecker@gmail.com (Colin Decker)
 */
class JdbcPersistService implements Provider<Connection>, PersistService, UnitOfWork {
  private final DataSource dataSource;
  private final ThreadLocal<Connection> connectionThreadLocal = new ThreadLocal<Connection>();

  private final ThreadLocal<Integer> beginCountThreadLocal = new ThreadLocal<Integer>();

  @Inject JdbcPersistService(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public void start() {
    /*
     * Nothing to do here really. We're injecting the DataSource this uses directly, not creating it
     * when the service starts, and there isn't any concept of "closing" the DataSource itself.
     */
  }

  public void stop() {
    // Nothing to do here either. Some DataSources have a close() method, but it's not part of the
    // interface so....
  }

  public void begin() {
    if (!isWorking()) {
      connectionThreadLocal.set(getConnection());
      beginCountThreadLocal.set(1);
    } else {
      beginCountThreadLocal.set(beginCountThreadLocal.get() + 1);
    }
  }

  public void end() {
    Integer beginCount = beginCountThreadLocal.get();
    if (beginCount == null)
      return;

    beginCountThreadLocal.set(--beginCount);

    if (beginCount == 0) {
      closeConnection(connectionThreadLocal.get());
      connectionThreadLocal.remove();
      beginCountThreadLocal.remove();
    }
  }

  /**
   * @return {@code true} if work is currently active on the current thread; {@code false} otherwise.
   */
  public boolean isWorking() {
    return connectionThreadLocal.get() != null;
  }

  public Connection get() {
    Connection result = connectionThreadLocal.get();
    if (result == null) {
      throw new IllegalStateException("No UnitOfWork active while attempting to retrieve Connection. " +
          "Be sure to call UnitOfWork.begin() before retrieving a Connection or that you retrieve " +
          "the Connection in a @Transactional method.");
    }
    return result;
  }

  private Connection getConnection() {
    try {
      return dataSource.getConnection();
    } catch (SQLException e) {
      throw new JdbcException(e);
    }
  }

  private void closeConnection(Connection conn) {
    try {
      conn.close();
    } catch (SQLException e) {
      throw new JdbcException(e);
    }
  }
}
