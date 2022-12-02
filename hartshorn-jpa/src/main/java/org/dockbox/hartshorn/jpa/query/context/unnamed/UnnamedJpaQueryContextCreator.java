package org.dockbox.hartshorn.jpa.query.context.unnamed;

import org.dockbox.hartshorn.application.context.ApplicationContext;
import org.dockbox.hartshorn.component.processing.ComponentProcessingContext;
import org.dockbox.hartshorn.jpa.annotations.Query;
import org.dockbox.hartshorn.jpa.query.context.JpaQueryContext;
import org.dockbox.hartshorn.jpa.query.context.JpaQueryContextCreator;
import org.dockbox.hartshorn.proxy.MethodInterceptorContext;
import org.dockbox.hartshorn.util.introspect.view.MethodView;
import org.dockbox.hartshorn.util.introspect.view.TypeView;
import org.dockbox.hartshorn.util.option.Option;

public class UnnamedJpaQueryContextCreator implements JpaQueryContextCreator {

    @Override
    public <T> Option<JpaQueryContext> create(final ApplicationContext applicationContext,
                                              final MethodInterceptorContext<T, ?> interceptorContext,
                                              final TypeView<?> entityType,
                                              final Object persistenceCapable) {

        final MethodView<T, ?> method = interceptorContext.method();
        return method.annotations()
                .get(Query.class)
                .map(query -> new UnnamedJpaQueryContext(
                        query,
                        interceptorContext.args(),
                        method,
                        entityType,
                        applicationContext,
                        persistenceCapable
                ));
    }

    @Override
    public <T> boolean supports(final ComponentProcessingContext<T> processingContext, final MethodView<T, ?> method) {
        return method.annotations().has(Query.class);
    }
}