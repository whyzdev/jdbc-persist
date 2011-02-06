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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * @author cgdecker@gmail.com (Colin Decker)
 */
public class FooGetter {
  private final Provider<Connection> connectionProvider;

  @Inject public FooGetter(Provider<Connection> connectionProvider) {
    this.connectionProvider = connectionProvider;
  }

  public List<Foo> getFoos() throws SQLException {
    Connection conn = connectionProvider.get();
    PreparedStatement pS = conn.prepareStatement("SELECT * FROM foo");
    ResultSet rS = pS.executeQuery();
    List<Foo> result = Foo.fromResultSet(rS);
    pS.close();
    return result;
  }
}
