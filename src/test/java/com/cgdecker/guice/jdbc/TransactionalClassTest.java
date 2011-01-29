package com.cgdecker.guice.jdbc;

import com.google.inject.persist.Transactional;
import com.google.inject.persist.UnitOfWork;
import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.inject.Inject;

import static com.cgdecker.guice.jdbc.PersistAsserts.assertTransactionActive;
import static com.cgdecker.guice.jdbc.PersistAsserts.assertTransactionNotActive;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author cgdecker@gmail.com (Colin Decker)
 */
public class TransactionalClassTest extends AbstractHsqldbTest {

  @Test public void transactionsStartAndEndAroundMethods() throws SQLException {
    JdbcConnectionManager connectionManager = injector.getInstance(JdbcConnectionManager.class);

    assertTransactionNotActive(connectionManager);
    assertTransactionNotActive(connectionManager.get());
    injector.getInstance(TransactionChecker.class).checkForActiveTransaction();
    assertTransactionNotActive(connectionManager);
    assertTransactionNotActive(connectionManager.get());
  }

  @Test public void testFoo() throws SQLException {
    System.out.println("start testFoo");
    UnitOfWork unitOfWork = injector.getInstance(UnitOfWork.class);
    unitOfWork.begin();
    FooRepository repository = injector.getInstance(FooRepository.class);

    assertTrue("sanity check failed", repository.getFoos().isEmpty());
    repository.addFoo(new Foo(1, "abc"));

    List<Foo> savedFoos = repository.getFoos();
    assertEquals(1, savedFoos.size());
    assertEquals(new Foo(1, "abc"), savedFoos.get(0));

    unitOfWork.end();

    unitOfWork.begin();

    savedFoos = repository.getFoos();
    assertEquals(1, savedFoos.size());
    assertEquals(new Foo(1, "abc"), savedFoos.get(0));

    unitOfWork.end();
  }

  @Transactional
  public static class TransactionChecker {
    @Inject private JdbcConnectionManager connectionManager;

    public void checkForActiveTransaction() {
      assertTransactionActive(connectionManager);
      assertTransactionActive(connectionManager.get());
    }
  }

  @Transactional
  public static class FooRepository {
    private final JdbcConnectionManager connectionProvider;

    @Inject public FooRepository(JdbcConnectionManager connectionProvider) {
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

    public void addFoo(Foo foo) throws SQLException {
      Connection conn = connectionProvider.get();
      PreparedStatement pS = conn.prepareStatement("INSERT INTO foo (id, name) VALUES (?,?)");
      pS.setInt(1, foo.getId());
      pS.setString(2, foo.getName());
      pS.executeUpdate();
      pS.close();
    }
  }
}
