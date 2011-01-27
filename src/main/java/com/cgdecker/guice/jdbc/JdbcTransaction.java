package com.cgdecker.guice.jdbc;

import java.sql.SQLException;

/**
 * @author cgdecker@gmail.com (Colin Decker)
 */
interface JdbcTransaction {
  void begin();

  void commit();

  void rollback();

  boolean isActive();
}
