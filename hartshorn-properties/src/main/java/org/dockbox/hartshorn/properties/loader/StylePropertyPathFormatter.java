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

package org.dockbox.hartshorn.properties.loader;

import org.dockbox.hartshorn.properties.loader.path.PropertyFieldPathNode;
import org.dockbox.hartshorn.properties.loader.path.PropertyIndexPathNode;
import org.dockbox.hartshorn.properties.loader.path.PropertyPathFormatter;
import org.dockbox.hartshorn.properties.loader.path.PropertyPathNode;
import org.dockbox.hartshorn.properties.loader.path.PropertyPathStyle;
import org.dockbox.hartshorn.properties.loader.path.PropertyRootPathNode;
import org.dockbox.hartshorn.properties.loader.path.StandardPropertyPathStyle;

/**
 * A {@link PropertyPathFormatter} that formats a {@link PropertyPathNode} to a string representation following the
 * style defined by a {@link PropertyPathStyle}.
 *
 * @see PropertyPathFormatter
 *
 * @since 0.7.0
 *
 * @author Guus Lieben
 */
public class StylePropertyPathFormatter implements PropertyPathFormatter {

    private final PropertyPathStyle style;

    public StylePropertyPathFormatter() {
        this(StandardPropertyPathStyle.INSTANCE);
    }

    public StylePropertyPathFormatter(PropertyPathStyle style) {
        this.style = style;
    }

    @Override
    public String formatPath(PropertyPathNode pathNode) {
        StringBuilder builder = new StringBuilder();
        return this.formatPath(pathNode, builder);
    }

    private String formatPath(PropertyPathNode pathNode, StringBuilder builder) {
        return switch(pathNode) {
            case PropertyFieldPathNode fieldPathNode -> {
                builder.insert(0, this.formatField(fieldPathNode));
                yield this.formatPath(fieldPathNode.parent(), builder);
            }
            case PropertyIndexPathNode indexPathNode -> {
                builder.insert(0, this.formatIndex(indexPathNode));
                yield this.formatPath(indexPathNode.parent(), builder);
            }
            case PropertyRootPathNode ignored -> builder.toString();
        };
    }

    /**
     * Formats a field node to a string representation. If the node is a top-level node, the field name is returned
     * as-is.
     *
     * @param node the field node to format
     * @return the formatted field
     */
    protected String formatField(PropertyFieldPathNode node) {
        boolean isTopLevel = node.parent() instanceof PropertyRootPathNode;
        return isTopLevel ? node.name() : this.style.field(node.name());
    }

    /**
     * Formats an index node to a string representation.
     *
     * @param node the index node to format
     * @return the formatted index
     */
    protected String formatIndex(PropertyIndexPathNode node) {
        return this.style.index(node.index());
    }
}
