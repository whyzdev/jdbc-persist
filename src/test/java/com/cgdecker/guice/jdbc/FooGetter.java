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
