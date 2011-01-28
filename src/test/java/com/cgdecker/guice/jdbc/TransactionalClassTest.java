package com.cgdecker.guice.jdbc;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.persist.Transactional;
import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.sql.DataSource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author cgdecker@gmail.com (Colin Decker)
 */
public class TransactionalClassTest {

  @Test public void testFoo() throws SQLException {
    final DataSource dataSource = Hsqldb.getDataSource();
    Injector injector = Guice.createInjector(new AbstractModule() {
      @Override protected void configure() {
        bind(DataSource.class).toInstance(dataSource);
      }
    }, new JdbcPersistModule());

    FooRepository repository = injector.getInstance(FooRepository.class);
    assertTrue(repository.getFoos().isEmpty());
    repository.addFoo(new Foo(1, "abc"));
    assertEquals(1, repository.getFoos().size());
  }

  @Transactional
  public static class FooRepository {
    private final Provider<Connection> connectionProvider;

    @Inject public FooRepository(Provider<Connection> connectionProvider) {
      this.connectionProvider = connectionProvider;
    }

    public List<Foo> getFoos() {
      Connection conn = connectionProvider.get();
      try {
        PreparedStatement pS = conn.prepareStatement("SELECT * FROM foo");
        ResultSet rS = pS.executeQuery();
        List<Foo> result = Foo.fromResultSet(rS);
        rS.close();
        pS.close();
        return result;
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    }

    public void addFoo(Foo foo) {
      Connection conn = connectionProvider.get();
      try {
        PreparedStatement pS = conn.prepareStatement("INSERT INTO foo (id, name) VALUES (?,?)");
        pS.setInt(1, foo.getId());
        pS.setString(2, foo.getName());
        pS.executeUpdate();
        pS.close();
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
