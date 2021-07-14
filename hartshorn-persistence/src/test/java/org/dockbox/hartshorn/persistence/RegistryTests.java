/*
 * Copyright (C) 2020 Guus Lieben
 *
 * This framework is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library. If not, see {@literal<http://www.gnu.org/licenses/>}.
 */

package org.dockbox.hartshorn.persistence;

import org.dockbox.hartshorn.api.domain.Exceptional;
import org.dockbox.hartshorn.persistence.registry.Registry;
import org.dockbox.hartshorn.persistence.registry.RegistryColumn;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RegistryTests {

    @Test
    public void testThatRegistryCanGetCorrectMatchingColumns() {
        Registry<Registry<String>> testRegistry = this.buildTestRegistry();

        RegistryColumn<RegistryColumn<String>> result = testRegistry
                .matchingColumns(TestIdentifier.BRICK)
                .map(r -> r.matchingColumns(TestIdentifier.FULLBLOCK));

        Assertions.assertTrue(result.first().get().contains("Brick Fullblock1"));
        Assertions.assertTrue(result.first().get().contains("Brick Fullblock2"));
    }

    private Registry<Registry<String>> buildTestRegistry() {
        return new Registry<Registry<String>>()
                .addColumn(
                        TestIdentifier.BRICK,
                        new Registry<String>()
                                .addColumn(TestIdentifier.FULLBLOCK, "Brick Fullblock1", "Brick Fullblock2")
                                .addColumn(TestIdentifier.STAIR, "Brick Stair1")
                                .addColumn(TestIdentifier.SLAB, "Brick Slab1"))
                .addColumn(
                        TestIdentifier.SANDSTONE,
                        new Registry<String>()
                                .addColumn(TestIdentifier.FULLBLOCK, "Sandstone Fullblock1")
                                .addColumn(TestIdentifier.STAIR, "Sandstone Stair1"))
                .addColumn(
                        TestIdentifier.COBBLESTONE,
                        new Registry<String>().addColumn(TestIdentifier.FULLBLOCK, "Cobblestone Fullblock1"));
    }

    @Test
    public void testThatRegistryCanAddData() throws NullPointerException {
        Registry<Registry<String>> testRegistry = this.buildTestRegistry();

        testRegistry
                .get(TestIdentifier.COBBLESTONE)
                .first()
                .present(r -> {
                    r.add(TestIdentifier.FULLBLOCK, "Cobblestone Fullblock2");
                    r.add(TestIdentifier.STAIR, "Cobblestone Stair1");
                });

        Exceptional<Registry<String>> eCobblestoneRegistry =
                testRegistry.matchingColumns(TestIdentifier.COBBLESTONE).first();

        Registry<String> cobblestoneRegistry = eCobblestoneRegistry.get();
        RegistryColumn<String> fullblocks =
                cobblestoneRegistry.matchingColumns(TestIdentifier.FULLBLOCK);

        Assertions.assertTrue(cobblestoneRegistry.containsColumns(TestIdentifier.STAIR));
        Assertions.assertTrue(fullblocks.contains("Cobblestone Fullblock2"));
        Assertions.assertEquals(2, fullblocks.size());
    }

    @Test
    public void testThatRegistryCanRemoveColumns() {
        Registry<Registry<String>> testRegistry = this.buildTestRegistry();

        testRegistry.removeColumns(TestIdentifier.BRICK, TestIdentifier.SANDSTONE);

        Assertions.assertFalse(testRegistry.containsColumns(TestIdentifier.BRICK));
        Assertions.assertFalse(testRegistry.containsColumns(TestIdentifier.SANDSTONE));
    }

    @Test
    public void testThatRegistryCanBeFilteredByColumn() {
        Registry<Registry<String>> testRegistry = this.buildTestRegistry();

        Registry<Registry<String>> result = testRegistry.removeColumnsIf(TestIdentifier.BRICK::same);

        Assertions.assertTrue(result.containsColumns(TestIdentifier.SANDSTONE, TestIdentifier.COBBLESTONE));
        Assertions.assertFalse(result.containsColumns(TestIdentifier.BRICK));
    }

    @Test
    public void testThatRegistryCanBeFilteredByValue() {
        Registry<Registry<String>> testRegistry = this.buildTestRegistry();

        Registry<Registry<String>> result = testRegistry.removeValuesIf(r -> 2 <= r.size());
        int brickColumnSize = result.matchingColumns(TestIdentifier.BRICK).size();

        Assertions.assertTrue(result.containsColumns(TestIdentifier.BRICK, TestIdentifier.SANDSTONE, TestIdentifier.COBBLESTONE));
        Assertions.assertEquals(0, brickColumnSize);
    }

    @Test
    public void testGetOrCreateRegistryColumn() {
        Registry<Registry<String>> testRegistry = this.buildTestRegistry();

        RegistryColumn<Registry<String>> column = testRegistry.getColumnOrCreate(TestIdentifier.WOOD, new Registry<>());

        Assertions.assertEquals(1, column.size());
        Assertions.assertTrue(testRegistry.containsColumns(TestIdentifier.WOOD));
    }

    @Test
    public void testThatRegistryCanBeAdded() {
        Registry<Registry<String>> testRegistry = this.buildTestRegistry();

        Registry<Registry<String>> secondRegistry = new Registry<Registry<String>>()
                .addColumn(
                        TestIdentifier.SANDSTONE,
                        new Registry<String>().addColumn(TestIdentifier.STAIR, "Sandstone Stair2"))
                .addColumn(
                        TestIdentifier.WOOD,
                        new Registry<String>().addColumn(TestIdentifier.STAIR, "Wooden Stair1"));

        testRegistry.addRegistry(secondRegistry);
        RegistryColumn<Object> result = testRegistry
                .matchingColumns(TestIdentifier.SANDSTONE, TestIdentifier.WOOD)
                .map(r -> r.matchingColumns(TestIdentifier.STAIR).safe(0).orNull());

        Assertions.assertTrue(result.contains("Sandstone Stair1"));
        Assertions.assertTrue(result.contains("Sandstone Stair2"));
        Assertions.assertTrue(result.contains("Wooden Stair1"));
    }
}
