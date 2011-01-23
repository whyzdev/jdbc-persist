package com.cgdecker.guice.jdbc;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * @author cgdecker@gmail.com (Colin Decker)
 */
public class JdbcTransactionInterceptor implements MethodInterceptor {
  public Object invoke(MethodInvocation invocation) throws Throwable {
    return null;
  }
}
