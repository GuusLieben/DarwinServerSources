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

package org.dockbox.hartshorn.launchpad.environment;

import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.dockbox.hartshorn.inject.InjectorConfiguration;
import org.dockbox.hartshorn.inject.component.ApplicationMainComponentContainer;
import org.dockbox.hartshorn.inject.component.ComponentRegistry;
import org.dockbox.hartshorn.launchpad.DelegatingApplicationContext;
import org.dockbox.hartshorn.launchpad.component.TypeReferenceLookupComponentRegistry;
import org.dockbox.hartshorn.launchpad.launch.ApplicationBootstrapContext;
import org.dockbox.hartshorn.inject.ExceptionHandler;
import org.dockbox.hartshorn.inject.LoggingExceptionHandler;
import org.dockbox.hartshorn.inject.ComponentKeyResolver;
import org.dockbox.hartshorn.inject.StandardAnnotationComponentKeyResolver;
import org.dockbox.hartshorn.inject.environment.DefaultProxyOrchestratorLoader;
import org.dockbox.hartshorn.inject.targets.ComponentInjectionPointsResolver;
import org.dockbox.hartshorn.inject.targets.MethodsAndFieldsInjectionPointResolver;
import org.dockbox.hartshorn.launchpad.ApplicationContext;
import org.dockbox.hartshorn.launchpad.SimpleApplicationContext;
import org.dockbox.hartshorn.launchpad.banner.Banner;
import org.dockbox.hartshorn.launchpad.banner.HartshornLogoBanner;
import org.dockbox.hartshorn.launchpad.banner.ResourcePathBanner;
import org.dockbox.hartshorn.launchpad.context.ModifiableApplicationContextCarrier;
import org.dockbox.hartshorn.launchpad.lifecycle.ObservableApplicationEnvironment;
import org.dockbox.hartshorn.launchpad.lifecycle.Observer;
import org.dockbox.hartshorn.proxy.ProxyOrchestrator;
import org.dockbox.hartshorn.spi.DiscoveryService;
import org.dockbox.hartshorn.spi.ServiceDiscoveryException;
import org.dockbox.hartshorn.util.ApplicationRuntimeException;
import org.dockbox.hartshorn.util.ContextualInitializer;
import org.dockbox.hartshorn.util.Customizer;
import org.dockbox.hartshorn.util.Initializer;
import org.dockbox.hartshorn.util.SingleElementContext;
import org.dockbox.hartshorn.util.introspect.BatchCapableIntrospector;
import org.dockbox.hartshorn.util.introspect.Introspector;
import org.dockbox.hartshorn.util.introspect.IntrospectorLoader;
import org.dockbox.hartshorn.util.introspect.ProxyLookup;
import org.dockbox.hartshorn.util.introspect.SupplierAdapterProxyLookup;
import org.dockbox.hartshorn.util.introspect.annotations.AnnotationLookup;
import org.dockbox.hartshorn.util.introspect.annotations.VirtualHierarchyAnnotationLookup;
import org.dockbox.hartshorn.util.introspect.scan.TypeReferenceCollectorContext;
import org.dockbox.hartshorn.util.introspect.view.TypeView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO: #1060 Add documentation
 *
 * @since 0.4.8
 *
 * @author Guus Lieben
 */
public final class ContextualApplicationEnvironment implements ObservableApplicationEnvironment, ModifiableApplicationContextCarrier {

    private final Set<Observer> observers = ConcurrentHashMap.newKeySet();
    private final Set<Class<? extends Observer>> lazyObservers = ConcurrentHashMap.newKeySet();

    private final FileSystemProvider fileSystemProvider;
    private final ProxyOrchestrator proxyOrchestrator;
    private final ExceptionHandler exceptionHandler;
    private final AnnotationLookup annotationLookup;
    private final ClasspathResourceLocator classPathResourceLocator;

    private final ComponentInjectionPointsResolver injectionPointsResolver;
    private final ComponentKeyResolver componentKeyResolver;
    private final EnvironmentTypeResolver typeResolver;
    private final ComponentRegistry componentRegistry;

    private final boolean isBuildEnvironment;
    private final boolean isBatchMode;
    private final boolean isStrictMode;

    private final ApplicationArgumentParser argumentParser;
    private final Properties arguments;

    private ApplicationContext applicationContext;
    private Introspector introspector;

    private ContextualApplicationEnvironment(SingleElementContext<? extends ApplicationBootstrapContext> context, Configurer configurer) {
        SingleElementContext<ApplicationEnvironment> environmentInitializerContext = context.transform(this);
        environmentInitializerContext.addContext(context.input());

        this.exceptionHandler = this.configure(environmentInitializerContext, configurer.exceptionHandler);
        this.annotationLookup = this.configure(environmentInitializerContext, configurer.annotationLookup);
        this.proxyOrchestrator = this.configure(environmentInitializerContext.transform(this.introspector()), configurer.proxyOrchestrator);
        this.fileSystemProvider = this.configure(environmentInitializerContext, configurer.applicationFSProvider);
        this.argumentParser = this.configure(environmentInitializerContext, configurer.applicationArgumentParser);
        this.classPathResourceLocator = this.configure(environmentInitializerContext, configurer.classpathResourceLocator);
        this.injectionPointsResolver = this.configure(environmentInitializerContext, configurer.injectionPointsResolver);
        this.componentKeyResolver = this.configure(environmentInitializerContext, configurer.componentKeyResolver);
        this.typeResolver = this.configure(environmentInitializerContext, configurer.typeResolver);
        this.componentRegistry = this.configure(environmentInitializerContext, configurer.componentRegistry);

        this.arguments = this.argumentParser.parse(context.input().arguments());

        SingleElementContext<Properties> argumentsInitializerContext = context.transform(this.arguments);
        this.printStackTraces(configurer.showStacktraces.initialize(argumentsInitializerContext));
        this.isBatchMode = configurer.enableBatchMode.initialize(argumentsInitializerContext);
        this.isStrictMode = configurer.enableStrictMode.initialize(argumentsInitializerContext);
        if (this.introspector() instanceof BatchCapableIntrospector batchCapableIntrospector) {
            batchCapableIntrospector.enableBatchMode(this.isBatchMode());
        }

        Boolean isBuildEnvironment = configurer.isBuildEnvironment.initialize(environmentInitializerContext);
        if (isBuildEnvironment == null) {
            isBuildEnvironment = false;
        }
        this.isBuildEnvironment = isBuildEnvironment;

        if (!this.isBuildEnvironment && configurer.enableBanner.initialize(argumentsInitializerContext)) {
            this.printBanner(context.input().mainClass());
        }

        ApplicationContext initializedContext = configurer.applicationContext.initialize(environmentInitializerContext);
        // This will handle two aspects:
        // 1. If the context was not initialized through the implementation of ModifiableContextCarrier, it
        //    will be set here to the initialized context.
        // 2. If the context was initialized through the implementation of ModifiableContextCarrier, it will
        //    verify that the context is the same as the initialized context, or throw an exception to prevent
        //    the context from being overwritten and leaving the application in an inconsistent state.
        if (initializedContext != null) {
            this.applicationContext(initializedContext);
        }
    }

    private <I, T> T configure(SingleElementContext<I> context, ContextualInitializer<I, T> initializer) {
        T instance = initializer.initialize(context);
        return this.configure(instance);
    }

    @Override
    public Properties rawArguments() {
        return this.arguments;
    }

    @Override
    public ComponentKeyResolver componentKeyResolver() {
        return this.componentKeyResolver;
    }

    @Override
    public ComponentInjectionPointsResolver injectionPointsResolver() {
        return this.injectionPointsResolver;
    }

    private <T> T configure(T instance) {
        if (instance instanceof ApplicationManaged managed) {
            managed.environment(this);
        }
        return instance;
    }

    public FileSystemProvider applicationFSProvider() {
        return this.fileSystemProvider;
    }

    @Override
    public ExceptionHandler exceptionHandler() {
        return this.exceptionHandler;
    }

    public AnnotationLookup annotationLookup() {
        return this.annotationLookup;
    }

    @Override
    public FileSystemProvider fileSystem() {
        return this.fileSystemProvider;
    }

    @Override
    public ClasspathResourceLocator classpath() {
        return this.classPathResourceLocator;
    }

    @Override
    public ProxyOrchestrator proxyOrchestrator() {
        return this.proxyOrchestrator;
    }

    @Override
    public InjectorConfiguration configuration() {
        return new InjectorConfiguration() {

            @Override
            public boolean isStrictMode() {
                return ContextualApplicationEnvironment.this.isStrictMode;
            }
        };
    }

    @Override
    public Introspector introspector() {
        if (this.introspector == null) {
            // Lazy, as the proxy orchestrator may not yet be initialized
            ProxyLookup proxyLookup = new SupplierAdapterProxyLookup(() -> this.proxyOrchestrator);
            try {
                this.introspector = DiscoveryService.instance()
                    .discover(IntrospectorLoader.class)
                    .create(proxyLookup, this.annotationLookup());
            }
            catch (ServiceDiscoveryException e) {
                throw new ApplicationRuntimeException(e);
            }
        }
        return this.introspector;
    }

    @Override
    public EnvironmentTypeResolver typeResolver() {
        return this.typeResolver;
    }

    @Override
    public boolean isBuildEnvironment() {
        return this.isBuildEnvironment;
    }

    @Override
    public boolean isBatchMode() {
        return this.isBatchMode;
    }

    @Override
    public boolean isStrictMode() {
        return this.isStrictMode;
    }

    @Override
    public ApplicationContext applicationContext() {
        return this.applicationContext;
    }

    @Override
    public ComponentRegistry componentRegistry() {
        return this.componentRegistry;
    }
    @Override
    public void handle(Throwable throwable) {
        this.exceptionHandler.handle(throwable);
    }

    @Override
    public void handle(String message, Throwable throwable) {
        this.exceptionHandler.handle(message, throwable);
    }

    @Override
    public ExceptionHandler printStackTraces(boolean stacktraces) {
        return this.exceptionHandler.printStackTraces(stacktraces);
    }

    @Override
    public void register(Observer observer) {
        this.observers.add(observer);
    }

    @Override
    public void register(Class<? extends Observer> observer) {
        this.lazyObservers.add(observer);
    }

    @Override
    public <T extends Observer> Set<T> observers(Class<T> type) {
        if (type == null) {
            throw new IllegalArgumentException("type cannot be null");
        }

        Set<T> observers = new HashSet<>();
        this.observers.stream()
                .filter(type::isInstance)
                .map(type::cast)
                .forEach(observers::add);

        this.lazyObservers.stream()
                .filter(type::isAssignableFrom)
                .map(this.applicationContext::get)
                .map(type::cast)
                .forEach(observers::add);

        return observers;
    }

    private void printBanner(Class<?> mainClass) {
        Logger logger = LoggerFactory.getLogger(mainClass);
        this.createBanner().print(logger);
    }

    private Banner createBanner() {
        try {
            return this.classPathResourceLocator.resource("banner.txt")
                    .map(resource -> (Banner) new ResourcePathBanner(resource))
                    .orElseGet(HartshornLogoBanner::new);
        }
        catch (IOException e) {
            return new HartshornLogoBanner();
        }
    }

    /**
     * Creates a new {@link ContextualInitializer} for the {@link ContextualApplicationEnvironment} using the given
     * {@link Customizer}.
     *
     * @param customizer the customizer to use, if left empty the default configuration will be used
     * @return a non-cached {@link ContextualInitializer} for the {@link ContextualApplicationEnvironment}
     */
    public static ContextualInitializer<ApplicationBootstrapContext, ContextualApplicationEnvironment> create(Customizer<Configurer> customizer) {
        return context -> {
            Configurer configurer = new Configurer();
            customizer.configure(configurer);
            return new ContextualApplicationEnvironment(context, configurer);
        };
    }

    @Override
    public ModifiableApplicationContextCarrier applicationContext(ApplicationContext context) {
        if (this.applicationContext != null && this.applicationContext != context) {
            throw new IllegalStateException("Application context already set");
        }
        this.applicationContext = context;
        return this;
    }

    /**
     * TODO: #1060 Add documentation
     *
     * @since 0.5.0
     *
     * @author Guus Lieben
     */
    public static class Configurer {

        private ContextualInitializer<Properties, Boolean> enableBanner = ContextualInitializer.of(properties -> Boolean.valueOf(properties.getProperty("hartshorn.banner.enabled", "true")));
        private ContextualInitializer<Properties, Boolean> enableBatchMode = ContextualInitializer.of(properties -> Boolean.valueOf(properties.getProperty("hartshorn.batch.enabled", "false")));
        private ContextualInitializer<Properties, Boolean> enableStrictMode = ContextualInitializer.of(properties -> Boolean.valueOf(properties.getProperty("hartshorn.strict.enabled", "true")));
        private ContextualInitializer<Properties, Boolean> showStacktraces = ContextualInitializer.of(properties -> Boolean.valueOf(properties.getProperty("hartshorn.exceptions.stacktraces", "true")));

        private ContextualInitializer<ApplicationEnvironment, EnvironmentTypeResolver> typeResolver = context -> {
            TypeReferenceCollectorContext collectorContext = context.firstContext(TypeReferenceCollectorContext.class)
                    .orElseGet(TypeReferenceCollectorContext::new);
            return new ClassPathEnvironmentTypeResolver(new EnvironmentTypeCollector(context.input(), collectorContext));
        };

        private ContextualInitializer<ApplicationEnvironment, ? extends ComponentRegistry> componentRegistry = context -> {
            ApplicationEnvironment environment = context.input();
            TypeReferenceLookupComponentRegistry registry = new TypeReferenceLookupComponentRegistry(environment.typeResolver());
            context.firstContext(ApplicationBootstrapContext.class)
                    .peek(bootstrap -> {
                        TypeView<?> mainClass = environment.introspector().introspect(bootstrap.mainClass());
                        registry.addCustomContainer(new ApplicationMainComponentContainer<>(mainClass));
                    });
            return registry;
        };
        private ContextualInitializer<Introspector, ? extends ProxyOrchestrator> proxyOrchestrator = DefaultProxyOrchestratorLoader.create(Customizer.useDefaults());
        private ContextualInitializer<ApplicationEnvironment, ? extends FileSystemProvider> applicationFSProvider = ContextualInitializer.of(PathFileSystemProvider::new);
        private ContextualInitializer<ApplicationEnvironment, ? extends ExceptionHandler> exceptionHandler = ContextualInitializer.of(LoggingExceptionHandler::new);
        private ContextualInitializer<ApplicationEnvironment, ? extends ApplicationArgumentParser> applicationArgumentParser = ContextualInitializer.of(StandardApplicationArgumentParser::new);
        private ContextualInitializer<ApplicationEnvironment, ? extends ClasspathResourceLocator> classpathResourceLocator = ContextualInitializer.of(ClassLoaderClasspathResourceLocator::new);
        private ContextualInitializer<ApplicationEnvironment, ? extends AnnotationLookup> annotationLookup = ContextualInitializer.of(VirtualHierarchyAnnotationLookup::new);
        private ContextualInitializer<ApplicationEnvironment, ? extends ApplicationContext> applicationContext = SimpleApplicationContext.create(Customizer.useDefaults());
        private ContextualInitializer<ApplicationEnvironment, Boolean> isBuildEnvironment = ContextualInitializer.of(environment -> BuildEnvironmentPredicate.isBuildEnvironment());
        private ContextualInitializer<ApplicationEnvironment, ComponentInjectionPointsResolver> injectionPointsResolver = ContextualInitializer.defer(() -> MethodsAndFieldsInjectionPointResolver.create(Customizer.useDefaults()));
        private ContextualInitializer<ApplicationEnvironment, ComponentKeyResolver> componentKeyResolver = ContextualInitializer.of(StandardAnnotationComponentKeyResolver::new);

        /**
         * Enables or disables the banner. If the banner is enabled, it will be printed to the console when the
         * application starts. The banner is enabled by default.
         *
         * @param enableBanner whether to enable or disable the banner
         * @return the current {@link Configurer} instance
         */
        public Configurer enableBanner(ContextualInitializer<Properties, Boolean> enableBanner) {
            this.enableBanner = enableBanner;
            return this;
        }

        /**
         * Enables the banner. If the banner is enabled, it will be printed to the console when the application
         * starts. The banner is enabled by default.
         *
         * @return the current {@link Configurer} instance
         */
        public Configurer enableBanner() {
            return this.enableBanner(ContextualInitializer.of(true));
        }

        /**
         * Disables the banner. If the banner is disabled, it will not be printed to the console when the application
         * starts. The banner is enabled by default.
         *
         * @return the current {@link Configurer} instance
         */
        public Configurer disableBanner() {
            return this.enableBanner(ContextualInitializer.of(false));
        }

        /**
         * Enables or disables batch mode. Batch mode is typically used for optimizations specific to applications
         * which will spawn multiple application contexts with shared resources. Batch mode is disabled by default.
         *
         * @param enableBatchMode whether to enable or disable batch mode
         * @return the current {@link Configurer} instance
         */
        public Configurer enableBatchMode(ContextualInitializer<Properties, Boolean> enableBatchMode) {
            this.enableBatchMode = enableBatchMode;
            return this;
        }

        /**
         * Enables strict mode. Strict mode is typically used to indicate that a lookup should only return a value if
         * it is explicitly bound to the key, and not if it is bound to a sub-type of the key.
         *
         * @return the current {@link Configurer} instance
         */
        public Configurer enableStrictMode() {
            return this.enableStrictMode(ContextualInitializer.of(true));
        }

        /**
         * Disables strict mode. Strict mode is typically used to indicate that a lookup should only return a value if
         * it is explicitly bound to the key, and not if it is bound to a sub-type of the key.
         *
         * @return the current {@link Configurer} instance
         */
        public Configurer disableStrictMode() {
            return this.enableStrictMode(ContextualInitializer.of(false));
        }

        /**
         * Enables or disables strict mode. Strict mode is typically used to indicate that a lookup should only return a
         * value if it is explicitly bound to the key, and not if it is bound to a sub-type of the key.
         *
         * @param enableStrictMode whether to enable or disable strict mode
         * @return the current {@link Configurer} instance
         */
        public Configurer enableStrictMode(ContextualInitializer<Properties, Boolean> enableStrictMode) {
            this.enableStrictMode = enableStrictMode;
            return this;
        }

        /**
         * Enables batch mode. Batch mode is typically used for optimizations specific to applications which will
         * spawn multiple application contexts with shared resources. Batch mode is disabled by default.
         *
         * @return the current {@link Configurer} instance
         */
        public Configurer enableBatchMode() {
            return this.enableBatchMode(ContextualInitializer.of(true));
        }

        /**
         * Disables batch mode. Batch mode is typically used for optimizations specific to applications which will
         * spawn multiple application contexts with shared resources. Batch mode is disabled by default.
         *
         * @return the current {@link Configurer} instance
         */
        public Configurer disableBatchMode() {
            return this.enableBatchMode(ContextualInitializer.of(false));
        }

        /**
         * Enables or disables the printing of stacktraces when exceptions occur. Stacktraces are enabled by default.
         *
         * @param showStacktraces whether to enable or disable stacktraces
         * @return the current {@link Configurer} instance
         */
        public Configurer showStacktraces(ContextualInitializer<Properties, Boolean> showStacktraces) {
            this.showStacktraces = showStacktraces;
            return this;
        }

        /**
         * Enables the printing of stacktraces when exceptions occur. Stacktraces are enabled by default.
         *
         * @return the current {@link Configurer} instance
         */
        public Configurer showStacktraces() {
            return this.showStacktraces(ContextualInitializer.of(true));
        }

        /**
         * Disables the printing of stacktraces when exceptions occur. Stacktraces are enabled by default.
         *
         * @return the current {@link Configurer} instance
         */
        public Configurer hideStacktraces() {
            return this.showStacktraces(ContextualInitializer.of(false));
        }

        /**
         * Configures the {@link ComponentRegistry} that is used by the {@link DelegatingApplicationContext} to locate
         * components.
         *
         * @param componentRegistry the {@link ComponentRegistry} to use
         * @return the current instance
         */
        public Configurer componentRegistry(ComponentRegistry componentRegistry) {
            return this.componentRegistry(ContextualInitializer.of(componentRegistry));
        }

        /**
         * Configures the {@link ComponentRegistry} that is used by the {@link DelegatingApplicationContext} to locate
         * components.
         *
         * @param componentRegistry the {@link ComponentRegistry} to use
         * @return the current instance
         */
        public Configurer componentRegistry(ContextualInitializer<ApplicationEnvironment, ? extends ComponentRegistry> componentRegistry) {
            this.componentRegistry = componentRegistry;
            return this;
        }

        /**
         * Sets the {@link ProxyOrchestrator} to use. The {@link ProxyOrchestrator} is responsible for creating
         * proxies for application components. The default implementation is provided by {@link DefaultProxyOrchestratorLoader}.
         *
         * @param proxyOrchestrator the {@link ProxyOrchestrator} to use
         *
         * @return the current {@link Configurer} instance
         *
         * @see ProxyOrchestrator
         */
        public Configurer applicationOrchestrator(ProxyOrchestrator proxyOrchestrator) {
            return this.applicationOrchestrator(ContextualInitializer.of(proxyOrchestrator));
        }

        /**
         * Sets the {@link ProxyOrchestrator} to use. The {@link ProxyOrchestrator} is responsible for creating
         * proxies for application components. The default implementation is provided by {@link DefaultProxyOrchestratorLoader}.
         *
         * @param orchestrator the {@link ProxyOrchestrator} to use
         *
         * @return the current {@link Configurer} instance
         *
         * @see ProxyOrchestrator
         */
        public Configurer applicationOrchestrator(ContextualInitializer<Introspector, ? extends ProxyOrchestrator> orchestrator) {
            this.proxyOrchestrator = orchestrator;
            return this;
        }

        /**
         * Sets the {@link FileSystemProvider} to use. The {@link FileSystemProvider} is responsible for
         * providing the application's file system. The default implementation is {@link PathFileSystemProvider}.
         *
         * @param fileSystemProvider the {@link FileSystemProvider} to use
         *
         * @return the current {@link Configurer} instance
         *
         * @see FileSystemProvider
         */
        public Configurer applicationFSProvider(FileSystemProvider fileSystemProvider) {
            return this.applicationFSProvider(ContextualInitializer.of(fileSystemProvider));
        }

        /**
         * Sets the {@link FileSystemProvider} to use. The {@link FileSystemProvider} is responsible for
         * providing the application's file system. The default implementation is {@link PathFileSystemProvider}.
         *
         * @param applicationFSProvider the {@link FileSystemProvider} to use
         *
         * @return the current {@link Configurer} instance
         *
         * @see FileSystemProvider
         */
        public Configurer applicationFSProvider(ContextualInitializer<ApplicationEnvironment, ? extends FileSystemProvider> applicationFSProvider) {
            this.applicationFSProvider = applicationFSProvider;
            return this;
        }

        /**
         * Sets the {@link ExceptionHandler} to use. The {@link ExceptionHandler} is responsible for handling
         * exceptions that occur during the application's lifecycle. The default implementation is {@link LoggingExceptionHandler}.
         *
         * @param exceptionHandler the {@link ExceptionHandler} to use
         *
         * @return the current {@link Configurer} instance
         *
         * @see ExceptionHandler
         */
        public Configurer exceptionHandler(ExceptionHandler exceptionHandler) {
            return this.exceptionHandler(ContextualInitializer.of(exceptionHandler));
        }

        /**
         * Sets the {@link ExceptionHandler} to use. The {@link ExceptionHandler} is responsible for handling
         * exceptions that occur during the application's lifecycle. The default implementation is {@link LoggingExceptionHandler}.
         *
         * @param exceptionHandler the {@link ExceptionHandler} to use
         *
         * @return the current {@link Configurer} instance
         *
         * @see ExceptionHandler
         */
        public Configurer exceptionHandler(ContextualInitializer<ApplicationEnvironment, ? extends ExceptionHandler> exceptionHandler) {
            this.exceptionHandler = exceptionHandler;
            return this;
        }

        /**
         * Sets the {@link ApplicationArgumentParser} to use. The {@link ApplicationArgumentParser} is responsible for
         * parsing arguments passed to the application. The default implementation is {@link StandardApplicationArgumentParser}.
         *
         * @param applicationArgumentParser the {@link ApplicationArgumentParser} to use
         *
         * @return the current {@link Configurer} instance
         *
         * @see ApplicationArgumentParser
         */
        public Configurer applicationArgumentParser(ApplicationArgumentParser applicationArgumentParser) {
            return this.applicationArgumentParser(ContextualInitializer.of(applicationArgumentParser));
        }

        /**
         * Sets the {@link ApplicationArgumentParser} to use. The {@link ApplicationArgumentParser} is responsible for
         * parsing arguments passed to the application. The default implementation is {@link StandardApplicationArgumentParser}.
         *
         * @param applicationArgumentParser the {@link ApplicationArgumentParser} to use
         *
         * @return the current {@link Configurer} instance
         *
         * @see ApplicationArgumentParser
         */
        public Configurer applicationArgumentParser(ContextualInitializer<ApplicationEnvironment, ? extends ApplicationArgumentParser> applicationArgumentParser) {
            this.applicationArgumentParser = applicationArgumentParser;
            return this;
        }

        /**
         * Sets the {@link ClasspathResourceLocator} to use. The {@link ClasspathResourceLocator} is responsible for
         * locating resources on the classpath. The default implementation is {@link ClassLoaderClasspathResourceLocator}.
         *
         * @param classpathResourceLocator the {@link ClasspathResourceLocator} to use
         *
         * @return the current {@link Configurer} instance
         *
         * @see ClasspathResourceLocator
         */
        public Configurer classpathResourceLocator(ClasspathResourceLocator classpathResourceLocator) {
            return this.classpathResourceLocator(ContextualInitializer.of(classpathResourceLocator));
        }

        /**
         * Sets the {@link ClasspathResourceLocator} to use. The {@link ClasspathResourceLocator} is responsible for
         * locating resources on the classpath. The default implementation is {@link ClassLoaderClasspathResourceLocator}.
         *
         * @param classpathResourceLocator the {@link ClasspathResourceLocator} to use
         *
         * @return the current {@link Configurer} instance
         *
         * @see ClasspathResourceLocator
         */
        public Configurer classpathResourceLocator(ContextualInitializer<ApplicationEnvironment, ? extends ClasspathResourceLocator> classpathResourceLocator) {
            this.classpathResourceLocator = classpathResourceLocator;
            return this;
        }

        /**
         * Sets the {@link AnnotationLookup} to use. The {@link AnnotationLookup} is responsible for looking up
         * annotations on elements. The default implementation is {@link VirtualHierarchyAnnotationLookup}.
         *
         * @param annotationLookup the {@link AnnotationLookup} to use
         *
         * @return the current {@link Configurer} instance
         *
         * @see AnnotationLookup
         */
        public Configurer annotationLookup(AnnotationLookup annotationLookup) {
            return this.annotationLookup(ContextualInitializer.of(annotationLookup));
        }

        /**
         * Sets the {@link AnnotationLookup} to use. The {@link AnnotationLookup} is responsible for looking up
         * annotations on elements. The default implementation is {@link VirtualHierarchyAnnotationLookup}.
         *
         * @param annotationLookup the {@link AnnotationLookup} to use
         *
         * @return the current {@link Configurer} instance
         *
         * @see AnnotationLookup
         */
        public Configurer annotationLookup(ContextualInitializer<ApplicationEnvironment, ? extends AnnotationLookup> annotationLookup) {
            this.annotationLookup = annotationLookup;
            return this;
        }

        /**
         * Sets the {@link ApplicationContext} to use. The {@link ApplicationContext} is responsible for providing
         * access to components and global application state. The default implementation is {@link SimpleApplicationContext}.
         *
         * @param applicationContext the {@link ApplicationContext} to use
         *
         * @return the current {@link Configurer} instance
         *
         * @see ApplicationContext
         */
        public Configurer applicationContext(ApplicationContext applicationContext) {
            return this.applicationContext(ContextualInitializer.of(applicationContext));
        }

        /**
         * Sets the {@link ApplicationContext} to use. The {@link ApplicationContext} is responsible for providing
         * access to components and global application state. The default implementation is {@link SimpleApplicationContext}.
         *
         * @param applicationContext the {@link ApplicationContext} to use
         *
         * @return the current {@link Configurer} instance
         *
         * @see ApplicationContext
         */
        public Configurer applicationContext(ContextualInitializer<ApplicationEnvironment, ? extends ApplicationContext> applicationContext) {
            this.applicationContext = applicationContext;
            return this;
        }

        /**
         * Sets whether the application is running in a build environment. This is typically used to disable
         * certain features that are not required in a build environment. By default this will follow the result
         * of
         *
         * @param isBuildEnvironment whether the application is running in a build environment
         *
         * @return the current {@link Configurer} instance
         */
        public Configurer isBuildEnvironment(ContextualInitializer<ApplicationEnvironment, Boolean> isBuildEnvironment) {
            this.isBuildEnvironment = isBuildEnvironment;
            return this;
        }

        /**
         * Sets whether the application is running in a build environment. This is typically used to disable
         * certain features that are not required in a build environment. This is disabled by default.
         *
         * @param isBuildEnvironment whether the application is running in a build environment
         *
         * @return the current {@link Configurer} instance
         */
        public Configurer isBuildEnvironment(boolean isBuildEnvironment) {
            return this.isBuildEnvironment(ContextualInitializer.of(isBuildEnvironment));
        }

        public Configurer injectionPointsResolver(ComponentInjectionPointsResolver injectionPointsResolver) {
            return this.injectionPointsResolver(ContextualInitializer.of(() -> injectionPointsResolver));
        }

        public Configurer injectionPointsResolver(ContextualInitializer<ApplicationEnvironment, ComponentInjectionPointsResolver> injectionPointsResolver) {
            this.injectionPointsResolver = injectionPointsResolver;
            return this;
        }

        public Configurer componentKeyResolver(ComponentKeyResolver componentKeyResolver) {
            return this.componentKeyResolver(ContextualInitializer.of(componentKeyResolver));
        }

        public Configurer componentKeyResolver(Initializer<ComponentKeyResolver> componentKeyResolver) {
            return this.componentKeyResolver(ContextualInitializer.of(componentKeyResolver));
        }

        public Configurer componentKeyResolver(ContextualInitializer<ApplicationEnvironment, ComponentKeyResolver> componentKeyResolver) {
            this.componentKeyResolver = componentKeyResolver;
            return this;
        }

        public Configurer typeResolver(EnvironmentTypeResolver typeResolver) {
            return this.typeResolver(ContextualInitializer.of(typeResolver));
        }

        public Configurer typeResolver(ContextualInitializer<ApplicationEnvironment, EnvironmentTypeResolver> typeResolver) {
            this.typeResolver = typeResolver;
            return this;
        }
    }
}
