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

package org.dockbox.hartshorn.properties.loader.support;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.dockbox.hartshorn.properties.ConfiguredProperty;
import org.dockbox.hartshorn.properties.MapPropertyRegistry;
import org.dockbox.hartshorn.properties.PropertyRegistry;
import org.dockbox.hartshorn.properties.SingleConfiguredProperty;
import org.dockbox.hartshorn.properties.loader.PredicatePropertyRegistryLoader;
import org.dockbox.hartshorn.properties.loader.PropertyRegistryLoader;
import org.dockbox.hartshorn.properties.loader.path.PropertyPathFormatter;
import org.dockbox.hartshorn.properties.loader.path.PropertyPathNode;
import org.dockbox.hartshorn.properties.loader.path.PropertyRootPathNode;
import org.dockbox.hartshorn.util.CollectionUtilities;
import org.dockbox.hartshorn.util.option.Option;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;

public abstract class JacksonPropertyRegistryLoader implements PredicatePropertyRegistryLoader {

    private final PropertyPathFormatter formatter;
    private ObjectMapper objectMapper;

    public JacksonPropertyRegistryLoader(PropertyPathFormatter formatter) {
        this.formatter = formatter;
    }

    protected abstract ObjectMapper createObjectMapper();

    @Override
    public void loadRegistry(PropertyRegistry registry, Path path) throws IOException {
        JsonNode graph = loadGraph(path);
        this.loadRegistry(registry, graph);
    }

    protected PropertyRegistry loadRegistry(PropertyRegistry registry, JsonNode node) {
        List<ConfiguredProperty> properties = loadProperties(node);
        for(ConfiguredProperty property : properties) {
            registry.register(property);
        }
        return registry;
    }

    protected List<ConfiguredProperty> loadProperties(JsonNode node) {
        return loadProperties(new PropertyRootPathNode(), node);
    }

    protected List<ConfiguredProperty> loadProperties(PropertyPathNode path, JsonNode node) {
        return switch(node) {
            case ArrayNode arrayNode -> loadArrayProperties(path, arrayNode);
            case ObjectNode objectNode -> loadObjectProperties(path, objectNode);
            case ValueNode valueNode -> List.of(loadSingleProperty(path, valueNode));
            default -> throw new IllegalArgumentException("Invalid node type: " + node.getNodeType());
        };
    }

    protected ConfiguredProperty loadSingleProperty(PropertyPathNode path, ValueNode valueNode) {
        String propertyPath = formatter.formatPath(path);
        return new SingleConfiguredProperty(propertyPath, valueNode.asText());
    }

    protected List<ConfiguredProperty> loadArrayProperties(PropertyPathNode path, ArrayNode arrayNode) {
        List<ConfiguredProperty> properties = new ArrayList<>();
        CollectionUtilities.indexed(arrayNode.elements(), (index, element) -> {
            PropertyPathNode nextPath = path.index(index);
            properties.addAll(loadProperties(nextPath, element));
        });
        return properties;
    }

    protected List<ConfiguredProperty> loadObjectProperties(PropertyPathNode path, ObjectNode objectNode) {
        List<ConfiguredProperty> properties = new ArrayList<>();
        CollectionUtilities.iterateEntries(objectNode.fields(), (name, value) -> {
            PropertyPathNode nextPath = path.property(name);
            properties.addAll(loadProperties(nextPath, value));
        });
        return properties;
    }

    protected JsonNode loadGraph(Path path) throws IOException {
        return this.objectMapper().readTree(path.toFile());
    }

    protected ObjectMapper objectMapper() {
        if(this.objectMapper == null) {
            this.objectMapper = this.createObjectMapper();
        }
        return this.objectMapper;
    }
}
