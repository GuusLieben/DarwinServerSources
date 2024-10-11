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

package org.dockbox.hartshorn.reporting.support;

import org.dockbox.hartshorn.reporting.DiagnosticsPropertyWriter;
import org.dockbox.hartshorn.reporting.Reportable;

import java.lang.annotation.Annotation;
import java.util.stream.Stream;

public class ValueAdapterDiagnosticsPropertyWriterConsumer implements DiagnosticsPropertyWriterConsumer {

    private final Object object;

    public ValueAdapterDiagnosticsPropertyWriterConsumer(Object object) {
        this.object = object;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void writeTo(DiagnosticsPropertyWriter writer) {
        switch (object) {
            case String string -> writer.writeString(string);
            case Integer integer -> writer.writeInt(integer);
            case Long longNumber -> writer.writeLong(longNumber);
            case Float floatNumber -> writer.writeFloat(floatNumber);
            case Double doubleNumber -> writer.writeDouble(doubleNumber);
            case Boolean booleanValue -> writer.writeBoolean(booleanValue);
            case Enum enumValue -> writer.writeEnum(enumValue);
            case Reportable reportable -> writer.writeDelegate(reportable);
            case Class<?> clazz -> writer.writeString(clazz.getCanonicalName());
            case Annotation annotation -> writer.writeDelegate(new AnnotationReporter<>(annotation));
            case String[] strings -> writer.writeStrings(strings);
            case int[] integers -> writer.writeInts(integers);
            case long[] longs -> writer.writeLongs(longs);
            case float[] floats -> writer.writeFloats(floats);
            case double[] doubles -> writer.writeDoubles(doubles);
            case boolean[] booleans -> writer.writeBooleans(booleans);
            case Enum[] enums -> writer.writeEnums(enums);
            case Reportable[] reportables -> writer.writeDelegates(reportables);
            case Class[] clazz -> {
                String[] array = Stream.of(clazz)
                        .map(Class::getCanonicalName)
                        .toArray(String[]::new);
                writer.writeStrings(array);
            }
            case Annotation[] annotations -> {
                AnnotationReporter[] array = Stream.of(annotations)
                        .map(AnnotationReporter::new)
                        .toArray(AnnotationReporter[]::new);
                writer.writeDelegates(array);
            }
            default -> throw new IllegalStateException("Unexpected value: " + object);
        }
    }
}
