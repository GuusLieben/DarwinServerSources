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

import java.util.function.Predicate;

public class GraphInverter {

    public <T> Graph<T> invertGraphForValue(final Graph<T> graph, final T root) throws GraphException {
        try {
            return this.invertGraph(graph, node -> node.equals(root));
        }
        catch (final GraphException e) {
            throw new GraphException("Could not find root node " + root + " for inverted graph", e);
        }
    }

    public <T> Graph<T> invertGraph(final Graph<T> graph, final Predicate<T> rule) throws GraphException {
        final Graph<T> inverted = new SimpleGraph<>();
        final GraphNodeLookupVisitor<T> visitor = new GraphNodeLookupVisitor<>(rule);
        visitor.iterate(graph);
        final GraphNode<T> foundNode = visitor.foundNode();
        if (foundNode == null) {
            throw new GraphException("Could not find new root node for inverted graph");
        }

        // Create a parent-less copy of the found node, otherwise it is not a valid root node
        final MutableGraphNode<T> graphNode = new SimpleGraphNode<>(foundNode.value());
        // TODO: Invert entire graph
//        graphNode.addChildren(foundNode.parents());

        inverted.addRoot(graphNode);
        return inverted;
    }
}