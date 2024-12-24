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

package test.org.dockbox.hartshorn.util;

import java.util.Collection;
import java.util.stream.Stream;

import org.dockbox.hartshorn.util.CollectionUtilities;
import org.dockbox.hartshorn.util.collections.ArrayListMultiMap;
import org.dockbox.hartshorn.util.collections.ConcurrentSetMultiMap;
import org.dockbox.hartshorn.util.collections.ConcurrentSetTreeMultiMap;
import org.dockbox.hartshorn.util.collections.CopyOnWriteArrayListMultiMap;
import org.dockbox.hartshorn.util.collections.HashSetMultiMap;
import org.dockbox.hartshorn.util.collections.MultiMap;
import org.dockbox.hartshorn.util.collections.SynchronizedArrayListMultiMap;
import org.dockbox.hartshorn.util.collections.SynchronizedHashSetMultiMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class MultimapTests {

    public static Stream<Arguments> multiMapImplementations() {
        return Stream.of(
                Arguments.of(new ArrayListMultiMap<>()),
                Arguments.of(new HashSetMultiMap<>()),
                Arguments.of(new CopyOnWriteArrayListMultiMap<>()),
                Arguments.of(new ConcurrentSetMultiMap<>()),
                Arguments.of(new ConcurrentSetTreeMultiMap<>()),
                Arguments.of(new SynchronizedArrayListMultiMap<>()),
                Arguments.of(new SynchronizedHashSetMultiMap<>())
        );
    }

    @ParameterizedTest
    @MethodSource("multiMapImplementations")
    void testAllValuesIsNonNull(MultiMap<String, String> map) {
        Assertions.assertNotNull(map.allValues());
        Assertions.assertTrue(map.allValues().isEmpty());
    }

    @ParameterizedTest
    @MethodSource("multiMapImplementations")
    void testGetReturnsEmptyCollectionIfAbsent(MultiMap<String, String> map) {
        Collection<String> collection = map.get("test");
        Assertions.assertNotNull(collection);
        Assertions.assertTrue(collection.isEmpty());
    }

    @ParameterizedTest
    @MethodSource("multiMapImplementations")
    void testPutCreatesCollection(MultiMap<String, String> map) {
        map.put("test", "test");

        Collection<String> collection = map.get("test");
        Assertions.assertNotNull(collection);
        Assertions.assertFalse(collection.isEmpty());
        Assertions.assertEquals(1, collection.size());

        String value = CollectionUtilities.first(collection);
        Assertions.assertEquals("test", value);
    }

    @ParameterizedTest
    @MethodSource("multiMapImplementations")
    void testPutMultipleAddsToCollection(MultiMap<String, String> map) {
        map.put("test", "test");
        map.put("test", "test2");

        Collection<String> collection = map.get("test");
        Assertions.assertNotNull(collection);
        Assertions.assertFalse(collection.isEmpty());
        Assertions.assertEquals(2, collection.size());

        Assertions.assertTrue(collection.contains("test"));
        Assertions.assertTrue(collection.contains("test2"));
    }

    @ParameterizedTest
    @MethodSource("multiMapImplementations")
    void testPutIfAbsentDoesNotAddDuplicate(MultiMap<String, String> map) {
        map.put("test", "test");
        map.putIfAbsent("test", "test");

        Collection<String> collection = map.get("test");
        Assertions.assertNotNull(collection);
        Assertions.assertFalse(collection.isEmpty());
        Assertions.assertEquals(1, collection.size());

        String value = CollectionUtilities.first(collection);
        Assertions.assertEquals("test", value);
    }

    @ParameterizedTest
    @MethodSource("multiMapImplementations")
    void testPutIfAbsentAddsIfAbsent(MultiMap<String, String> map) {
        map.put("test", "test");
        map.putIfAbsent("test", "test2");

        Assertions.assertEquals(2, map.size());
        Assertions.assertEquals(2, map.allValues().size());

        Collection<String> collection = map.get("test");
        Assertions.assertNotNull(collection);
        Assertions.assertFalse(collection.isEmpty());
        Assertions.assertEquals(2, collection.size());

        Assertions.assertTrue(collection.contains("test"));
        Assertions.assertTrue(collection.contains("test2"));
    }

    @ParameterizedTest
    @MethodSource("multiMapImplementations")
    void testKeySetIsNonNull(MultiMap<String, String> map) {
        Assertions.assertNotNull(map.keySet());
        Assertions.assertTrue(map.keySet().isEmpty());
    }

    @ParameterizedTest
    @MethodSource("multiMapImplementations")
    void testKeySetContainsKey(MultiMap<String, String> map) {
        map.put("test", "test");
        Assertions.assertTrue(map.keySet().contains("test"));
    }

    @ParameterizedTest
    @MethodSource("multiMapImplementations")
    void testKeySetDoesNotContainKeyIfEmpty(MultiMap<String, String> map) {
        Assertions.assertFalse(map.keySet().contains("test"));
    }

    @ParameterizedTest
    @MethodSource("multiMapImplementations")
    void testContainsKeyReturnsTrueIfKeyExists(MultiMap<String, String> map) {
        map.put("test", "test");
        Assertions.assertTrue(map.containsKey("test"));
    }

    @ParameterizedTest
    @MethodSource("multiMapImplementations")
    void testContainsKeyReturnsFalseIfKeyDoesNotExist(MultiMap<String, String> map) {
        Assertions.assertFalse(map.containsKey("test"));
    }

    @ParameterizedTest
    @MethodSource("multiMapImplementations")
    void testContainsValueReturnsTrueIfValueExists(MultiMap<String, String> map) {
        map.put("test", "test");
        Assertions.assertTrue(map.containsValue("test"));
    }

    @ParameterizedTest
    @MethodSource("multiMapImplementations")
    void testContainsValueReturnsFalseIfValueDoesNotExist(MultiMap<String, String> map) {
        Assertions.assertFalse(map.containsValue("test"));
    }

    @ParameterizedTest
    @MethodSource("multiMapImplementations")
    void testContainsEntryReturnsTrueIfEntryExists(MultiMap<String, String> map) {
        map.put("test", "test");
        Assertions.assertTrue(map.containsEntry("test", "test"));
    }

    @ParameterizedTest
    @MethodSource("multiMapImplementations")
    void testContainsEntryReturnsFalseIfEntryDoesNotExist(MultiMap<String, String> map) {
        Assertions.assertFalse(map.containsEntry("test", "test"));
    }

    @ParameterizedTest
    @MethodSource("multiMapImplementations")
    void testRemoveRemovesAllEntries(MultiMap<String, String> map) {
        map.put("test", "test");
        Assertions.assertTrue(map.containsEntry("test", "test"));

        map.remove("test", "test");
        Assertions.assertFalse(map.containsEntry("test", "test"));
    }

    @ParameterizedTest
    @MethodSource("multiMapImplementations")
    void testRemoveKeyRemovesKey(MultiMap<String, String> map) {
        map.put("test", "test");
        Assertions.assertTrue(map.containsKey("test"));

        map.remove("test");
        Assertions.assertFalse(map.containsKey("test"));
    }

    @ParameterizedTest
    @MethodSource("multiMapImplementations")
    void testReplaceReplacesMatchingEntries(MultiMap<String, String> map) {
        map.put("test", "test");
        Assertions.assertTrue(map.containsEntry("test", "test"));

        map.replace("test", "test", "test2");
        Assertions.assertFalse(map.containsEntry("test", "test"));
        Assertions.assertTrue(map.containsEntry("test", "test2"));
    }
}
