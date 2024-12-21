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

import org.dockbox.hartshorn.properties.ListProperty;
import org.dockbox.hartshorn.properties.MapPropertyRegistry;
import org.dockbox.hartshorn.properties.ObjectProperty;
import org.dockbox.hartshorn.properties.PropertyRegistry;
import org.dockbox.hartshorn.properties.ValueProperty;
import org.dockbox.hartshorn.properties.loader.PropertyRegistryLoader;
import org.dockbox.hartshorn.properties.loader.StandardPropertyPathFormatter;
import org.dockbox.hartshorn.properties.loader.support.JacksonYamlPropertyRegistryLoader;
import org.dockbox.hartshorn.util.option.Option;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

public class PropertyRegistryTests {

    @Test
    void testComplexRegistryAccessing() throws IOException {
        PropertyRegistryLoader loader = new JacksonYamlPropertyRegistryLoader(new StandardPropertyPathFormatter());
        Path path = Path.of("src/test/resources/complex-configuration.yml");

        PropertyRegistry registry = new MapPropertyRegistry();
        loader.loadRegistry(registry, path);

        Option<ObjectProperty> complexObject = registry.object("sample.complex");
        Assertions.assertTrue(complexObject.present());
        complexObject.peek(object -> {
            Assertions.assertEquals("sample.complex", object.name());

            Option<ListProperty> configurationList = object.list("configuration");
            Assertions.assertTrue(configurationList.present());
            configurationList.peek(list -> {
                Assertions.assertEquals(3, list.size());
                Assertions.assertEquals("sample.complex.configuration", list.name());

                assertConfigurationObject(list, 0, "name1", "value1");
                assertConfigurationObject(list, 1, "name2", "value2");
                assertConfigurationObject(list, 2, "name3", "value3");
            });

            Option<ListProperty> valuesList = object.list("values");
            Assertions.assertTrue(valuesList.present());
            valuesList.peek(list -> {
                Assertions.assertEquals(3, list.size());
                Assertions.assertEquals("sample.complex.values", list.name());

                Option<ValueProperty> valueOne = list.get(0);
                Assertions.assertTrue(valueOne.present());
                Assertions.assertEquals("value1", valueOne.get().value().get());
                Assertions.assertEquals("sample.complex.values[0]", valueOne.get().name());

                Option<ValueProperty> valueTwo = list.get(1);
                Assertions.assertTrue(valueTwo.present());
                Assertions.assertEquals("value2", valueTwo.get().value().get());
                Assertions.assertEquals("sample.complex.values[1]", valueTwo.get().name());

                Option<ValueProperty> valueThree = list.get(2);
                Assertions.assertTrue(valueThree.present());
                Assertions.assertEquals("value3", valueThree.get().value().get());
                Assertions.assertEquals("sample.complex.values[2]", valueThree.get().name());
            });

            Option<ValueProperty> flatValue = object.get("flat");
            Assertions.assertTrue(flatValue.present());
            Assertions.assertEquals("value1", flatValue.get().value().get());
            Assertions.assertEquals("sample.complex.flat", flatValue.get().name());
        });
    }

    private static void assertConfigurationObject(ListProperty listProperty, int index, String expectedName, String expectedValue) {
        Option<ObjectProperty> configurationObject = listProperty.object(index);
        Assertions.assertTrue(configurationObject.present());
        configurationObject.peek(object -> {
            Assertions.assertEquals("sample.complex.configuration[" + index + "]", object.name());

            Option<ValueProperty> name = object.get("name");
            Assertions.assertTrue(name.present());
            Assertions.assertEquals(expectedName, name.get().value().get());
            Assertions.assertEquals("sample.complex.configuration[" + index + "].name", name.get().name());

            Option<ValueProperty> value = object.get("value");
            Assertions.assertTrue(value.present());
            Assertions.assertEquals(expectedValue, value.get().value().get());
            Assertions.assertEquals("sample.complex.configuration[" + index + "].value", value.get().name());
        });
    }
}
