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

package org.dockbox.hartshorn.config.annotations;

import org.dockbox.hartshorn.config.PathSerializationSourceConverter;
import org.dockbox.hartshorn.util.introspect.annotations.Extends;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * TODO: #1062 Add documentation
 *
 * @since 0.4.1
 *
 * @author Guus Lieben
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Extends(SerializationSource.class)
@SerializationSource(converter = PathSerializationSourceConverter.class)
public @interface FileSource {
    String value();
    boolean relativeToApplicationPath() default true;
}