package com.cgdecker.guice.jdbc;

import java.sql.SQLException;

/**
 * An unchecked wrapper for any {@link SQLException}s that occur in the process of managing
 * {@link java.sql.Connection Connection}s or transactions. The {@code SQLException} that occurred
 * can be retrieved with {@code getCause()}.
 *
 * @author cgdecker@gmail.com (Colin Decker)
 */
public class JdbcException extends RuntimeException {
  JdbcException(SQLException cause) {
    super(cause);
  }

  @Override public SQLException getCause() {
    return (SQLException) super.getCause();
  }
}
