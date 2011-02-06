package com.cgdecker.guice.jdbc;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.PrivateModule;
import com.google.inject.name.Names;
import org.junit.Test;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.sql.Connection;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Qualifier;
import javax.sql.DataSource;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.junit.Assert.assertFalse;

/**
 * @author cgdecker@gmail.com (Colin Decker)
 */
public class MultipleDataSourceTest {

  @Test public void bindingMultipleDataSourcesForRepositoriesInPrivateModule() {
    Injector injector = Guice.createInjector(
        new PrivateModule() {
          @Override protected void configure() {
            bind(DataSource.class).toInstance(Hsqldb.getDataSource());
            install(new JdbcPersistModule());
            bind(FirstDao.class);
            expose(FirstDao.class);
          }
        },
        new PrivateModule() {
          @Override protected void configure() {
            bind(DataSource.class).toInstance(Hsqldb.getDataSource());
            install(new JdbcPersistModule());
            bind(SecondDao.class);
            expose(SecondDao.class);
          }
        }
    );

    FirstDao firstDao = injector.getInstance(FirstDao.class);
    SecondDao secondDao = injector.getInstance(SecondDao.class);
    assertFalse("first dao and second dao had the same data source",
        firstDao.service.dataSource == secondDao.service.dataSource);
  }

  @Test public void bindingMultipleDataSourcesWithBindingAnnotations() {
    Injector injector = Guice.createInjector(
        new PrivateModule() {
          @Override protected void configure() {
            bind(DataSource.class).toInstance(Hsqldb.getDataSource());
            install(new JdbcPersistModule());
            bind(JdbcPersistService.class).annotatedWith(First.class).to(JdbcPersistService.class);
            bind(Connection.class).annotatedWith(First.class).to(Connection.class);
            expose(JdbcPersistService.class).annotatedWith(First.class);
            expose(Connection.class).annotatedWith(First.class);
          }
        },
        new PrivateModule() {
          @Override protected void configure() {
            bind(DataSource.class).toInstance(Hsqldb.getDataSource());
            install(new JdbcPersistModule());
            bind(JdbcPersistService.class).annotatedWith(Names.named("second")).to(JdbcPersistService.class);
            bind(Connection.class).annotatedWith(Names.named("second")).to(Connection.class);
            expose(JdbcPersistService.class).annotatedWith(Names.named("second"));
            expose(Connection.class).annotatedWith(Names.named("second"));
          }
        }
    );

    MultiServiceDao dao = injector.getInstance(MultiServiceDao.class);
    assertFalse("first service and second service had the same data source",
        dao.first.dataSource == dao.second.dataSource);
  }

  @Retention(RUNTIME)
  @Target({FIELD, PARAMETER})
  @Qualifier
  public @interface First {}

  private static class FirstDao {
    @Inject JdbcPersistService service;
  }

  private static class SecondDao {
    @Inject JdbcPersistService service;
  }

  private static class MultiServiceDao {
    @Inject @First JdbcPersistService first;
    @Inject @Named("second") JdbcPersistService second;

    @Inject @First Provider<Connection> connectionProvider;
    @Inject @Named("second") Provider<Connection> connectionProvider2;
  }
}
