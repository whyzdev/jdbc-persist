package com.cgdecker.guice.jdbc;

import java.sql.SQLException;

/**
 * @author cgdecker@gmail.com (Colin Decker)
 */
public interface FooAdder<X extends Exception> {
  void addFoo(Foo foo) throws SQLException, X;
}
