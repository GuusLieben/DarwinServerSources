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

package org.dockbox.hartshorn.util.graph;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class SimpleGraphNode<T> implements MutableContainableGraphNode<T> {

    private final T value;
    private final Set<GraphNode<T>> parents = new HashSet<>();
    private final Set<GraphNode<T>> children = new HashSet<>();

    public SimpleGraphNode(final T value) {
        this.value = value;
    }

    @Override
    public T value() {
        return this.value;
    }

    @Override
    public Set<GraphNode<T>> parents() {
        return Set.copyOf(this.parents);
    }

    @Override
    public Set<GraphNode<T>> children() {
        return Set.copyOf(this.children);
    }

    @Override
    public void addParent(final GraphNode<T> parent) {
        if (!this.parents.contains(parent)) {
            this.parents.add(parent);
            if (parent instanceof MutableGraphNode<T> parentGraphNode) {
                parentGraphNode.addChild(this);
            }
        }
    }

    @Override
    public void addParents(final Collection<GraphNode<T>> parents) {
        parents.forEach(this::addParent);
    }

    @Override
    public void addChild(final GraphNode<T> child) {
        if (!this.children.contains(child)) {
            this.children.add(child);
            if (child instanceof MutableContainableGraphNode<T> childGraphNode) {
                childGraphNode.addParent(this);
            }
        }
    }

    @Override
    public void addChildren(final Collection<GraphNode<T>> children) {
        children.forEach(this::addChild);
    }

    @Override
    public String toString() {
        return "SimpleGraphNode{" +
                "value=" + this.value +
                '}';
    }
}