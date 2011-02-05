package com.cgdecker.guice.jdbc;

import com.google.inject.ProvisionException;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

/**
 * @author cgdecker@gmail.com (Colin Decker)
 */
public class UnitOfWorkTest extends AbstractPersistTest {

  @Test public void differentConnectionsForDifferentUnitsOfWork() {
    getUnitOfWork().begin();
    Connection firstConn = injector.getInstance(Connection.class);
    assertEquals(firstConn, injector.getInstance(Connection.class));
    getUnitOfWork().end();

    getUnitOfWork().begin();
    Connection secondConn = injector.getInstance(Connection.class);
    assertFalse(firstConn.equals(secondConn));
    assertEquals(secondConn, injector.getInstance(Connection.class));
    getUnitOfWork().end();
  }

  @Test public void illegalToRetrieveConnectionOutsideUnitOfWork() {
    try {
      injector.getInstance(Connection.class);
      fail();
    } catch (ProvisionException e) {
      assertEquals(IllegalStateException.class, e.getCause().getClass());
    }
  }

  @Test public void unitsOfWorkNestCorrectly() throws SQLException {
    getUnitOfWork().begin();
    Connection conn = injector.getInstance(Connection.class);
    assertFalse("conn is not open", conn.isClosed());

    getUnitOfWork().begin();
    getUnitOfWork().begin();

    assertEquals(conn, injector.getInstance(Connection.class));
    assertFalse("conn is not open", conn.isClosed());

    getUnitOfWork().end();

    assertEquals(conn, injector.getInstance(Connection.class));
    assertFalse("conn is not open", conn.isClosed());

    getUnitOfWork().begin();
    getUnitOfWork().end();

    getUnitOfWork().end();

    assertEquals(conn, injector.getInstance(Connection.class));
    assertFalse("conn is not open", conn.isClosed());

    getUnitOfWork().end();

    try {
      injector.getInstance(Connection.class);
      fail("unit of work did not end when expected");
    } catch (ProvisionException expected) {
      assertEquals(IllegalStateException.class, expected.getCause().getClass());
    }
  }
}
