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

package org.dockbox.hartshorn.inject.graph;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.dockbox.hartshorn.inject.InjectionCapableApplication;
import org.dockbox.hartshorn.inject.binding.HierarchicalBinder;
import org.dockbox.hartshorn.inject.graph.declaration.ComponentKeyDependencyDeclarationContext;
import org.dockbox.hartshorn.inject.graph.declaration.DependencyContext;
import org.dockbox.hartshorn.inject.graph.declaration.DependencyDeclarationContext;
import org.dockbox.hartshorn.inject.graph.declaration.ImplementationDependencyContext;
import org.dockbox.hartshorn.inject.ComponentKey;
import org.dockbox.hartshorn.inject.provider.CompositeInstantiationStrategy;
import org.dockbox.hartshorn.inject.provider.InstantiationStrategy;
import org.dockbox.hartshorn.inject.provider.TypeAwareInstantiationStrategy;
import org.dockbox.hartshorn.inject.binding.BindingHierarchy;
import org.dockbox.hartshorn.inject.collection.ComponentCollection;
import org.dockbox.hartshorn.util.CollectionUtilities;
import org.dockbox.hartshorn.util.ContextualInitializer;
import org.dockbox.hartshorn.util.TypeUtils;
import org.dockbox.hartshorn.util.collections.ArrayListMultiMap;
import org.dockbox.hartshorn.util.collections.MultiMap;
import org.dockbox.hartshorn.util.graph.Graph;
import org.dockbox.hartshorn.util.graph.GraphNode;
import org.dockbox.hartshorn.util.graph.MutableContainableGraphNode;
import org.dockbox.hartshorn.util.graph.SimpleGraphNode;
import org.dockbox.hartshorn.util.introspect.Introspector;
import org.dockbox.hartshorn.util.introspect.view.View;

/**
 * TODO: #1060 Add documentation
 *
 * @since 0.5.0
 *
 * @author Guus Lieben
 */
public class DependencyGraphBuilder {

    private final HierarchicalBinder binder;
    private final DependencyResolver resolver;
    private final Introspector introspector;

    protected DependencyGraphBuilder(
            DependencyResolver resolver,
            HierarchicalBinder binder,
            Introspector introspector
    ) {
        this.resolver = resolver;
        this.binder = binder;
        this.introspector = introspector;
    }

    /**
     * Creates a new {@link ContextualInitializer} that can be used to create a new instance of
     * {@link DependencyGraphBuilder}.
     *
     * @return a new {@link ContextualInitializer} that can be used to create a new {@link DependencyGraphBuilder}.
     */
    public static ContextualInitializer<DependencyResolver, DependencyGraphBuilder> create() {
        return context -> {
            InjectionCapableApplication application = context.firstContext(InjectionCapableApplication.class)
                    .orElseThrow(() -> new IllegalStateException("No application context found"));
            return create(context.input(), application.defaultBinder(), application.environment().introspector());
        };
    }

    /**
     * Creates a new instance of {@link DependencyGraphBuilder} with the given {@link DependencyResolver},
     * {@link HierarchicalBinder} and {@link Introspector}.
     *
     * @param resolver the resolver to use
     * @param binder the binder to use
     * @param introspector the introspector to use
     * @return a new instance of {@link DependencyGraphBuilder}
     */
    public static DependencyGraphBuilder create(DependencyResolver resolver, HierarchicalBinder binder, Introspector introspector) {
        return new DependencyGraphBuilder(resolver, binder, introspector);
    }

    /**
     * Builds a new {@link DependencyGraph} from the given {@link DependencyContext}s.
     *
     * @param dependencyContexts the contexts to build the graph from
     * @return a new {@link DependencyGraph} from the given {@link DependencyContext}s
     * @throws DependencyResolutionException if the resolution of the dependencies fails
     */
    public DependencyGraph buildDependencyGraph(Iterable<DependencyContext<?>> dependencyContexts) throws DependencyResolutionException {
        Set<DependencyContext<?>> contexts = this.inflateDependencyContexts(dependencyContexts);
        MultiMap<PriorityComponentKey, MutableContainableGraphNode<DependencyContext<?>>> nodes = this.computeNodeMap(contexts);
        DependencyGraph graph = new DependencyGraph();
        // Don't use inflated contexts here, as we want to keep the original context for the graph. If the inflated contexts are relevant,
        // they've already been attached to the component key of the original context.
        this.buildDependencyNodes(dependencyContexts, nodes, graph);
        return graph;
    }

    private void buildDependencyNodes(
        Iterable<DependencyContext<?>> dependencyContexts,
        MultiMap<PriorityComponentKey, MutableContainableGraphNode<DependencyContext<?>>> nodes,
        Graph<DependencyContext<?>> graph
    ) {
        for (DependencyContext<?> dependencyContext : dependencyContexts) {
            this.buildSingleDependencyNode(nodes, graph, dependencyContext);
        }
    }

    protected <T> Set<InstantiationStrategy<? extends T>> lookupImplementationProviders(DependencyContext<T> dependencyContext) {
        ComponentKey<T> componentKey = dependencyContext.componentKey();
        BindingHierarchy<T> hierarchy = this.binder.hierarchy(componentKey);
        int highestPriority = hierarchy.highestPriority();
        return hierarchy.get(highestPriority)
            .map(provider -> {
                if (provider instanceof CompositeInstantiationStrategy<T> composite) {
                    return composite.provider();
                }
                return provider;
            })
            .stream()
            .collect(Collectors.toSet());
    }

    private Set<DependencyContext<?>> inflateDependencyContexts(Iterable<DependencyContext<?>> dependencyContexts)
        throws DependencyResolutionException {
        Set<DependencyContext<?>> contexts = new HashSet<>();
        for (DependencyContext<?> dependencyContext : dependencyContexts) {
            contexts.add(dependencyContext);
            Class<?> dependencyType = dependencyContext.componentKey().type();
            Set<DependencyContext<?>> resolvedContexts = this.resolver.resolve(this.getImplementationContexts(dependencyContext)).stream()
                .map(implementationContext -> {
                    Class<?> implementationType = implementationContext.componentKey().type();
                    if (dependencyType.isAssignableFrom(implementationType)) {
                        return new ImplementationDependencyContext<>(implementationContext,
                            TypeUtils.unchecked(dependencyContext, DependencyContext.class));
                    }
                    return null;
                }).collect(Collectors.toSet());
            contexts.addAll(resolvedContexts);
        }
        return contexts;
    }

    @NonNull
    private <T> Set<DependencyDeclarationContext<?>> getImplementationContexts(DependencyContext<T> dependencyContext) {
        Set<InstantiationStrategy<? extends T>> strategies = this.lookupImplementationProviders(dependencyContext);
        return strategies.stream()
            .filter(provider -> provider instanceof TypeAwareInstantiationStrategy<? extends T>)
            .map(provider -> (TypeAwareInstantiationStrategy<? extends T>) provider)
            .map(provider -> {
                ComponentKey<? extends T> implementationKey = dependencyContext.componentKey()
                    .mutable()
                    .type(provider.type())
                    .build();
                return new ComponentKeyDependencyDeclarationContext<>(this.introspector, implementationKey, TypeUtils.unchecked(provider, InstantiationStrategy.class));
            })
            .collect(Collectors.toSet());
    }

    protected void visitContextForNodeMapping(
        DependencyContext<?> dependencyContext,
        MultiMap<PriorityComponentKey, MutableContainableGraphNode<DependencyContext<?>>> nodes
    ) {
        MutableContainableGraphNode<DependencyContext<?>> node = new SimpleGraphNode<>(dependencyContext);
        if (dependencyContext instanceof ImplementationDependencyContext<?, ?> implementationDependencyContext) {
            ComponentKey<?> componentKey = implementationDependencyContext.declarationContext().componentKey();
            PriorityComponentKey key = new PriorityComponentKey(implementationDependencyContext.priority(), componentKey);
            nodes.put(key, node);
        }

        ComponentKey<?> componentKey = dependencyContext.componentKey();
        PriorityComponentKey key = new PriorityComponentKey(dependencyContext.priority(), componentKey);
        switch (dependencyContext.memberType()) {
            case ComponentMemberType.STANDALONE -> nodes.put(key, node);
            case ComponentMemberType.COMPOSITE -> {
                ComponentKey<? extends ComponentCollection<?>> collectorComponentKey = componentKey.mutable().collector().build();
                PriorityComponentKey collectorKey = new PriorityComponentKey(dependencyContext.priority(), collectorComponentKey);
                nodes.put(collectorKey, node);
            }
            default -> throw new IllegalStateException("Unexpected value: " + dependencyContext.memberType());
        }
    }

    private void buildSingleDependencyNode(
        MultiMap<PriorityComponentKey, MutableContainableGraphNode<DependencyContext<?>>> nodes,
        Graph<DependencyContext<?>> graph,
        DependencyContext<?> dependencyContext
    ) {
        Collection<MutableContainableGraphNode<DependencyContext<?>>> dependencyContexts =
            collectDependencyContexts(nodes, dependencyContext);
        graph.addRoots(Set.copyOf(dependencyContexts));

        for (MutableContainableGraphNode<DependencyContext<?>> componentNode : dependencyContexts) {
            DependencyContext<?> componentDependencyContext = componentNode.value();
            for (ComponentKey<?> dependency : componentDependencyContext.dependencies().allValues()) {
                Set<GraphNode<DependencyContext<?>>> dependencyNodes = this.getHighestPriorityNodes(nodes, dependency, key -> {
                    if (key.componentKey().equals(dependencyContext.componentKey())) {
                        return key.priority() < dependencyContext.priority();
                    }
                    return true;
                });
                this.checkNoDuplicateContexts(dependency, dependencyNodes);
                graph.addRoots(dependencyNodes);
                dependencyContexts.forEach(node -> node.addParents(dependencyNodes));
            }
        }
    }

    private Set<GraphNode<DependencyContext<?>>> getHighestPriorityNodes(
        MultiMap<PriorityComponentKey, MutableContainableGraphNode<DependencyContext<?>>> nodes,
        ComponentKey<?> componentKey,
        Predicate<PriorityComponentKey> predicate
    ) {
        Set<GraphNode<DependencyContext<?>>> highestPriorityNodes = new HashSet<>();
        for (PriorityComponentKey key : nodes.keySet()) {
            if (key.componentKey().equals(componentKey) && predicate.test(key)) {
                Collection<MutableContainableGraphNode<DependencyContext<?>>> componentNodes = nodes.get(key);
                int highestPriority = componentNodes.stream()
                    .mapToInt(node -> node.value().priority())
                    .max()
                    .orElseThrow();
                for (MutableContainableGraphNode<DependencyContext<?>> componentNode : componentNodes) {
                    if (componentNode.value().priority() == highestPriority) {
                        highestPriorityNodes.add(componentNode);
                    }
                }
            }
        }
        return highestPriorityNodes;
    }

    private void checkNoDuplicateContexts(ComponentKey<?> dependency, Set<GraphNode<DependencyContext<?>>> dependencyNodes) {
        if (dependencyNodes.size() > 1) {
            boolean collectionsOnly = dependencyNodes.stream().allMatch(node -> node.value().memberType() == ComponentMemberType.COMPOSITE);
            if (!collectionsOnly) {
                MultiMap<Integer, DependencyContext<?>> contextsByPriority = this.groupDependenciesByPriority(dependencyNodes);
                for (int priority : contextsByPriority.keySet()) {
                    Collection<DependencyContext<?>> dependencyContexts = contextsByPriority.get(priority);
                    if (dependencyContexts.size() > 1) {
                        this.reportDuplicatePrioritiesForDependencyNode(dependency, priority, dependencyContexts);
                    }
                }
            }
        }
    }

    @NonNull
    private MultiMap<Integer, DependencyContext<?>> groupDependenciesByPriority(
        Set<GraphNode<DependencyContext<?>>> dependencyNodes) {
        MultiMap<Integer, DependencyContext<?>> contextsByPriority = new ArrayListMultiMap<>();
        for (GraphNode<DependencyContext<?>> dependencyNode : dependencyNodes) {
            int priority = dependencyNode.value().priority();
            contextsByPriority.put(priority, dependencyNode.value());
        }
        return contextsByPriority;
    }

    private void reportDuplicatePrioritiesForDependencyNode(ComponentKey<?> dependency, int priority, Collection<DependencyContext<?>> dependencyContexts) {
        String origins = dependencyContexts.stream()
            .map(DependencyContext::origin)
            .map(View::qualifiedName)
            .collect(Collectors.joining(",\n"));
        throw new IllegalStateException(
            "Multiple nodes found for dependency %s at priority %d but not all are collections. Defined by: %s".formatted(
                dependency,
                priority,
                origins
            ));
    }

    @NonNull
    private static Collection<MutableContainableGraphNode<DependencyContext<?>>> collectDependencyContexts(
        MultiMap<PriorityComponentKey, MutableContainableGraphNode<DependencyContext<?>>> nodes,
        DependencyContext<?> dependencyContext
    ) {
        PriorityComponentKey key = new PriorityComponentKey(dependencyContext.priority(), dependencyContext.componentKey());
        Collection<MutableContainableGraphNode<DependencyContext<?>>> componentNodes = nodes.get(key);

        ComponentKey<? extends ComponentCollection<?>> collectionComponentKey =
            dependencyContext.componentKey().mutable().collector().build();
        PriorityComponentKey collectionKey = new PriorityComponentKey(key.priority(), collectionComponentKey);
        Collection<MutableContainableGraphNode<DependencyContext<?>>> collectionComponentNodes = nodes.get(collectionKey);

        return CollectionUtilities.merge(componentNodes, collectionComponentNodes);
    }

    @NonNull
    private MultiMap<PriorityComponentKey, MutableContainableGraphNode<DependencyContext<?>>> computeNodeMap(
        Iterable<DependencyContext<?>> allDependencyContexts
    ) {
        MultiMap<PriorityComponentKey, MutableContainableGraphNode<DependencyContext<?>>> nodes = new ArrayListMultiMap<>();
        for (DependencyContext<?> dependencyContext : allDependencyContexts) {
            this.visitContextForNodeMapping(dependencyContext, nodes);
        }

        return nodes;
    }

    /**
     * A record to store a priority-based component key. This may be used for sorting purposes, and should typically only
     * be used in the context of a map-like structure, where this record represents a key.
     *
     * @param priority the priority
     * @param componentKey the component key
     */
    protected record PriorityComponentKey(int priority, ComponentKey<?> componentKey) {
    }
}
