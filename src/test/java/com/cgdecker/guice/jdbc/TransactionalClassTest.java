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

import com.google.inject.persist.Transactional;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author cgdecker@gmail.com (Colin Decker)
 */
public class TransactionalClassTest extends AbstractPersistTest {

  @Test public void transactionsStartAndEndAroundMethods() throws SQLException {
    getUnitOfWork().begin();
    assertTransactionNotActive();
    injector.getInstance(TransactionChecker.class).checkForActiveTransaction(this);
    assertTransactionNotActive();
    getUnitOfWork().end();
  }

  @Test public void savingAndRetrieving() throws SQLException {
    NonThrowingRepository repository = injector.getInstance(NonThrowingRepository.class);

    getUnitOfWork().begin();

    assertTrue("sanity check failed", getFoos().isEmpty());
    repository.addFoo(new Foo(1, "abc"));

    List<Foo> savedFoos = getFoos();
    assertEquals(1, savedFoos.size());
    assertEquals(new Foo(1, "abc"), savedFoos.get(0));

    getUnitOfWork().end();

    getUnitOfWork().begin();

    savedFoos = getFoos();
    assertEquals(1, savedFoos.size());
    assertEquals(new Foo(1, "abc"), savedFoos.get(0));

    getUnitOfWork().end();
  }

  @Test public void sqlExceptionCausesRollback() throws SQLException {
    testRollbackOccurs(SQLExceptionThrowingRepository.class, SQLException.class);
  }

  @Test public void runtimeExceptionCausesRollback() throws SQLException {
    testRollbackOccurs(RuntimeExceptionThrowingRepository.class, IllegalStateException.class);
  }

  @Test public void checkedExceptionCausesRollback() throws SQLException {
    testRollbackOccurs(CheckedExceptionThrowingRepository.class, FileNotFoundException.class);
  }

  @Test public void ignoredCheckedExceptionDoesNotCauseRollback() throws SQLException {
    testRollbackDoesNotOccur(IgnoredCheckedExceptionThrowingRepository.class, FileNotFoundException.class);
  }

  @Test public void ignoredRuntimeExceptionDoesNotCauseRollback() throws SQLException {
    testRollbackDoesNotOccur(IgnoredRuntimeExceptionThrowingRepository.class, IllegalStateException.class);
  }

  @Transactional
  public static class TransactionChecker {
    @Inject private Provider<Connection> connectionProvider;

    public void checkForActiveTransaction(TransactionalClassTest test) {
      test.assertTransactionActive();
    }
  }

  @Transactional
  public static class NonThrowingRepository {
    private final Provider<Connection> connectionProvider;

    @Inject public NonThrowingRepository(Provider<Connection> connectionProvider) {
      this.connectionProvider = connectionProvider;
    }

    public void addFoo(Foo foo) throws SQLException {
      add(connectionProvider.get(), foo);
    }
  }

  @Transactional
  public static class SQLExceptionThrowingRepository implements FooAdder<SQLException> {
    private final Provider<Connection> connectionProvider;

    @Inject public SQLExceptionThrowingRepository(Provider<Connection> connectionProvider) {
      this.connectionProvider = connectionProvider;
    }

    public void addFoo(Foo foo) throws SQLException {
      add(connectionProvider.get(), foo);
      throw new SQLException();
    }
  }

  @Transactional
  public static class RuntimeExceptionThrowingRepository implements FooAdder<IllegalStateException> {
    private final Provider<Connection> connectionProvider;

    @Inject public RuntimeExceptionThrowingRepository(Provider<Connection> connectionProvider) {
      this.connectionProvider = connectionProvider;
    }

    public void addFoo(Foo foo) throws SQLException {
      add(connectionProvider.get(), foo);
      throw new IllegalStateException();
    }
  }

  @Transactional(rollbackOn = IOException.class)
  public static class CheckedExceptionThrowingRepository implements FooAdder<FileNotFoundException> {
    private final Provider<Connection> connectionProvider;

    @Inject public CheckedExceptionThrowingRepository(Provider<Connection> connectionProvider) {
      this.connectionProvider = connectionProvider;
    }

    public void addFoo(Foo foo) throws SQLException, FileNotFoundException {
      add(connectionProvider.get(), foo);
      throw new FileNotFoundException();
    }
  }

  @Transactional(rollbackOn = IOException.class, ignore = FileNotFoundException.class)
  public static class IgnoredCheckedExceptionThrowingRepository implements FooAdder<FileNotFoundException> {
    private final Provider<Connection> connectionProvider;

    @Inject public IgnoredCheckedExceptionThrowingRepository(Provider<Connection> connectionProvider) {
      this.connectionProvider = connectionProvider;
    }

    public void addFoo(Foo foo) throws SQLException, FileNotFoundException {
      add(connectionProvider.get(), foo);
      throw new FileNotFoundException();
    }
  }

  @Transactional(ignore = IllegalStateException.class)
  public static class IgnoredRuntimeExceptionThrowingRepository implements FooAdder<IllegalStateException> {
    private final Provider<Connection> connectionProvider;

    @Inject public IgnoredRuntimeExceptionThrowingRepository(Provider<Connection> connectionProvider) {
      this.connectionProvider = connectionProvider;
    }

    public void addFoo(Foo foo) throws SQLException {
      add(connectionProvider.get(), foo);
      throw new IllegalStateException();
    }
  }

  private static void add(Connection conn, Foo foo) throws SQLException {
    PreparedStatement pS = conn.prepareStatement("INSERT INTO foo (id, name) VALUES (?,?)");
    pS.setInt(1, foo.getId());
    pS.setString(2, foo.getName());
    pS.executeUpdate();
    pS.close();
  }
}
