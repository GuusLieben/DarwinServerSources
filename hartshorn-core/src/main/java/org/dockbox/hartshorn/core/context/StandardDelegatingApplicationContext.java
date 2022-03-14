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

package org.dockbox.hartshorn.core.context;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.dockbox.hartshorn.core.CustomMultiTreeMap;
import org.dockbox.hartshorn.core.Enableable;
import org.dockbox.hartshorn.core.InjectConfiguration;
import org.dockbox.hartshorn.core.Key;
import org.dockbox.hartshorn.core.MetaProvider;
import org.dockbox.hartshorn.core.Modifiers;
import org.dockbox.hartshorn.core.MultiMap;
import org.dockbox.hartshorn.core.annotations.activate.Activator;
import org.dockbox.hartshorn.core.annotations.activate.AutomaticActivation;
import org.dockbox.hartshorn.core.annotations.activate.ServiceActivator;
import org.dockbox.hartshorn.core.annotations.inject.ComponentBinding;
import org.dockbox.hartshorn.core.binding.BindingHierarchy;
import org.dockbox.hartshorn.core.binding.Providers;
import org.dockbox.hartshorn.core.boot.ApplicationLogger;
import org.dockbox.hartshorn.core.boot.ApplicationManager;
import org.dockbox.hartshorn.core.boot.ApplicationProxier;
import org.dockbox.hartshorn.core.boot.ClasspathResourceLocator;
import org.dockbox.hartshorn.core.boot.ExceptionHandler;
import org.dockbox.hartshorn.core.boot.LifecycleObservable;
import org.dockbox.hartshorn.core.boot.LifecycleObserver;
import org.dockbox.hartshorn.core.boot.ObservableApplicationManager;
import org.dockbox.hartshorn.core.boot.SelfActivatingApplicationContext;
import org.dockbox.hartshorn.core.context.element.MethodContext;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.dockbox.hartshorn.core.domain.Exceptional;
import org.dockbox.hartshorn.core.exceptions.ApplicationException;
import org.dockbox.hartshorn.core.inject.ProviderContext;
import org.dockbox.hartshorn.core.proxy.ProxyLookup;
import org.dockbox.hartshorn.core.services.ComponentContainer;
import org.dockbox.hartshorn.core.services.ComponentLocator;
import org.dockbox.hartshorn.core.services.ComponentPostProcessor;
import org.dockbox.hartshorn.core.services.ComponentPreProcessor;
import org.dockbox.hartshorn.core.services.ComponentProcessor;
import org.slf4j.Logger;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Properties;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.inject.Named;

public class StandardDelegatingApplicationContext extends DefaultContext implements SelfActivatingApplicationContext, HierarchicalComponentProvider {

    public static Comparator<String> PREFIX_PRIORITY_COMPARATOR = Comparator.naturalOrder();

    private static final Pattern ARGUMENTS = Pattern.compile("--([a-zA-Z0-9\\.]+)=(.+)");

    protected final transient MultiMap<Integer, ComponentPreProcessor<?>> preProcessors = new CustomMultiTreeMap<>(ConcurrentHashMap::newKeySet);
    protected final transient Queue<String> prefixQueue = new PriorityQueue<>(PREFIX_PRIORITY_COMPARATOR);
    protected final transient Map<String, ScopedComponentProvider> providers = new ConcurrentHashMap<>();
    protected final transient Properties environmentValues = new Properties();

    private final transient StandardComponentProvider componentProvider;

    private final Set<Modifiers> modifiers;
    private final Set<Annotation> activators = ConcurrentHashMap.newKeySet();

    private final ApplicationEnvironment environment;
    private final ClasspathResourceLocator resourceLocator;
    private final ComponentPopulator componentPopulator;
    private final ComponentLocator locator;
    private final MetaProvider metaProvider;
    private final Activator activator;

    public StandardDelegatingApplicationContext(final ApplicationEnvironment environment,
                                                final Function<ApplicationContext, ComponentLocator> componentLocator,
                                                final Function<ApplicationContext, ClasspathResourceLocator> resourceLocator,
                                                final Function<ApplicationContext, MetaProvider> metaProvider,
                                                final Function<ApplicationContext, ComponentProvider> componentProvider,
                                                final Function<ApplicationContext, ComponentPopulator> componentPopulator,
                                                final TypeContext<?> activationSource,
                                                final Set<String> args,
                                                final Set<Modifiers> modifiers) {
        
        this.componentProvider = new HierarchicalApplicationComponentProvider(this);
        this.store(this.componentProvider);

        this.componentPopulator = new ContextualComponentPopulator(this);

        this.componentProvider.singleton(Key.of(ApplicationContext.class), this);
        this.environment = environment;
        final Exceptional<Activator> activator = activationSource.annotation(Activator.class);
        if (activator.absent()) {
            throw new IllegalStateException("Activation source is not marked with @Activator");
        }
        this.activator = activator.get();
        this.environment().annotationsWith(activationSource, ServiceActivator.class).forEach(this::addActivator);

        this.log().debug("Located %d service activators".formatted(this.activators().size()));

        this.populateArguments(args);

        this.locator = componentLocator.apply(this);
        this.resourceLocator = resourceLocator.apply(this);
        this.metaProvider = metaProvider.apply(this);
        this.modifiers = modifiers;

        this.registerDefaultBindings();
    }

    protected void registerDefaultBindings() {
        this.bind(Key.of(ComponentProvider.class), this);
        this.bind(Key.of(ApplicationContext.class), this);
        this.bind(Key.of(ActivatorSource.class), this);
        this.bind(Key.of(ApplicationPropertyHolder.class), this);
        this.bind(Key.of(ApplicationBinder.class), this);

        this.bind(Key.of(ComponentPopulator.class), this.componentPopulator);
        this.bind(Key.of(StandardComponentProvider.class), this.componentProvider);
        this.bind(Key.of(ComponentProvider.class), this.componentProvider);

        this.bind(Key.of(MetaProvider.class), this.meta());
        this.bind(Key.of(ComponentLocator.class), this.locator());
        this.bind(Key.of(ApplicationEnvironment.class), this.environment());
        this.bind(Key.of(ClasspathResourceLocator.class), this.resourceLocator());

        this.bind(Key.of(ProxyLookup.class), this.environment().manager());
        this.bind(Key.of(ApplicationLogger.class), this.environment().manager());
        this.bind(Key.of(ApplicationProxier.class), this.environment().manager());
        this.bind(Key.of(ApplicationManager.class), this.environment().manager());
        this.bind(Key.of(LifecycleObservable.class), this.environment().manager());

        this.bind(Key.of(Logger.class), (Supplier<Logger>) this::log);
    }

    @Override
    public void addActivator(final Annotation annotation) {
        if (this.activators.contains(annotation)) return;
        final TypeContext<? extends Annotation> annotationType = TypeContext.of(annotation.annotationType());
        final Exceptional<ServiceActivator> activator = annotationType.annotation(ServiceActivator.class);
        if (activator.present()) {
            this.activators.add(annotation);
            for (final String scan : activator.get().scanPackages()) {
                this.bind(scan);
            }
            this.environment().annotationsWith(annotationType, ServiceActivator.class).forEach(this::addActivator);
        }
    }

    @Override
    public void add(final ComponentProcessor<?> processor) {
        final Integer order = processor.order();
        final String name = TypeContext.of(processor).name();

        if (processor instanceof ComponentPostProcessor<?> postProcessor) {
            this.componentProvider.postProcessor(postProcessor);
            this.log().debug("Added " + name + " for component post-processing at phase " + order);
        }
        else if (processor instanceof ComponentPreProcessor<?> preProcessor) {
            this.preProcessors.put(preProcessor.order(), preProcessor);
            this.log().debug("Added " + name + " for component pre-processing at phase " + order);
        }
        else {
            this.log().warn("Unsupported component processor type [" + name + "]");
        }
    }

    @Override
    public Set<Annotation> activators() {
        return Set.copyOf(this.activators);
    }

    @Override
    public <A> A activator(final Class<A> activator) {
        return (A) this.activators.stream().filter(a -> a.annotationType().equals(activator)).findFirst().orElse(null);
    }

    @Override
    public void processPrefixQueue() {
        String next;
        while ((next = this.prefixQueue.poll()) != null) {
            this.processPrefix(next);
        }
    }

    protected void processPrefix(final String prefix) {
        this.locator().register(prefix);

        final Collection<TypeContext<?>> binders = this.environment().types(prefix, ComponentBinding.class, false);

        for (final TypeContext<?> binder : binders) {
            final ComponentBinding bindAnnotation = binder.annotation(ComponentBinding.class).get();
            this.handleBinder(binder, bindAnnotation);
        }
    }

    @Override
    public void process() {
        this.processPrefixQueue();
        final Collection<ComponentContainer> containers = this.locator().containers();
        this.log().debug("Located %d components from classpath".formatted(containers.size()));
        this.process(containers);
    }

    protected void process(final Collection<ComponentContainer> containers) {
        for (final ComponentPreProcessor<?> serviceProcessor : this.preProcessors.allValues()) {
            for (final ComponentContainer container : containers) {
                final TypeContext<?> service = container.type();
                final Key<?> key = Key.of(service);
                if (serviceProcessor.modifies(this, key)) {
                    this.log().debug("Processing component %s with registered processor %s".formatted(container.id(), TypeContext.of(serviceProcessor).name()));
                    serviceProcessor.process(this, key);
                }
            }
        }
    }

    @Override
    public <T> Exceptional<T> property(final String key) {
        return Exceptional.of(() -> (T) this.environmentValues.getOrDefault(key, System.getenv(key)));
    }

    @Override
    public <T> Exceptional<Collection<T>> properties(final String key) {
        // List values are stored as key[0], key[1], ...
        // We use regex to match this pattern, so we can restore the collection
        final String regex = key + "\\[[0-9]+]";
        final List<T> properties = this.environmentValues.entrySet().stream()
                .filter(e -> {
                    final String k = (String) e.getKey();
                    return k.matches(regex);
                })
                // Sort the collection using the key, as these are formatted to contain the index this means we
                // restore the original order of the collection.
                .sorted(Comparator.comparing(e -> (String) e.getKey()))
                .map(Entry::getValue)
                .map(v -> (T) v)
                .collect(Collectors.toList());

        if (properties.isEmpty()) return Exceptional.empty();
        return Exceptional.of(properties);
    }

    @Override
    public boolean hasProperty(final String key) {
        return this.property(key).present();
    }

    @Override
    public <T> void property(final String key, final T value) {
        this.environmentValues.put(key, value);
    }

    @Override
    public void properties(final Map<String, Object> tree) {
        for (final Entry<String, Object> entry : tree.entrySet())
            this.property(entry.getKey(), entry.getValue());
    }

    @Override
    public Properties properties() {
        return this.environmentValues;
    }

    private void populateArguments(final Set<String> args) {
        for (final String arg : args) {
            final Matcher matcher = ARGUMENTS.matcher(arg);
            if (matcher.find()) this.property(matcher.group(1), matcher.group(2));
        }
    }

    @Override
    public ComponentLocator locator() {
        return this.locator;
    }

    @Override
    public MetaProvider meta() {
        return this.metaProvider;
    }

    @Override
    public boolean hasActivator(final Class<? extends Annotation> activator) {
        final Exceptional<ServiceActivator> annotation = TypeContext.of(activator).annotation(ServiceActivator.class);
        if (annotation.absent())
            throw new IllegalArgumentException("Requested activator " + activator.getSimpleName() + " is not decorated with @ServiceActivator");

        if (this.modifiers.contains(Modifiers.ACTIVATE_ALL)) return true;
        else {
            return this.activators.stream()
                    .map(Annotation::annotationType)
                    .toList()
                    .contains(activator);
        }
    }

    @Override
    public void bind(final InjectConfiguration configuration) {
        this.log().debug("Activating configuration binder " + TypeContext.of(configuration).name());
        configuration.binder(this).collect(this);
    }

    @Override
    public void bind(final String prefix) {
        for (final String scannedPrefix : this.environment().prefixContext().prefixes()) {
            if (prefix.startsWith(scannedPrefix)) return;
            if (scannedPrefix.startsWith(prefix)) {
                // If a previously scanned prefix is a prefix of the current prefix, it is more specific and should be ignored,
                // as this prefix will include the specific prefix.
                this.environment().prefixContext().prefixes().remove(scannedPrefix);
            }
        }
        this.environment().prefix(prefix);
        this.prefixQueue.add(prefix);
    }

    @Override
    public <T> void add(final ProviderContext<T> context) {
        final Key<T> key = context.key();
        this.componentProvider.inHierarchy(key, hierarchy -> {
            if (context.singleton()) {
                if (context.lazy()) {
                    hierarchy.add(context.priority(), Providers.of(() -> context.provider().get()));
                }
                else {
                    hierarchy.add(context.priority(), Providers.of(context.provider().get()));
                }
            }
            else {
                hierarchy.add(context.priority(), Providers.of(context.provider()));
            }
        });
    }

    @Override
    public <T> T populate(final T type) {
        return this.componentPopulator.populate(type);
    }

    @Override
    public <T> T invoke(final MethodContext<T, ?> method) {
        return this.invoke((MethodContext<? extends T, Object>) method, this.get(method.parent()));
    }

    @Override
    public <T, P> T invoke(final MethodContext<T, P> method, final P instance) {
        final List<TypeContext<?>> parameters = method.parameterTypes();

        final Object[] invokingParameters = new Object[parameters.size()];
        for (int i = 0; i < parameters.size(); i++) {
            final TypeContext<?> parameter = parameters.get(i);
            final Exceptional<Named> annotation = parameter.annotation(Named.class);
            if (annotation.present()) {
                invokingParameters[i] = this.get(parameter, annotation.get());
            }
            else {
                invokingParameters[i] = this.get(parameter);
            }
        }
        try {
            return (T) method.invoke(instance, invokingParameters);
        }
        catch (final Throwable e) {
            return null;
        }
    }

    @Override
    public void enable(final Object instance) throws ApplicationException {
        if (instance instanceof Enableable enableable && enableable.canEnable()) {
            enableable.enable();
        }
    }

    private <T> void handleBinder(final TypeContext<T> implementer, final ComponentBinding annotation) {
        final TypeContext<T> target = TypeContext.of((Class<T>) annotation.value());

        if (implementer.boundConstructors().isEmpty()) {
            this.handleScanned(implementer, target, annotation);
        }
        else {
            this.componentProvider.inHierarchy(Key.of(target), hierarchy -> hierarchy.add(annotation.priority(), Providers.of(implementer)));
        }
    }

    private <C> void handleScanned(final TypeContext<? extends C> binder, final TypeContext<C> binds, final ComponentBinding bindAnnotation) {
        final Named meta = bindAnnotation.named();
        Key<C> key = Key.of(binds);
        if (!"".equals(meta.value())) {
            key = key.name(meta);
        }
        this.componentProvider.inHierarchy(key, hierarchy -> hierarchy.add(bindAnnotation.priority(), Providers.of(binder)));
    }

    @Override
    public void lookupActivatables() {
        final Collection<TypeContext<? extends ComponentProcessor>> children = this.environment().children(ComponentProcessor.class);
        for (final TypeContext<? extends ComponentProcessor> processor : children) {
            if (processor.isAbstract()) continue;

            if (processor.annotation(AutomaticActivation.class).map(AutomaticActivation::value).or(false)) {
                final ComponentProcessor componentProcessor = this.get(processor);
                if (this.hasActivator(componentProcessor.activator()))
                    this.add(componentProcessor);
            }
        }
    }

    @Override
    public void handle(final Throwable throwable) {
        this.environment().manager().handle(throwable);
    }

    @Override
    public void handle(final String message, final Throwable throwable) {
        this.environment().manager().handle(message, throwable);
    }

    @Override
    public ExceptionHandler stacktraces(final boolean stacktraces) {
        return this.environment().manager().stacktraces(stacktraces);
    }

    @Override
    public void close() {
        this.log().info("Runtime shutting down, notifying observers");
        final ApplicationManager manager = this.environment().manager();
        if (manager instanceof ObservableApplicationManager observable) {
            for (final LifecycleObserver observer : observable.observers()) {
                this.log().debug("Notifying " + observer.getClass().getSimpleName() + " of shutdown");
                try {
                    observer.onExit(this);
                } catch (final Throwable e) {
                    this.log().error("Error notifying " + observer.getClass().getSimpleName() + " of shutdown", e);
                }
            }
        }
    }

    @Override
    public <T> T get(final Key<T> key) {
        return this.componentProvider.get(key);
    }

    @Override
    public <T> T get(final Key<T> key, final boolean enable) {
        return this.componentProvider.get(key, enable);
    }

    @Override
    public <C> void bind(final Key<C> contract, final Supplier<C> supplier) {
        this.componentProvider.bind(contract, supplier);
    }

    @Override
    public <C, T extends C> void bind(final Key<C> key, final Class<? extends T> implementation) {
        this.componentProvider.bind(key, implementation);
    }

    @Override
    public <C, T extends C> void bind(final Key<C> key, final T instance) {
        this.componentProvider.bind(key, instance);
    }

    public ClasspathResourceLocator resourceLocator() {
        return this.resourceLocator;
    }

    protected Activator activator() {
        return this.activator;
    }

    @Override
    public ApplicationEnvironment environment() {
        return this.environment;
    }

    public Set<Modifiers> modifiers() {
        return this.modifiers;
    }

    @Override
    public <T, C extends T> void singleton(final Key<T> key, final C instance) {
        this.componentProvider.singleton(key, instance);
    }

    @Override
    public <C> void inHierarchy(final Key<C> key, final Consumer<BindingHierarchy<C>> consumer) {
        this.componentProvider.inHierarchy(key, consumer);
    }

    @Override
    public <T> BindingHierarchy<T> hierarchy(final Key<T> key) {
        return this.componentProvider.hierarchy(key);
    }

    @Override
    public void store(final ScopedComponentProvider provider) {
        final String key = provider.scope();
        if (this.providers.containsKey(key)) {
            throw new IllegalStateException("A provider for " + key + " already exists");
        }
        this.providers.put(key, provider);
    }

    @Override
    @Nullable
    public ScopedComponentProvider get(final String key) {
        return this.providers.get(key);
    }

    @Override
    public void remove(final String key) {
        this.providers.remove(key);
    }
}