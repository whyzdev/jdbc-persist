package com.cgdecker.guice.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author cgdecker@gmail.com (Colin Decker)
 */
class JdbcTransaction {
  private final Connection connection;

  JdbcTransaction(Connection connection) {
    this.connection = connection;
  }

  public void begin() {
    if (connection != null) {
      try {
        connection.setAutoCommit(false);
      } catch (SQLException e) {
        throw new JdbcException(e);
      }
    }
  }

  public void commit() {
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
    try {
      return !connection.getAutoCommit();
    } catch (SQLException e) {
      throw new JdbcException(e);
    }
  }
}
