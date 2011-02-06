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
