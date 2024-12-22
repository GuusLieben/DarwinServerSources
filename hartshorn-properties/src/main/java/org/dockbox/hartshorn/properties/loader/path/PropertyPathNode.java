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

package org.dockbox.hartshorn.properties.loader.path;

/**
 * Represents a node in a property path. A node is a single element in a configuration path, such as a field or an index.
 * Nodes can be chained together to form a path.
 *
 * @see PropertyRootPathNode
 * @see PropertyFieldPathNode
 * @see PropertyIndexPathNode
 *
 * @since 0.7.0
 *
 * @author Guus Lieben
 */
public sealed interface PropertyPathNode permits PropertyFieldPathNode, PropertyIndexPathNode, PropertyRootPathNode {

    /**
     * Returns the name of the node. This does not include the parent node's name.
     * @return the name of the node
     */
    String name();

    /**
     * Returns the parent node of this node. If this node is the root node, an {@link UnsupportedOperationException} is thrown.
     * @return the parent node
     */
    PropertyPathNode parent();

    /**
     * Creates a new {@link PropertyFieldPathNode} with the given name and this node as its parent.
     *
     * @param name the name of the field
     * @return a new field node
     */
    default PropertyPathNode property(String name) {
        return new PropertyFieldPathNode(name, this);
    }

    /**
     * Creates a new {@link PropertyIndexPathNode} with the given index and this node as its parent.
     *
     * @param index the index of the node
     * @return a new index node
     */
    default PropertyPathNode index(int index) {
        return new PropertyIndexPathNode(index, this);
    }
}
