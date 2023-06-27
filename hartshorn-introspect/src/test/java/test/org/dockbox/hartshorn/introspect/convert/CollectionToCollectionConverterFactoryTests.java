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

package test.org.dockbox.hartshorn.introspect.convert;

import org.dockbox.hartshorn.util.introspect.Introspector;
import org.dockbox.hartshorn.util.introspect.convert.Converter;
import org.dockbox.hartshorn.util.introspect.convert.support.CollectionToCollectionConverterFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class CollectionToCollectionConverterFactoryTests {

    @Test
    void convertHashSetToArrayList() {
        final Set<Integer> input = new HashSet<>(Arrays.asList(1, 2, 3));
        final List<Integer> expectedOutput = Arrays.asList(1, 2, 3);

        final Introspector introspector = ConverterIntrospectionHelper.createIntrospectorForCollection(ArrayList.class, ArrayList::new);
        final Converter<Collection<?>, ArrayList> converter = new CollectionToCollectionConverterFactory(introspector).create(ArrayList.class);
        final List<Integer> output = converter.convert(input);

        Assertions.assertTrue(output instanceof ArrayList);
        Assertions.assertEquals(expectedOutput.size(), output.size());
        Assertions.assertTrue(output.containsAll(expectedOutput));
    }

    @Test
    void convertLinkedHashSetToLinkedList() {
        final Set<String> input = new LinkedHashSet<>(Arrays.asList("foo", "bar", "baz"));
        final List<String> expectedOutput = new LinkedList<>(Arrays.asList("foo", "bar", "baz"));

        final Introspector introspector = ConverterIntrospectionHelper.createIntrospectorForCollection(LinkedList.class, LinkedList::new);
        final Converter<Collection<?>, LinkedList> converter = new CollectionToCollectionConverterFactory(introspector).create(LinkedList.class);
        final List<String> output = converter.convert(input);

        Assertions.assertTrue(output instanceof LinkedList);
        Assertions.assertEquals(expectedOutput.size(), output.size());
        Assertions.assertTrue(output.containsAll(expectedOutput));
    }

    @Test
    void convertOrderedSetToOrderedSet() {
        final Set<Integer> input = new LinkedHashSet<>(Arrays.asList(1, 2, 3));
        final Set<Integer> expectedOutput = new LinkedHashSet<>(Arrays.asList(1, 2, 3));

        final Introspector introspector = ConverterIntrospectionHelper.createIntrospectorForCollection(LinkedHashSet.class, LinkedHashSet::new);
        final Converter<Collection<?>, LinkedHashSet> converter = new CollectionToCollectionConverterFactory(introspector).create(LinkedHashSet.class);
        final Set<?> output = converter.convert(input);

        Assertions.assertTrue(output instanceof LinkedHashSet);
        Assertions.assertEquals(expectedOutput.size(), output.size());

        // Assert order is preserved
        final List<?> outputList = new ArrayList<>(output);
        final List<?> expectedOutputList = new ArrayList<>(expectedOutput);
        for (int i = 0; i < outputList.size(); i++) {
            Assertions.assertEquals(expectedOutputList.get(i), outputList.get(i));
        }
    }
}
