/*
 * Copyright 2019-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dockbox.hartshorn.data.hibernate;

import org.dockbox.hartshorn.component.Enableable;
import org.dockbox.hartshorn.util.StringUtilities;
import org.dockbox.hartshorn.inject.binding.Bound;
import org.dockbox.hartshorn.application.ExceptionHandler;
import org.dockbox.hartshorn.application.context.ApplicationContext;
import org.dockbox.hartshorn.util.reflect.TypeContext;
import org.dockbox.hartshorn.util.Exceptional;
import org.dockbox.hartshorn.util.ApplicationException;
import org.dockbox.hartshorn.data.context.EntityContext;
import org.dockbox.hartshorn.data.jpa.JpaRepository;
import org.dockbox.hartshorn.data.remote.DerbyFileRemote;
import org.dockbox.hartshorn.data.remote.MariaDbRemote;
import org.dockbox.hartshorn.data.remote.MySQLRemote;
import org.dockbox.hartshorn.data.remote.PersistenceConnection;
import org.dockbox.hartshorn.data.remote.PostgreSQLRemote;
import org.dockbox.hartshorn.data.remote.Remote;
import org.dockbox.hartshorn.data.remote.SqlServerRemote;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.dialect.DerbyTenSevenDialect;
import org.hibernate.dialect.MariaDB103Dialect;
import org.hibernate.dialect.MySQL8Dialect;
import org.hibernate.dialect.PostgreSQL95Dialect;
import org.hibernate.dialect.SQLServer2012Dialect;

import java.io.Closeable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.inject.Inject;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

public class HibernateJpaRepository<T, ID> implements JpaRepository<T, ID>, Enableable, Closeable {

    private final Map<Class<? extends Remote<?>>, String> dialects = new ConcurrentHashMap<>();
    private final Configuration configuration = new Configuration();
    private final Class<T> type;

    private SessionFactory factory;
    private PersistenceConnection connection;
    private Session session;

    @Inject
    private ApplicationContext applicationContext;

    @Bound
    public HibernateJpaRepository(final Class<T> type) {
        this.type = type;
    }

    protected SessionFactory factory() {
        return this.factory;
    }

    protected PersistenceConnection connection() {
        return this.connection;
    }

    public ApplicationContext applicationContext() {
        return this.applicationContext;
    }

    @Override
    public boolean canEnable() {
        return this.factory == null;
    }

    public void registerDialect(final Class<? extends Remote<?>> remote, final String dialectClass) {
        this.dialects.put(remote, dialectClass);
    }

    protected String dialect(final PersistenceConnection connection) throws ApplicationException {
        final Remote remote = connection.remote();

        if (remote instanceof HibernateRemote hibernateRemote) {
            return hibernateRemote.dialect().getCanonicalName();
        }

        final String dialect = this.dialects.get(remote.getClass());
        if (dialect != null) return dialect;

        throw new ApplicationException("Unexpected remote connection: " + remote);
    }

    protected void registerDefaultDialects() {
        this.registerDialect(DerbyFileRemote.class, DerbyTenSevenDialect.class.getCanonicalName());
        this.registerDialect(MySQLRemote.class, MySQL8Dialect.class.getCanonicalName());
        this.registerDialect(PostgreSQLRemote.class, PostgreSQL95Dialect.class.getCanonicalName());
        this.registerDialect(MariaDbRemote.class, MariaDB103Dialect.class.getCanonicalName());
        this.registerDialect(SqlServerRemote.class, SQLServer2012Dialect.class.getCanonicalName());
    }

    @Override
    public void enable() throws ApplicationException {
        if (this.connection == null) {
            this.applicationContext().log().debug("No connection was set for JPA repository instance, using configuration values instead.");
            final HibernateRemote remote = this.applicationContext().get(HibernateRemote.class);
            this.connection = remote.connection();
        }

        this.registerDefaultDialects();

        if (StringUtilities.notEmpty(this.connection.username()) || StringUtilities.notEmpty(this.connection.password())) {
            this.applicationContext().log().debug("Username or password were configured in the active connection, adding to Hibernate configuration");
            this.configuration.setProperty("hibernate.connection.username", this.connection.username());
            this.configuration.setProperty("hibernate.connection.password", this.connection.password());
        }
        this.configuration.setProperty("hibernate.connection.url", this.connection.url());

        final String driver = this.connection.remote().driver();
        final String dialect = this.dialect(this.connection);

        this.applicationContext().log().debug("Determined driver: %s and dialect: %s".formatted(driver, dialect));

        this.configuration.setProperty("hibernate.hbm2ddl.auto", (String) this.applicationContext().property("hibernate.hbm2ddl.auto").or("update"));
        this.configuration.setProperty("hibernate.connection.driver_class", driver);
        this.configuration.setProperty("hibernate.dialect", dialect);

        Exceptional<EntityContext> context = this.applicationContext().first(EntityContext.class);
        if (context.absent()) {
            final Collection<TypeContext<?>> entities = this.applicationContext.environment().types(Entity.class);
            final EntityContext entityContext = new EntityContext(entities);
            this.applicationContext.add(entityContext);
            context = Exceptional.of(entityContext);
        }

        final Collection<TypeContext<?>> entities = context.get().entities();
        for (final TypeContext<?> entity : entities) {
            this.configuration.addAnnotatedClass(entity.type());
        }

        this.configuration.addProperties(this.applicationContext.properties());

        try {
            this.applicationContext().log().debug("Building session factory for Hibernate service #%d".formatted(this.hashCode()));
            this.factory = this.configuration.buildSessionFactory();
        }
        catch (final Throwable e) {
            throw new ApplicationException(e);
        }
    }

    @Override
    public Object save(final Object object) {
        return this.transformTransactional(session -> {
            session.save(object);
            return object;
        });
    }

    @Override
    public Object update(final Object object) {
        return this.transformTransactional(session -> {
            session.update(object);
            return object;
        });
    }

    @Override
    public Object updateOrSave(final Object object) {
        return this.transformTransactional(session -> {
            session.saveOrUpdate(object);
            return object;
        });
    }

    @Override
    public void delete(final Object object) {
        this.performTransactional(session -> session.delete(object));
    }

    private void performTransactional(final Consumer<Session> action) {
        final Session session = this.session();
        final Transaction transaction = session.beginTransaction();
        action.accept(session);
        transaction.commit();
        this.close();
    }

    private <R> R transformTransactional(final Function<Session, R> action) {
        final Session session = this.session();
        final Transaction transaction = session.beginTransaction();
        final R out = action.apply(session);
        transaction.commit();
        this.close();
        return out;
    }

    @Override
    public Set<T> findAll() {
        final Session session = this.session();
        final CriteriaBuilder builder = session.getCriteriaBuilder();
        final CriteriaQuery<T> criteria = builder.createQuery(this.reify());
        criteria.from(this.reify());
        final List<T> data = session.createQuery(criteria).getResultList();
        return Set.copyOf(data);
    }

    @Override
    public Exceptional<T> findById(final ID id) {
        final Session session = this.session();
        return Exceptional.of(session.find(this.reify(), id));
    }

    @Override
    public Class<T> reify() {
        return this.type;
    }

    @Override
    public void flush() {
        if (this.session != null && this.session.isOpen()) {
            this.session.flush();
            final Transaction transaction = this.session().getTransaction();
            if (transaction.isActive()) transaction.commit();
            this.close();
        }
    }

    @Override
    public JpaRepository<T, ID> connection(final PersistenceConnection connection) {
        if (this.connection != null) throw new IllegalStateException("Connection has already been configured!");
        this.connection = connection;
        try {
            this.applicationContext().enable(this);
        } catch (final ApplicationException e) {
            this.applicationContext().handle(e);
        }
        return this;
    }

    @Override
    public void close() {
        if (this.session != null && this.session.isOpen()) {
            this.session.close();
            this.session = null;
        }
    }

    @Override
    public EntityManager entityManager() {
        return this.session();
    }

    public Session session() {
        if (this.session != null && this.session.isOpen()) return this.session;

        // If the factory was not constructed at this point, the connection was not configured. This indicates the
        // repository should fall back to constructing the default remote through the active HibernateRemote binding.
        if (this.factory == null) {
            try {
                this.applicationContext().enable(this);
            } catch (final ApplicationException e) {
                return ExceptionHandler.unchecked(e);
            }
        }

        final PersistenceConnection connection = this.connection();
        if (connection == null) throw new IllegalStateException("Connection has not been configured!");

        this.applicationContext().log().debug("Opening remote session to %s".formatted(connection.url()));
        this.session = this.factory().openSession();

        return this.session;
    }
}
