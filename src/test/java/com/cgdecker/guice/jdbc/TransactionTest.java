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
