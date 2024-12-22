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
 * Represents the root node of a property path.
 *
 * @since 0.7.0
 *
 * @author Guus Lieben
 */
public final class PropertyRootPathNode implements PropertyPathNode {

    @Override
    public String name() {
        return "";
    }

    @Override
    public PropertyPathNode parent() {
        throw new UnsupportedOperationException("Root node has no parent");
    }
}
