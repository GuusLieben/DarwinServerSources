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

package org.dockbox.hartshorn.inject.binding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.dockbox.hartshorn.inject.ComponentKey;
import org.dockbox.hartshorn.inject.CompositeQualifier;
import org.dockbox.hartshorn.inject.provider.InstantiationStrategy;
import org.dockbox.hartshorn.inject.provider.TypeAwareInstantiationStrategy;
import org.dockbox.hartshorn.util.option.Option;
import org.dockbox.hartshorn.util.stream.EntryStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A base implementation of a {@link BindingHierarchy}. This implementation tracks providers by priority, and allows
 * for the addition of providers with a priority. The priority is used to determine the order in which providers are
 * evaluated. The higher the priority, the earlier the provider is evaluated.
 *
 * @param <T> the type of the component
 *
 * @since 0.5.0
 *
 * @author Guus Lieben
 */
public abstract class AbstractBindingHierarchy<T> implements BindingHierarchy<T> {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractBindingHierarchy.class);

    private final NavigableMap<Integer, InstantiationStrategy<T>> providers = new TreeMap<>(Collections.reverseOrder());

    private final ComponentKey<T> key;

    public AbstractBindingHierarchy(ComponentKey<T> key) {
        this.key = key;
    }

    /**
     * Returns the map of providers, where the key is the priority, and the value is the provider. The map is sorted
     * in descending order, meaning the highest priority is the first entry.
     *
     * @return the map of providers
     */
    protected NavigableMap<Integer, InstantiationStrategy<T>> priorityProviders() {
        return this.providers;
    }

    @Override
    public ComponentKey<T> key() {
        return this.key;
    }

    @Override
    public List<InstantiationStrategy<T>> providers() {
        return List.copyOf(this.priorityProviders().values());
    }

    @Override
    public BindingHierarchy<T> add(InstantiationStrategy<T> strategy) {
        return this.add(-1, strategy);
    }

    @Override
    public BindingHierarchy<T> add(int priority, InstantiationStrategy<T> strategy) {
        // Default providers may be overwritten without further warnings
        if (this.priorityProviders().containsKey(priority) && priority != -1) {
            LOG.warn(("There is already a provider for %s with priority %d. It will be overwritten! " +
                    "To avoid unexpected behavior, ensure the priority is not already present. Current hierarchy: %s").formatted(this.key()
                    .type().getSimpleName(), priority, this));
        }
        this.priorityProviders().put(priority, strategy);
        return this;
    }

    @Override
    public BindingHierarchy<T> addNext(InstantiationStrategy<T> strategy) {
        int next = -1;
        if (!this.priorityProviders().isEmpty()) {
            next = this.priorityProviders().lastKey()+1;
        }
        return this.add(next, strategy);
    }

    @Override
    public BindingHierarchy<T> merge(BindingHierarchy<T> hierarchy) {
        BindingHierarchy<T> merged = new NativePrunableBindingHierarchy<>(this.key());
        // Low priority, other
        for (Entry<Integer, InstantiationStrategy<T>> entry : hierarchy) {
            merged.add(entry.getKey(), entry.getValue());
        }
        // High priority, self
        for (Entry<Integer, InstantiationStrategy<T>> entry : this) {
            merged.add(entry.getKey(), entry.getValue());
        }
        return merged;
    }

    @Override
    public Option<InstantiationStrategy<T>> get(int priority) {
        return Option.of(this.priorityProviders().getOrDefault(priority, null));
    }

    @Override
    public int highestPriority() {
        NavigableMap<Integer, InstantiationStrategy<T>> providers = this.priorityProviders();
        return providers.isEmpty() ? -1 : providers.firstKey();
    }

    @Override
    public SortedSet<Integer> priorities() {
        NavigableSet<Integer> integers = this.priorityProviders().navigableKeySet();
        // Reverse, as keys are in descending order, and this method should return ascending order
        return integers.descendingSet();
    }

    @Override
    public int size() {
        return this.priorityProviders().size();
    }

    @NonNull
    @Override
    public Iterator<Entry<Integer, InstantiationStrategy<T>>> iterator() {
        return this.priorityProviders().entrySet().iterator();
    }

    @Override
    public String toString() {
        String contract = this.contractTypeToString();
        CompositeQualifier qualifier = this.key().qualifier();
        String qualifiers = "";
        if (qualifier != null && !qualifier.qualifiers().isEmpty()) {
            qualifiers = " " + qualifier;
        }

        // TODO: Verify order is still correct after changes (display low-to-high)
        String hierarchy = EntryStream.of(this.priorityProviders())
                .sortedKeys(Comparator.reverseOrder())
                .map((priority, strategy) -> {
                    String target = strategy.toString();
                    if (strategy instanceof TypeAwareInstantiationStrategy<?> typeAwareProvider) {
                        target = typeAwareProvider.type().getSimpleName();
                    }
                    return "%s: %s".formatted(String.valueOf(priority), target);
                })
                .collect(Collectors.joining(" -> "));

        return "Hierarchy<%s>%s: %s".formatted(contract, qualifiers, hierarchy);
    }

    /**
     * Returns a string representation of the contract type of this hierarchy.
     *
     * @return a string representation of the contract type of this hierarchy
     */
    protected String contractTypeToString() {
        return this.key().parameterizedType().toString();
    }
}
