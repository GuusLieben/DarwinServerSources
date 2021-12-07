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

package org.dockbox.hartshorn.data.mapping;

import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.domain.Exceptional;
import org.dockbox.hartshorn.data.Element;
import org.dockbox.hartshorn.data.EntityElement;
import org.dockbox.hartshorn.data.FileFormat;
import org.dockbox.hartshorn.data.FileFormats;
import org.dockbox.hartshorn.data.MultiElement;
import org.dockbox.hartshorn.data.NestedElement;
import org.dockbox.hartshorn.data.PersistentElement;
import org.dockbox.hartshorn.data.jackson.JacksonObjectMapper;
import org.dockbox.hartshorn.testsuite.HartshornTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import javax.inject.Inject;

import lombok.Getter;

@HartshornTest
public class ObjectMappingTests {

    @Inject
    @Getter
    private ApplicationContext applicationContext;

    private static Stream<Arguments> serialisationElements() {
        return Stream.of(
                Arguments.of(FileFormats.JSON, new PersistentElement(), "{\"name\":\"sample\"}"),
                Arguments.of(FileFormats.YAML, new PersistentElement(), "name: sample"),
                // Note that the element is not an explicit entity, so no alias is used for the root element in XML
                Arguments.of(FileFormats.XML, new PersistentElement(), "<PersistentElement><name>sample</name></PersistentElement>"),
                Arguments.of(FileFormats.TOML, new PersistentElement(), "name='sample'"),
                Arguments.of(FileFormats.PROPERTIES, new PersistentElement(), "name=sample"),

                Arguments.of(FileFormats.JSON, new EntityElement(), "{\"name\":\"sample\"}"),
                Arguments.of(FileFormats.YAML, new EntityElement(), "name: sample"),
                Arguments.of(FileFormats.XML, new EntityElement(), "<entity><name>sample</name></entity>"),
                Arguments.of(FileFormats.TOML, new EntityElement(), "name='sample'"),
                Arguments.of(FileFormats.PROPERTIES, new EntityElement(), "name=sample"),

                Arguments.of(FileFormats.JSON, new NestedElement(new EntityElement()), "{\"child\":{\"name\":\"sample\"}}"),
                Arguments.of(FileFormats.YAML, new NestedElement(new EntityElement()), "child:\n  name: sample"),
                Arguments.of(FileFormats.XML, new NestedElement(new EntityElement()), "<NestedElement><child><name>sample</name></child></NestedElement>"),
                Arguments.of(FileFormats.TOML, new NestedElement(new EntityElement()), "child.name='sample'"),
                Arguments.of(FileFormats.PROPERTIES, new NestedElement(new EntityElement()), "child.name=sample"),

                Arguments.of(FileFormats.JSON, new MultiElement(), "{\"name\":\"sample\",\"other\":\"sample\"}"),
                Arguments.of(FileFormats.YAML, new MultiElement(), "name: sample\nother: sample"),
                Arguments.of(FileFormats.XML, new MultiElement(), "<MultiElement><name>sample</name><other>sample</other></MultiElement>"),
                Arguments.of(FileFormats.TOML, new MultiElement(), "name='sample'\nother='sample'"),
                Arguments.of(FileFormats.PROPERTIES, new MultiElement(), "name=sample\nother=sample")
        );
    }

    @ParameterizedTest
    @MethodSource("serialisationElements")
    void testObjectSerialisation(final FileFormat fileFormat, final Element content, final String expected) {
        final ObjectMapper mapper = this.applicationContext().get(JacksonObjectMapper.class);
        mapper.fileType(fileFormat);

        content.name("sample");
        final Exceptional<String> result = mapper.write(content);

        Assertions.assertTrue(result.present());
        Assertions.assertEquals(expected.replaceAll("[ \n]+", ""), result.get().replaceAll("[ \n\r]+", ""));
    }

    @ParameterizedTest
    @MethodSource("serialisationElements")
    void testObjectDeserialisation(final FileFormat fileFormat, final Element expected, final String content) {
        final ObjectMapper mapper = this.applicationContext().get(JacksonObjectMapper.class);
        mapper.fileType(fileFormat);
        expected.name("sample");

        final Exceptional<? extends Element> result = mapper.read(content, expected.getClass());

        if (result.absent()) throw new RuntimeException(result.error().getMessage());
        Assertions.assertTrue(result.present());
        Assertions.assertEquals(expected.getClass(), result.type());
        Assertions.assertEquals(expected, result.get());
    }
}