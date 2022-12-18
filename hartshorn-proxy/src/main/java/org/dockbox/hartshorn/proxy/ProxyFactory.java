/*
 * Copyright 2019-2023 the original author or authors.
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

package org.dockbox.hartshorn.proxy;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.dockbox.hartshorn.util.ApplicationException;
import org.dockbox.hartshorn.util.collections.ConcurrentClassMap;
import org.dockbox.hartshorn.util.collections.MultiMap;
import org.dockbox.hartshorn.util.introspect.view.ConstructorView;
import org.dockbox.hartshorn.util.introspect.view.MethodView;
import org.dockbox.hartshorn.util.option.Attempt;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * The entrypoint for creating proxy objects. This class is responsible for creating proxy objects for
 * a given class, and is a default contract provided in all {@code ComponentProcessingContext}s for
 * components that permit the creation of proxies.
 *
 * <p>Proxy factories are responsible for creating proxy objects for a given class, after the proxy
 * has been created, the proxy object is passed to the responsible {@link ProxyManager} for further
 * life-cycle management. Proxy factories are able to create zero or more unique proxy objects for
 * a given class, and are responsible for ensuring that the proxy objects are unique.
 *
 * <p>To support the creation of proxies, the {@link ProxyFactory} exposes a set of methods that
 * can be used to modify the proxy object before it is created. This includes the delegation and
 * interception of methods.
 *
 * <h1>Interception</h1>
 * <p>Interception indicates the method is replaced by whichever implementation is chosen. Interception
 * can be done in two ways; full replacement, and wrapping.
 *
 * <h2>Full replacement interception</h2>
 * <p>A full replacement is done using a custom
 * {@link MethodInterceptor}, which accepts a {@link MethodInterceptorContext} to execute given functionality.
 * Within an interceptor it is possible to access all required information about the intercepted method,
 * as can be seen in the {@link MethodInterceptorContext} class.
 *
 * <p>Method interceptors are executed in series, allowing each step to re-use and/or modify the result of
 * another interceptor. To do so, the previous {@link MethodInterceptorContext#result()} is provided. If
 * the interceptor is the first one to execute, the result will be the default value of the return type.
 * The series are executed in no specific order.
 *
 * <pre>{@code
 * factory.intercept(greetingMethod, interceptorContext -> "Hello world!");
 * final User user = factory.proxy().get();
 * final String greeting = user.greeting(); // Returns 'Hello world!'
 * }</pre>
 *
 * <h2>Wrapping interception</h2>
 * <p>Wrapping interception is similar to the pre-existing method phasing
 * approach. It allows for specific callbacks to be executed before a method is performed, after it is finished,
 * and when an exception is thrown during the execution of the method. Wrappers will always be executed, even
 * if the method is intercepted or delegated. This allows for specific states to be prepared and closed around
 * a method's execution. For example, an annotation like {@code @Transactional} the wrapper can be used to:
 * <ul>
 *     <li>Open a transaction before the method is performed</li>
 *     <li>Commit the transaction after the method is finished</li>
 *     <li>Rollback the transaction if an exception is thrown</li>
 * </ul>
 *
 * <pre>{@code
 * public class UserMethodExecutionLogger implements MethodWrapper<User> {
 *     @Override
 *     public void acceptBefore(final MethodView<?, User> method, final User instance, final Object[] args) {
 *         System.out.println("Before method!");
 *     }
 *
 *     @Override
 *     public void acceptAfter(final MethodView<?, User> method, final User instance, final Object[] args) {
 *         System.out.println("After method!");
 *     }
 *
 *     @Override
 *     public void acceptError(final MethodView<?, User> method, final User instance, final Object[] args, final Throwable error) {
 *         System.out.println("Method caused an exception: " + error.getMessage());
 *     }
 * }
 * }</pre>
 * <pre>{@code
 * factory.intercept(greetingMethod, new UserMethodExecutionLogger());
 * final User user = factory.proxy().get();
 * user.speakGreeting();
 * }</pre>
 *
 * <p>The above would then result in the following output:
 * <pre>{@code
 * Before method!
 * User says: Hello world!
 * After method!
 * }</pre>
 *
 * <h1>Delegation</h1>
 * <p>Like interception, delegation replaces the implementation of a proxy object. However, it does not carry the proxy's
 * context down to the implementation. Instead, it redirects the method call to another object. Delegation knows two different
 * delegate types; original instance, and backing implementations.
 *
 * <h2>Original instance delegation</h2>
 * <p>Original instance delegation indicates that the delegate is of the exact same type as the proxy type, or a sub-type of that
 * type. This allows all functionality to be delegated to this instance.
 *
 * <pre>{@code
 * public interface User {
 *     String greeting();
 * }
 * public class UserImpl implements User {
 *     @Override
 *     public String greeting() {
 *         return "Hello implementation!";
 *     }
 * }
 * }</pre>
 * <pre>{@code
 * final StateAwareProxyFactory<User, ?> factory = applicationManager.factory(User.class);
 * factory.delegate(new UserImpl());
 * final User user = factory.proxy().get();
 * user.greeting(); // Returns 'Hello implementation!'
 * }</pre>
 *
 * <h2>Backing implementation delegation</h2>
 * <p>Backing implementations follow the opposite rule of original instance delegation. Instead of requiring the exact type or a subtype to
 * be implemented, backing implementations delegate the behavior of a given parent of the type. This allows types like {@code JpaRepository}
 * implementations to specifically delegate to e.g. {@code HibernateJpaRepository}.
 *
 * <pre>{@code
 * public interface User extends ContextCarrier {
 *     String greeting();
 * }
 * public class ContextCarrierImpl implements ContextCarrier {
 *     @Override
 *     public ApplicationContext applicationContext() {
 *         return ...;
 *     }
 * }
 * }</pre>
 * <pre>{@code
 * final StateAwareProxyFactory<User, ?> factory = applicationManager.factory(User.class);
 * factory.delegate(ContextCarrier.class, new ContextCarrierImpl());
 * final User user = factory.proxy().get();
 * user.applicationContext(); // Returns a valid application context
 * user.greeting(); // Yields an exception as no implementation is assigned and the method is abstract
 * }</pre>
 *
 * <p>However, it is not unlikely a delegate returns itself in chained method calls. To avoid leaking the delegate, method handles always check if
 * the returned object is the delegate, and will replace it with the proxy instance if it is so.
 *
 * <pre>{@code
 * public interface Returner {
 *     Returner self();
 * }
 * public interface User extends Returner {
 *     String greeting();
 * }
 * public class ReturnerImpl implements Returner {
 *     @Override
 *     public Returner self() {
 *         return this;
 *     }
 * }
 * }</pre>
 * <pre>{@code
 * final StateAwareProxyFactory<User, ?> factory = applicationManager.factory(User.class);
 * factory.delegate(Returner.class, new ReturnerImpl());
 * final User user = factory.proxy().get();
 * user.self(); // Returns the user proxy object instead of the ReturnerImpl instance
 * }</pre>
 *
 * @param <T> The type of the proxy
 * @author Guus Lieben
 * @since 22.2
 */
public interface ProxyFactory<T> extends ModifiableProxyManager<T> {

    /**
     * Delegates all abstract methods defined by the active type to the given delegate
     * instance. This targets a backing implementation, not the original instance. Unlike {@link #delegate(Object)}
     * this will only delegate methods that do not already have a concrete implementation.
     *
     * @param delegate The delegate instance
     * @return This factory
     */
    ProxyFactory<T> delegateAbstract(T delegate);

    /**
     * Delegates all methods defined by the given {@code type} to the given delegate instance. This
     * targets a backing implementation, not the original instance.
     *
     * @param type The type of the delegate
     * @param delegate The delegate instance
     * @param <S> The type of the delegate
     * @return This factory
     */
    <S> ProxyFactory<T> delegate(Class<S> type, S delegate);

    /**
     * Delegates all methods defined by the given {@code type} which are not implemented to the given
     * delegate instance. This means any method which is still abstract at the top-level will be delegated,
     * and any method with a concrete implementation will invoke the default method without interception.
     * This targets a backing implementation, not the original instance.
     *
     * @param type The type of the delegate
     * @param delegate The delegate instance
     * @param <S> The type of the delegate
     * @return This factory
     */
    <S> ProxyFactory<T> delegateAbstract(Class<S> type, S delegate);

    /**
     * Delegates the given method to the given delegate instance. This targets a backing implementation,
     * not the original instance.
     *
     * @param method The method to delegate
     * @param delegate The delegate instance
     * @return This factory
     */
    ProxyFactory<T> delegate(MethodView<T, ?> method, T delegate);

    /**
     * Delegates the given method to the given delegate instance. This targets a backing implementation,
     * not the original instance.
     *
     * @param method The method to delegate
     * @param delegate The delegate instance
     * @return This factory
     */
    ProxyFactory<T> delegate(Method method, T delegate);

    /**
     * Intercepts the given method and replaces it with the given {@link MethodInterceptor}. If there is
     * already an interceptor for the given method, it will be chained, so it may be executed in series.
     *
     * @param method The method to intercept
     * @param interceptor The interceptor to use
     * @return This factory
     */
    <R> ProxyFactory<T> intercept(MethodView<T, R> method, MethodInterceptor<T, R> interceptor);

    /**
     * Intercepts the given method and replaces it with the given {@link MethodInterceptor}. If there is
     * already an interceptor for the given method, it will be chained, so it may be executed in series.
     *
     * @param method The method to intercept
     * @param interceptor The interceptor to use
     * @return This factory
     */
    ProxyFactory<T> intercept(Method method, MethodInterceptor<T, ?> interceptor);

    /**
     * Intercepts the given method and calls the given {@link MethodWrapper} for all known phases of the
     * wrapper. These phases are; before entry, after return, and after exception thrown.
     *
     * @param method The method to intercept
     * @param wrapper The wrapper to use
     * @return This factory
     * @see MethodWrapper
     */
    ProxyFactory<T> wrapAround(MethodView<T, ?> method, MethodWrapper<T> wrapper);

    /**
     * Intercepts the given method and calls the given {@link MethodWrapper} for all known phases of the
     * wrapper. These phases are; before entry, after return, and after exception thrown.
     *
     * @param method The method to intercept
     * @param wrapper The wrapper to use
     * @return This factory
     * @see MethodWrapper
     */
    ProxyFactory<T> wrapAround(Method method, MethodWrapper<T> wrapper);

    ProxyFactory<T> wrapAround(Method method, Consumer<MethodWrapperFactory<T>> wrapper);

    ProxyFactory<T> wrapAround(MethodView<T, ?> method, Consumer<MethodWrapperFactory<T>> wrapper);

    /**
     * Implements the given interfaces on the proxy. This will add the given interfaces to the list of
     * interfaces that the proxy implements. This will not replace existing interfaces. This will not
     * affect the interfaces that the proxy implements directly. This will not affect implemented methods,
     * and new interface methods will have to be implemented manually either through delegation or
     * intercepting.
     *
     * @param interfaces The interfaces to implement
     * @return This factory
     */
    ProxyFactory<T> implement(Class<?>... interfaces);

    ProxyFactory<T> defaultStub(MethodStub<T> stub);

    ProxyFactory<T> defaultStub(Supplier<MethodStub<T>> stub);

    /**
     * Creates a proxy instance of the active {@link #type()} and returns it. This will create a new proxy,
     * as well as a new {@link ProxyManager} responsible for managing the proxy. The proxy will be created
     * with all currently known behaviors.
     *
     * <p>If the proxy could not be created, {@link Attempt#empty()} will be returned. If the proxy is
     * absent, an exception will not always be thrown. It is up to the implementation to decide whether to
     * throw an {@link ApplicationException}, or use {@link Attempt#error()}.
     *
     * @return A proxy instance
     * @throws ApplicationException If the proxy could not be created
     */
    Attempt<T, Throwable> proxy() throws ApplicationException;

    /**
     * Creates a proxy instance of the given {@code type} and returns it. This will create a new proxy and
     * invokes the given {@link ConstructorView} to create the proxy instance. This also creates a new
     * {@link ProxyManager} responsible for managing the proxy. The proxy will be created with all currently
     * known behaviors.
     *
     * <p>If the proxy could not be created, {@link Attempt#empty()} will be returned. If the proxy is
     * absent, an exception will not always be thrown. It is up to the implementation to decide whether to
     * throw an {@link ApplicationException}, or use {@link Attempt#error()}.
     *
     * @param constructor The constructor to use
     * @param args The arguments to pass to the constructor
     * @return A proxy instance
     * @throws ApplicationException If the proxy could not be created
     */
    Attempt<T, Throwable> proxy(ConstructorView<T> constructor, Object[] args) throws ApplicationException;

    Attempt<T, Throwable> proxy(Constructor<T> constructor, Object[] args) throws ApplicationException;

    /**
     * Gets the type of the proxy. This will return the original type, and not a proxy type.
     * @return The type of the proxy
     */
    Class<T> type();

    /**
     * Gets the original instance delegate, if it was set through {@link #delegate(Object)}. This will
     * return {@code null} if no delegate was set.
     *
     * @return The original delegate instance, or {@code null}
     */
    @Nullable
    T typeDelegate();

    /**
     * Gets all known backing implementation delegates. This will return an empty map if no delegates were
     * set.
     *
     * @return All known delegates, or an empty map
     */
    Map<Method, Object> delegates();

    /**
     * Gets all known interceptors. This will return an empty map if no interceptors were set. If there
     * are multiple interceptors for the same method, they will be chained together.
     *
     * @return All known interceptors, or an empty map
     */
    Map<Method, MethodInterceptor<T, ?>> interceptors();

    /**
     * Gets all known wrappers. This will return an empty map if no wrappers were set. If there are
     * multiple wrappers for the same method, they will be separated into a single collection identified
     * by the method as the key.
     *
     * @return All known wrappers, or an empty map
     */
    MultiMap<Method, MethodWrapper<T>> wrappers();

    /**
     * Gets all known backing implementation delegates. This will return an empty map if no delegates were
     * set.
     * @return All known delegates, or an empty map
     */
    ConcurrentClassMap<Object> typeDelegates();

    /**
     * Gets all currently known interfaces. This will return an empty set if no interfaces were set. This
     * will not include {@link Proxy}.
     *
     * @return All known interfaces, or an empty set
     */
    Set<Class<?>> interfaces();

    /**
     * Gets a temporary context for the current proxy factory. When a new proxy is created, this
     * context will be assigned to its {@link ProxyManager}.
     * @return The temporary context
     */
    ProxyContextContainer contextContainer();

    /**
     * Gets the default {@link MethodStub} to use when a method is not implemented.
     * @return The default method stub
     */
    Supplier<MethodStub<T>> defaultStub();

    @Override
    ProxyFactory<T> delegate(T delegate);
}