/*
 * Copyright (C) 2020 Guus Lieben
 *
 * This framework is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library. If not, see {@literal<http://www.gnu.org/licenses/>}.
 */

package org.dockbox.hartshorn.data.service;

import org.dockbox.hartshorn.core.annotations.activate.AutomaticActivation;
import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.context.MethodProxyContext;
import org.dockbox.hartshorn.core.context.element.MethodContext;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.dockbox.hartshorn.core.proxy.ProxyContext;
import org.dockbox.hartshorn.core.proxy.ProxyFunction;
import org.dockbox.hartshorn.core.services.ProcessingOrder;
import org.dockbox.hartshorn.core.services.ServiceAnnotatedMethodInterceptorPostProcessor;
import org.dockbox.hartshorn.data.QueryFunction;
import org.dockbox.hartshorn.data.annotations.EntityModifier;
import org.dockbox.hartshorn.data.annotations.Query;
import org.dockbox.hartshorn.data.annotations.Query.QueryType;
import org.dockbox.hartshorn.data.annotations.Transactional;
import org.dockbox.hartshorn.data.annotations.UsePersistence;
import org.dockbox.hartshorn.data.context.QueryContext;
import org.dockbox.hartshorn.data.jpa.JpaRepository;

import java.util.Collection;
import java.util.List;

@AutomaticActivation
public class QueryPostProcessor extends ServiceAnnotatedMethodInterceptorPostProcessor<Query, UsePersistence> {

    @Override
    public Class<UsePersistence> activator() {
        return UsePersistence.class;
    }

    @Override
    public Class<Query> annotation() {
        return Query.class;
    }

    @Override
    public <T> boolean preconditions(final ApplicationContext context, final MethodProxyContext<T> methodContext) {
        final MethodContext<?, T> method = methodContext.method();
        final TypeContext<T> parent = method.parent();
        return parent.childOf(JpaRepository.class);
    }

    @Override
    public <T, R> ProxyFunction<T, R> process(final ApplicationContext context, final MethodProxyContext<T> methodContext) {
        final MethodContext<?, T> method = methodContext.method();
        final QueryFunction function = context.get(QueryFunction.class);
        final boolean modifying = method.annotation(EntityModifier.class).present();
        final boolean transactional = method.annotation(Transactional.class).present();
        final Query query = method.annotation(Query.class).get();
        final TypeContext<?> entityType = this.entityType(method, query);

        return (T instance, Object[] args, ProxyContext proxyContext) -> {
            final JpaRepository<?, ?> repository = (JpaRepository<?, ?>) methodContext.instance();
            if (query.automaticFlush() && !transactional) repository.flush();

            final QueryContext queryContext = new QueryContext(query, args, method, entityType, context, repository, modifying);

            final Object result = function.execute(queryContext);

            return (R) result;
        };
    }

    @Override
    public ProcessingOrder order() {
        return ProcessingOrder.LATE;
    }

    protected TypeContext<?> entityType(final MethodContext<?, ?> context, final Query query) {
        final TypeContext<?> queryEntityType = TypeContext.of(query.entityType());
        if (queryEntityType.isVoid()) {
            final TypeContext<?> returnType = context.genericReturnType();
            if (returnType.childOf(Collection.class)) {
                final List<TypeContext<?>> typeParameters = returnType.typeParameters();
                if (typeParameters.isEmpty()) {
                    if (query.type() == QueryType.NATIVE)
                        throw new IllegalStateException("Could not determine entity type of " + context.qualifiedName() + ". Alternatively, set the entityType in the @Query annotation on this method or change the query to JPQL.");
                    else return TypeContext.VOID;
                }
                return typeParameters.get(0);
            }
            else return returnType;
        }
        else return queryEntityType;
    }
}
