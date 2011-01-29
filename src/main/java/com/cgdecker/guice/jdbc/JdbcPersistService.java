package com.cgdecker.guice.jdbc;

import com.google.inject.Provider;
import com.google.inject.persist.PersistService;
import com.google.inject.persist.UnitOfWork;

import javax.inject.Inject;
import javax.sql.DataSource;

/**
 * @author cgdecker@gmail.com (Colin Decker)
 */
class JdbcPersistService implements Provider<JdbcConnectionManager>, PersistService, UnitOfWork {
  private final DataSource dataSource;
  private final ThreadLocal<JdbcConnectionManager> connectionManagerThreadLocal = new ThreadLocal<JdbcConnectionManager>();

  @Inject JdbcPersistService(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public void start() {
    /*
     * Nothing to do here really. We're injecting the DataSource this uses directly, not creating it
     * when the service starts, and there isn't any concept of "closing" the DataSource itself.
     */
  }

  public void stop() {
    // Nothing to do here either.
  }

  public void begin() {
    System.out.println("begin work");
    if (isWorking()) {
      throw new IllegalStateException("Work already begun on this thread. Looks like you have " +
          "called UnitOfWork.begin() twice without a balancing call to end() in between.");
    }

    //new Exception().printStackTrace();

    connectionManagerThreadLocal.set(new JdbcConnectionManager(dataSource));
  }

  public void end() {
    System.out.println("end work");
    JdbcConnectionManager manager = connectionManagerThreadLocal.get();

    if (manager == null)
      return;

    connectionManagerThreadLocal.remove();
    manager.close();
  }

  /**
   * @return {@code true} if work is currently active on the current thread; {@code false} otherwise.
   */
  public boolean isWorking() {
    return connectionManagerThreadLocal.get() != null;
  }

  public JdbcConnectionManager get() {
    System.out.println("Get connection manager");
    if (!isWorking())
      begin();
    return connectionManagerThreadLocal.get();
  }
}
