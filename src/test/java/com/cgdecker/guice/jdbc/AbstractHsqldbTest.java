package com.cgdecker.guice.jdbc;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.Before;

/**
 * @author cgdecker@gmail.com (Colin Decker)
 */
public abstract class AbstractHsqldbTest {
  protected Injector injector;

  @Before
  public void setUp() {
    injector = Guice.createInjector(Hsqldb.getModule());
  }
}
