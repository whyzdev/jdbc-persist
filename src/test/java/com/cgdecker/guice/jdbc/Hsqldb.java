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

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.apache.commons.dbcp.BasicDataSource;
import org.hsqldb.jdbc.JDBCDataSource;
import org.hsqldb.jdbcDriver;
import org.hsqldb.server.Server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.sql.DataSource;

/**
 * @author cgdecker@gmail.com (Colin Decker)
 */
class Hsqldb {
  public static Module getModule() {
    return new AbstractModule() {
      @Override protected void configure() {
        bind(DataSource.class).toInstance(getDataSource());
        install(new JdbcPersistModule());
      }
    };
  }

  public static DataSource getDataSource() {
    BasicDataSource result = new BasicDataSource();
    result.setDriverClassName(jdbcDriver.class.getName());
    result.setUrl("jdbc:hsqldb:mem:.");
    result.setUsername("sa");
    result.setPassword("");
    setUpDatabase(result);
    return result;
  }

  private static void setUpDatabase(DataSource dataSource) {
    Connection conn = null;
    try {
      conn = dataSource.getConnection();
      PreparedStatement pS = conn.prepareStatement("SET DATABASE TRANSACTION CONTROL MVCC");
      pS.executeUpdate();
      pS.close();

      pS = conn.prepareStatement("DROP TABLE foo IF EXISTS");
      pS.executeUpdate();
      pS.close();

      pS = conn.prepareStatement("CREATE TABLE foo ( id INTEGER, name VARCHAR(100) )");
      pS.executeUpdate();
      pS.close();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    } finally {
      if (conn != null) {
        try {
          conn.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }
  }
}
