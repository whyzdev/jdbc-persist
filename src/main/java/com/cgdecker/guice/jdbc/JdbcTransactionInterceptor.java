package com.cgdecker.guice.jdbc;

import com.google.inject.persist.Transactional;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.sql.Connection;
import java.sql.SQLException;

import javax.inject.Inject;

/**
 * Interceptor for {@code @Transactional} methods.
 *
 * @author cgdecker@gmail.com (Colin Decker)
 */
class JdbcTransactionInterceptor implements MethodInterceptor {
  @Inject private JdbcPersistService persistService;
  private final ThreadLocal<Boolean> workStarted = new ThreadLocal<Boolean>();

  public Object invoke(MethodInvocation invocation) throws Throwable {
    startWorkIfNecessary();

    Connection connection = persistService.get();
    JdbcTransaction transaction = new JdbcTransaction(connection);

    if (transaction.isActive()) {
      return invocation.proceed();
    }

    transaction.begin();

    try {
      Object result = invocation.proceed();
      transaction.commit();
      return result;
    } catch (Exception e) {
      throw rollbackOrCommit(transaction, e, invocation);
    } finally {
      endWorkIfNecessary();
    }
  }

  /**
   * Starts a unit of work if one isn't currently active.
   */
  private void startWorkIfNecessary() {
    if (!persistService.isWorking()) {
      persistService.begin();
      workStarted.set(true);
    }
  }

  /**
   * Ends the current unit of work if it was started by this invocation.
   */
  private void endWorkIfNecessary() {
    if (workStarted.get() != null) {
      workStarted.remove();
      persistService.end();
    }
  }

  /**
   * Rolls back or commits the transaction depending on the type of exception that occurred and what
   * types of exceptions the metadata specifies should cause rollbacks.
   *
   * @param transaction the active transaction
   * @param e the exception that occurred
   * @param invocation the method invocation
   * @return the given exception, for re-throwing
   */
  private static Exception rollbackOrCommit(JdbcTransaction transaction, Exception e,
                                            MethodInvocation invocation) {
    Transactional metadata = getTransactionalAnnotation(invocation);
    if (shouldRollback(e, metadata))
      transaction.rollback();
    else
      transaction.commit();
    return e;
  }

  private static final Transactional DEFAULT_METADATA = AnnotationHolder.class.getAnnotation(Transactional.class);

  /**
   * Gets the {@code @Transactional} metadata for the given method invocation. Attempts to read it
   * first from the method that is being invoked, then from the class of the object the method is
   * being invoked on. If it doesn't find it either of those places, the default is used.
   *
   * @param invocation the method invocation
   * @return transactional metadata for the invocation
   */
  private static Transactional getTransactionalAnnotation(MethodInvocation invocation) {
    Transactional result = invocation.getMethod().getAnnotation(Transactional.class);
    if (result == null)
      result = invocation.getThis().getClass().getAnnotation(Transactional.class);
    if (result == null)
      result = DEFAULT_METADATA;
    return result;
  }

  /**
   * Checks if a rollback should occur based on the type of exception and the given metadata. A
   * rollback should only occur if the exception is both a type that should be rolled back on and
   * not a type that should be ignored. In addition to any types defined in the metadata, we always
   * roll back on {@link SQLException}s unless the metadata specifies that it should be ignored.
   *
   * @param e        the exception that occurred
   * @param metadata the metadata telling what types of exceptions should cause rollbacks and what
   *                 types should not
   * @return {@code true} if the transaction should be rolled back; {@code false} if it should be
   *         committed
   */
  private static boolean shouldRollback(Exception e, Transactional metadata) {
    return (isInstance(e, metadata.rollbackOn()) || SQLException.class.isInstance(e)) &&
        !isInstance(e, metadata.ignore());
  }

  /**
   * Checks if the given object is an instance of one of the given types.
   *
   * @param obj   the object to check
   * @param types the types to check for
   * @return {@code true} if the object is an instance of one of the given types;
   *         {@code false} otherwise.
   */
  private static boolean isInstance(Object obj, Class<?>[] types) {
    for (Class<?> type : types) {
      if (type.isInstance(obj))
        return true;
    }
    return false;
  }

  @Transactional
  private static class AnnotationHolder {
  }
}
