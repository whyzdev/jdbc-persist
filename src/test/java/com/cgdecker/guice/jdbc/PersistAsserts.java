package com.cgdecker.guice.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author cgdecker@gmail.com (Colin Decker)
 */
public class PersistAsserts {
  public static void assertTransactionNotActive(JdbcConnectionManager manager) {
    assertFalse("transaction active when it should not be", manager.getTransaction().isActive());
  }

  public static void assertTransactionActive(JdbcConnectionManager manager) {
    assertTrue("transaction not active", manager.getTransaction().isActive());
  }

  public static void assertTransactionNotActive(Connection conn) {
    try {
      assertTrue("transaction active when it should not be", conn.getAutoCommit());
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public static void assertTransactionActive(Connection conn) {
    try {
      assertFalse("transaction not active", conn.getAutoCommit());
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
