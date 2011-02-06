/*
 * Copyright 2011 Colin Decker
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cgdecker.guice.jdbc;

import com.google.inject.Scopes;
import com.google.inject.persist.PersistModule;
import com.google.inject.persist.PersistService;
import com.google.inject.persist.Transactional;
import com.google.inject.persist.UnitOfWork;
import org.aopalliance.intercept.MethodInterceptor;

import java.sql.Connection;

import javax.sql.DataSource;

import static com.google.inject.matcher.Matchers.annotatedWith;
import static com.google.inject.matcher.Matchers.any;

/**
 * JDBC provider for guice-persist. Requires a binding for a {@link DataSource}.
 *
 * @author cgdecker@gmail.com (Colin Decker)
 */
public class JdbcPersistModule extends PersistModule {
  private final JdbcTransactionInterceptor transactionInterceptor = new JdbcTransactionInterceptor();

  @Override protected void configurePersistence() {
    requireBinding(DataSource.class);

    bind(JdbcPersistService.class).in(Scopes.SINGLETON);

    bind(PersistService.class).to(JdbcPersistService.class);
    bind(UnitOfWork.class).to(JdbcPersistService.class);
    bind(Connection.class).toProvider(JdbcPersistService.class);

    requestInjection(transactionInterceptor);

    // TODO: Remove this once issue 525 is fixed in Guice.
    bindInterceptor(annotatedWith(Transactional.class), any(), transactionInterceptor);
  }

  @Override protected MethodInterceptor getTransactionInterceptor() {
    return transactionInterceptor;
  }
}
