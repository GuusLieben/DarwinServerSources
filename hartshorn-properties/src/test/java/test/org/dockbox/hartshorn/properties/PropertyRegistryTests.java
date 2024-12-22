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

package test.org.dockbox.hartshorn.properties;

import org.dockbox.hartshorn.properties.ConfiguredProperty;
import org.dockbox.hartshorn.properties.ListProperty;
import org.dockbox.hartshorn.properties.MapPropertyRegistry;
import org.dockbox.hartshorn.properties.ObjectProperty;
import org.dockbox.hartshorn.properties.PropertyRegistry;
import org.dockbox.hartshorn.properties.SingleConfiguredProperty;
import org.dockbox.hartshorn.properties.ValueProperty;
import org.dockbox.hartshorn.util.option.Option;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PropertyRegistryTests {

    void assertWithRegistry(Collection<ConfiguredProperty> properties, Consumer<PropertyRegistry> registryConsumer) {
        PropertyRegistry registry = new MapPropertyRegistry();
        Map<String, ConfiguredProperty> propertyMap = properties.stream()
                .collect(Collectors.toMap(ConfiguredProperty::name, Function.identity()));
        registry.registerAll(propertyMap);
        registryConsumer.accept(registry);
    }

    @Test
    @DisplayName("Test object access with object on root level of registry")
    void testNonNestedObjectAccess() {
        this.assertObjectAccess("sample");
    }

    @Test
    @DisplayName("Test object access with object nested in another object")
    void testNestedInObjectAccess() {
        this.assertObjectAccess("sample.nested");
    }

    @Test
    @DisplayName("Test object access with object nested in another list")
    void testNestedInListAccess() {
        this.assertObjectAccess("sample.nested[0]");
    }

    void assertObjectAccess(String objectKey) {
        this.assertWithRegistry(List.of(
                new SingleConfiguredProperty(objectKey + ".one", "one"),
                new SingleConfiguredProperty(objectKey + ".two", "two")
        ), registry -> {
            Option<ObjectProperty> object = registry.object(objectKey);
            Assertions.assertTrue(object.present());
            object.peek(obj -> {
                Assertions.assertEquals(objectKey, obj.name());
                Option<ValueProperty> one = obj.get("one");
                Assertions.assertTrue(one.present());
                Assertions.assertEquals("one", one.get().value().get());
                Assertions.assertEquals(objectKey + ".one", one.get().name());

                Option<ValueProperty> two = obj.get("two");
                Assertions.assertTrue(two.present());
                Assertions.assertEquals("two", two.get().value().get());
                Assertions.assertEquals(objectKey + ".two", two.get().name());
            });
        });
    }

    @Test
    @DisplayName("Test list access with list on root level of registry")
    void testNonNestedListAccess() {
        this.testListAccess("sample");
    }

    @Test
    @DisplayName("Test list access with list nested in another object")
    void testNestedInObjectListAccess() {
        this.testListAccess("sample.nested");
    }

    @Test
    @DisplayName("Test list access with list nested in another list")
    void testNestedInListListAccess() {
        this.testListAccess("sample.nested[0]");
    }

    void testListAccess(String listKey) {
        this.assertWithRegistry(List.of(
                new SingleConfiguredProperty(listKey + "[0]", "one"),
                new SingleConfiguredProperty(listKey + "[1]", "two")
        ), registry -> {
            Option<ListProperty> list = registry.list(listKey);
            Assertions.assertTrue(list.present());
            list.peek(l -> {
                Assertions.assertEquals(listKey, l.name());
                Option<ValueProperty> one = l.get(0);
                Assertions.assertTrue(one.present());
                Assertions.assertEquals("one", one.get().value().get());
                Assertions.assertEquals(listKey + "[0]", one.get().name());

                Option<ValueProperty> two = l.get(1);
                Assertions.assertTrue(two.present());
                Assertions.assertEquals("two", two.get().value().get());
                Assertions.assertEquals(listKey + "[1]", two.get().name());
            });
        });
    }

    @Test
    @DisplayName("Test value access with value on root level of registry")
    void testNonNestedValueAccess() {
        this.testValueAccess("sample");
    }

    @Test
    @DisplayName("Test value access with value nested in another object")
    void testNestedInObjectValueAccess() {
        this.testValueAccess("sample.nested");
    }

    @Test
    @DisplayName("Test value access with value nested in another list")
    void testNestedInListValueAccess() {
        this.testValueAccess("sample.nested[0]");
    }

    void testValueAccess(String valueKey) {
        this.assertWithRegistry(List.of(
                new SingleConfiguredProperty(valueKey, "value")
        ), registry -> {
            Option<ValueProperty> value = registry.get(valueKey);
            Assertions.assertTrue(value.present());
            value.peek(v -> {
                Assertions.assertEquals(valueKey, v.name());
                Assertions.assertEquals("value", v.value().get());
            });
        });
    }

    @Test
    @DisplayName("Test access to deeply nested value with step-by-step access")
    void testComplexAccess() {
        this.assertWithRegistry(
                List.of(new SingleConfiguredProperty("sample[0][1][0].property.sample[1].value", "value")),
                registry -> {
                    // sample
                    Option<ListProperty> sample = registry.list("sample");
                    Assertions.assertTrue(sample.present());

                    // sample[0]
                    Option<ListProperty> sampleIndex0 = sample.get().list(0);
                    Assertions.assertTrue(sampleIndex0.present());

                    // sample[0][1]
                    Option<ListProperty> sampleIndex0Index1 = sampleIndex0.get().list(1);
                    Assertions.assertTrue(sampleIndex0Index1.present());

                    // sample[0][1][0]
                    Option<ObjectProperty> sampleIndex0Index1Index0 = sampleIndex0Index1.get().object(0);
                    Assertions.assertTrue(sampleIndex0Index1Index0.present());

                    // sample[0][1][0].property
                    Option<ObjectProperty> sampleIndex0Index1Index0Property = sampleIndex0Index1Index0.get().object("property");
                    Assertions.assertTrue(sampleIndex0Index1Index0Property.present());

                    // sample[0][1][0].property.sample
                    Option<ListProperty> sampleIndex0Index1Index0PropertySample = sampleIndex0Index1Index0Property.get().list("sample");
                    Assertions.assertTrue(sampleIndex0Index1Index0PropertySample.present());

                    // sample[0][1][0].property.sample[1]
                    Option<ObjectProperty> sampleIndex0Index1Index0PropertySampleIndex1 = sampleIndex0Index1Index0PropertySample.get().object(1);
                    Assertions.assertTrue(sampleIndex0Index1Index0PropertySampleIndex1.present());

                    // sample[0][1][0].property.sample[1].value
                    Option<ValueProperty> sampleIndex0Index1Index0PropertySampleIndex1Value = sampleIndex0Index1Index0PropertySampleIndex1.get().get("value");
                    Assertions.assertTrue(sampleIndex0Index1Index0PropertySampleIndex1Value.present());
                    Assertions.assertEquals("value", sampleIndex0Index1Index0PropertySampleIndex1Value.get().value().get());
                });
    }
}
