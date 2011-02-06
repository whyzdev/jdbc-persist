/*
 * Copyright 2011 Colin Decker
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cgdecker.guice.jdbc;

import com.google.common.base.Throwables;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.persist.PersistService;
import com.google.inject.persist.UnitOfWork;
import org.junit.After;
import org.junit.Before;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * @author cgdecker@gmail.com (Colin Decker)
 */
public abstract class AbstractPersistTest {
  protected Injector injector;

  @Before
  public void setUp() {
    injector = Guice.createInjector(Hsqldb.getModule());
    injector.getInstance(PersistService.class).start();
  }

  @After
  public void tearDown() {
    injector.getInstance(PersistService.class).stop();
  }

  protected PersistService getPersistService() {
    return injector.getInstance(PersistService.class);
  }

  protected UnitOfWork getUnitOfWork() {
    return injector.getInstance(UnitOfWork.class);
  }

  protected void assertTransactionActive() {
    assertTransaction(true);
  }

  protected void assertTransactionNotActive() {
    assertTransaction(false);
  }

  private void assertTransaction(boolean active) {
    String message = active ? "transaction not active when it should have been" :
        "transaction active when it should not have been";
    try {
      assertEquals(message, active, !injector.getInstance(Connection.class).getAutoCommit());
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  protected List<Foo> getFoos() {
    try {
      return injector.getInstance(FooGetter.class).getFoos();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  protected <X extends Exception, T extends FooAdder<X>> void testRollbackOccurs(Class<T> type,
                                                                                 Class<X> exceptionType) throws SQLException {
    testRollback(true, type, exceptionType);
  }

  protected <X extends Exception, T extends FooAdder<X>> void testRollbackDoesNotOccur(
      Class<T> type, Class<X> exceptionType) throws SQLException {
    testRollback(false, type, exceptionType);
  }

  protected <X extends Exception, T extends FooAdder<X>> void testRollback(boolean expected,
                                                                           Class<T> type,
                                                                           Class<X> exceptionType) throws SQLException {
    getUnitOfWork().begin();

    T repository = injector.getInstance(type);

    try {
      repository.addFoo(new Foo(1, "bar"));
    } catch (Exception e) {
      if (!exceptionType.isInstance(e))
        throw Throwables.propagate(e);
    }

    String message = expected ? "got a result, no rollback occurred" :
        "didn't get a result, a rollback occurred";
    assertEquals(message, expected, getFoos().isEmpty());

    getUnitOfWork().end();
  }

  protected void assertUnitOfWorkNotActive() {
    assertFalse("unit of work active when it should not have been",
        injector.getInstance(JdbcPersistService.class).isWorking());
  }
}
