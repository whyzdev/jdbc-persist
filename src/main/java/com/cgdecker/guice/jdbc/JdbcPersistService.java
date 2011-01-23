package com.cgdecker.guice.jdbc;

import com.google.inject.persist.PersistService;
import com.google.inject.persist.UnitOfWork;

import javax.inject.Inject;

/**
 * @author cgdecker@gmail.com (Colin Decker)
 */
class JdbcPersistService implements PersistService, UnitOfWork {
  @Inject JdbcPersistService() {
    // What needs to be injected here? DataSources??
  }

  public void start() {
  }

  public void stop() {
  }

  public void begin() {
    // scope to the current thread
  }

  public void end() {
  }
}
