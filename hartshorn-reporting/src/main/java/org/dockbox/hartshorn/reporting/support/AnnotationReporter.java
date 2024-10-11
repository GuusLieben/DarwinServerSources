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

import org.dockbox.hartshorn.reporting.DiagnosticsPropertyCollector;
import org.dockbox.hartshorn.reporting.Reportable;
import org.dockbox.hartshorn.util.TypeUtils;

import java.lang.annotation.Annotation;
import java.util.Map;

public class AnnotationReporter<A extends Annotation> implements Reportable {

    private final A annotation;

    public AnnotationReporter(A annotation) {
        this.annotation = annotation;
    }

    @Override
    public void report(DiagnosticsPropertyCollector collector) {
        Class<? extends Annotation> annotationType = annotation.annotationType();
        Map<String, Object> attributes = TypeUtils.getAttributes(annotation);
        collector.property("annotationType").writeString(annotationType.getCanonicalName());
        for (String attributeKey : attributes.keySet()) {
            Object attributeValue = attributes.get(attributeKey);
            var consumer = new ValueAdapterDiagnosticsPropertyWriterConsumer(attributeValue);
            consumer.writeTo(collector.property(attributeKey));
        }
    }
}
