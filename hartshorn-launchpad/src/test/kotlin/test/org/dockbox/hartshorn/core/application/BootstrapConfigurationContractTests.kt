/*
 * Copyright 2019-2024 the original author or authors.
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
package test.org.dockbox.hartshorn.core.application

import org.dockbox.hartshorn.inject.ExceptionHandler
import org.dockbox.hartshorn.inject.binding.Binder
import org.dockbox.hartshorn.inject.binding.DefaultBindingConfigurer
import org.dockbox.hartshorn.inject.component.ComponentRegistry
import org.dockbox.hartshorn.inject.condition.ConditionMatcher
import org.dockbox.hartshorn.inject.graph.ConfigurationDependencyVisitor
import org.dockbox.hartshorn.inject.graph.DependencyGraphBuilder
import org.dockbox.hartshorn.inject.graph.DependencyResolver
import org.dockbox.hartshorn.inject.graph.resolve.BindsMethodDependencyResolver
import org.dockbox.hartshorn.inject.introspect.ViewContextAdapter
import org.dockbox.hartshorn.inject.processing.construction.AnnotatedMethodComponentPostConstructor
import org.dockbox.hartshorn.inject.processing.construction.ComponentPostConstructor
import org.dockbox.hartshorn.inject.provider.ComponentProviderOrchestrator
import org.dockbox.hartshorn.inject.provider.HierarchicalComponentProviderOrchestrator
import org.dockbox.hartshorn.inject.provider.PostProcessingComponentProvider
import org.dockbox.hartshorn.launchpad.ApplicationContext
import org.dockbox.hartshorn.launchpad.DelegatingApplicationContext
import org.dockbox.hartshorn.launchpad.SimpleApplicationContext
import org.dockbox.hartshorn.launchpad.environment.*
import org.dockbox.hartshorn.launchpad.launch.ApplicationContextFactory
import org.dockbox.hartshorn.launchpad.launch.StandardApplicationBuilder
import org.dockbox.hartshorn.launchpad.launch.StandardApplicationContextFactory
import org.dockbox.hartshorn.proxy.ProxyOrchestrator
import org.dockbox.hartshorn.util.ContextualInitializer
import org.dockbox.hartshorn.util.Customizer
import org.dockbox.hartshorn.util.Initializer
import org.dockbox.hartshorn.util.StreamableConfigurer
import org.dockbox.hartshorn.util.introspect.annotations.AnnotationLookup
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.function.BiFunction
import java.util.function.Function
import java.util.stream.Stream

class BootstrapConfigurationContractTests {

    @Test
    fun testApplicationBuilderContract() {
        val instance = StandardApplicationBuilder.Configurer()

        assertDeferred(instance) { configurer, deferred: ApplicationContextFactory? -> configurer.applicationContextFactory(deferred) }
        assertInitializer(instance) { configurer, initializer -> configurer.applicationContextFactory(initializer) }
        assertContextInitializer(instance) { configurer, initializer -> configurer.applicationContextFactory(initializer) }

        assertCustom(instance) { configurer -> configurer.inferMainClass() }
        assertDeferred(instance) { configurer, deferred: Class<*>? -> configurer.mainClass(deferred) }
        assertInitializer(instance) { configurer, initializer -> configurer.mainClass(initializer) }

        assertDeferred(instance) { configurer, deferred: Array<String>? -> configurer.arguments(*(deferred ?: arrayOf("a"))) }
        assertDeferred(instance) { configurer, deferred: List<String>? -> configurer.arguments(deferred) }
        assertCustomizer(instance) { configurer, customizer -> configurer.arguments(customizer) }
    }

    @Test
    fun testApplicationConstructorContract() {
        val instance = StandardApplicationContextFactory.Configurer()

        assertCustomizer(instance) { configurer, customizer -> configurer.activators(customizer) }
        assertCustomizer(instance) { configurer, customizer -> configurer.componentPreProcessors(customizer) }
        assertCustomizer(instance) { configurer, customizer -> configurer.componentPostProcessors(customizer) }
        assertCustomizer(instance) { configurer, customizer -> configurer.standaloneComponents(customizer) }
        assertCustomizer(instance) { configurer, customizer -> configurer.scanPackages(customizer) }

        assertDeferred(instance) { configurer, deferred: ApplicationEnvironment? -> configurer.environment(deferred) }
        assertContextInitializer(instance) { configurer, initializer -> configurer.environment(initializer) }

        assertDeferred(instance) { configurer, deferred: Boolean? -> configurer.includeBasePackages(deferred ?: true) }
        assertContextInitializer(instance) { configurer, initializer -> configurer.includeBasePackages(initializer) }
    }

    @Test
    fun testContextualEnvironmentContract() {
        val instance = ContextualApplicationEnvironment.Configurer()

        assertContextInitializer(instance) { configurer, initializer -> configurer.enableBanner(initializer) }
        assertCustom(instance) { configurer -> configurer.enableBanner() }
        assertCustom(instance) { configurer -> configurer.disableBanner() }

        assertContextInitializer(instance) { configurer, initializer -> configurer.enableBatchMode(initializer) }
        assertCustom(instance) { configurer -> configurer.enableBatchMode() }
        assertCustom(instance) { configurer -> configurer.disableBatchMode() }

        assertContextInitializer(instance) { configurer, initializer -> configurer.showStacktraces(initializer) }
        assertCustom(instance) { configurer -> configurer.showStacktraces() }
        assertCustom(instance) { configurer -> configurer.hideStacktraces() }

        assertDeferred(instance) { configurer, deferred: ProxyOrchestrator? -> configurer.applicationOrchestrator(deferred) }
        assertContextInitializer(instance) { configurer, initializer -> configurer.applicationOrchestrator(initializer) }

        assertDeferred(instance) { configurer, deferred: FileSystemProvider? -> configurer.applicationFSProvider(deferred) }
        assertContextInitializer(instance) { configurer, initializer -> configurer.applicationFSProvider(initializer) }

        assertDeferred(instance) { configurer, deferred: ExceptionHandler? -> configurer.exceptionHandler(deferred) }
        assertContextInitializer(instance) { configurer, initializer -> configurer.exceptionHandler(initializer) }

        assertDeferred(instance) { configurer, deferred: ApplicationArgumentParser? -> configurer.applicationArgumentParser(deferred) }
        assertContextInitializer(instance) { configurer, initializer -> configurer.applicationArgumentParser(initializer) }

        assertDeferred(instance) { configurer, deferred: ClasspathResourceLocator? -> configurer.classpathResourceLocator(deferred) }
        assertContextInitializer(instance) { configurer, initializer -> configurer.classpathResourceLocator(initializer) }

        assertDeferred(instance) { configurer, deferred: AnnotationLookup? -> configurer.annotationLookup(deferred) }
        assertContextInitializer(instance) { configurer, initializer -> configurer.annotationLookup(initializer) }

        assertDeferred(instance) { configurer, deferred: ApplicationContext? -> configurer.applicationContext(deferred) }
        assertContextInitializer(instance) { configurer, initializer -> configurer.applicationContext(initializer) }

        assertDeferred(instance) { configurer, deferred: ComponentRegistry? -> configurer.componentRegistry(deferred) }
        assertContextInitializer(instance) { configurer, initializer -> configurer.componentRegistry(initializer) }

    }

    @Test
    fun testSimpleApplicationContextContract() {
        val instance = SimpleApplicationContext.Configurer()

        assertDeferred(instance) { configurer, deferred: org.dockbox.hartshorn.inject.graph.DependencyGraphInitializer? -> configurer.dependencyGraphInitializer(deferred) }
        assertContextInitializer(instance) { configurer, initializer -> configurer.dependencyGraphInitializer(initializer) }
    }

    @Test
    fun testDelegatingApplicationContextContract() {
        val instance = DelegatingApplicationContext.Configurer()

        assertDeferred(instance) { configurer, deferred: ComponentProviderOrchestrator? -> configurer.componentProvider(deferred) }
        assertContextInitializer(instance) { configurer, initializer -> configurer.componentProvider(initializer) }

        assertDeferred(instance) { configurer, deferred: DefaultBindingConfigurer? -> configurer.defaultBindings(deferred) }
        assertContextInitializer(instance) { configurer, initializer -> configurer.defaultBindings(initializer) }

        val biConsumerDefaultBindingsResult = instance.defaultBindings { _: ApplicationContext, _: Binder -> }
        Assertions.assertSame(instance, biConsumerDefaultBindingsResult)
    }

    @Test
    fun testScopeAwareComponentProviderContract() {
        val instance = HierarchicalComponentProviderOrchestrator.Configurer()

        assertDeferred(instance) { configurer, deferred: ComponentPostConstructor? -> configurer.componentPostConstructor(deferred) }
        assertContextInitializer(instance) { configurer, initializer -> configurer.componentPostConstructor(initializer) }
    }

    @Test
    fun testComponentPostConstructorImplContract() {
        val instance = AnnotatedMethodComponentPostConstructor.Configurer()

        assertDeferred(instance) { configurer, deferred: ViewContextAdapter? -> configurer.viewContextAdapter(deferred) }
        assertContextInitializer(instance) { configurer, initializer -> configurer.viewContextAdapter(initializer) }
    }

    @Test
    fun testDependencyGraphInitializerContract() {
        val instance = org.dockbox.hartshorn.inject.graph.DependencyGraphInitializer.Configurer()

        assertDeferred(instance) { configurer, deferred: DependencyResolver? -> configurer.dependencyResolver(deferred) }
        assertContextInitializer(instance) { configurer, initializer -> configurer.dependencyResolver(initializer) }

        assertDeferred(instance) { configurer, deferred: DependencyGraphBuilder? -> configurer.dependencyGraphBuilder(deferred) }
        assertContextInitializer(instance) { configurer, initializer -> configurer.dependencyGraphBuilder(initializer) }

        assertDeferred(instance) { configurer, deferred: ConfigurationDependencyVisitor? -> configurer.dependencyVisitor(deferred) }
        assertContextInitializer(instance) { configurer, initializer -> configurer.dependencyVisitor(initializer) }
    }

    @Test
    fun testApplicationDependencyResolverContract() {
        val instance = BindsMethodDependencyResolver.Configurer()

        assertDeferred(instance) { configurer, deferred: ConditionMatcher? -> configurer.conditionMatcher(deferred) }
        assertContextInitializer(instance) { configurer, initializer -> configurer.conditionMatcher(initializer) }
    }

    @Test
    fun testStreamableConfigurerContract() {
        val instance = StreamableConfigurer.empty<Any, Any>()

        var result: StreamableConfigurer<Any, Any>? = instance.add(null as Any?)
        Assertions.assertSame(instance, result)

        result = instance.add(Initializer.of(null))
        Assertions.assertSame(instance, result)

        result = instance.add(ContextualInitializer.of(null as Any?))
        Assertions.assertSame(instance, result)

        result = instance.addAll(listOf(null as Any?))
        Assertions.assertSame(instance, result)

        result = instance.addAll(*arrayOf(null as Any?))
        Assertions.assertSame(instance, result)

        result = instance.addAll(listOf(Initializer.of(null)))
        Assertions.assertSame(instance, result)

        result = instance.addAll(*arrayOf(ContextualInitializer.of(null as Any?)))
        Assertions.assertSame(instance, result)

        result = instance.remove(ContextualInitializer.of(null as Any?))
        Assertions.assertSame(instance, result)

        result = instance.clear()
        Assertions.assertSame(instance, result)

        val stream: Stream<*> = instance.stream()
        Assertions.assertNotNull(stream)
    }

    fun <T> assertCustom(configurer: T, deferredFunction: Function<T, T>) {
        val result = deferredFunction.apply(configurer)
        Assertions.assertSame(configurer, result)
    }

    fun <T, C> assertDeferred(configurer: T, deferredFunction: BiFunction<T, C?, T>) {
        val result = deferredFunction.apply(configurer, null)
        Assertions.assertSame(configurer, result)
    }

    fun <T, I, C> assertContextInitializer(configurer: T, initializerFunction: BiFunction<T, ContextualInitializer<I, C?>, T>) {
        val result = initializerFunction.apply(configurer) { _ -> null }
        Assertions.assertSame(configurer, result)
    }

    fun <T, C> assertInitializer(configurer: T, initializerFunction: BiFunction<T, Initializer<C?>, T>) {
        val result = initializerFunction.apply(configurer) { null }
        Assertions.assertSame(configurer, result)
    }

    fun <T, C> assertCustomizer(configurer: T, customizableFunction: BiFunction<T, Customizer<C>, T>) {
        val result = customizableFunction.apply(configurer) { _: C -> }
        Assertions.assertSame(configurer, result)
    }
}
