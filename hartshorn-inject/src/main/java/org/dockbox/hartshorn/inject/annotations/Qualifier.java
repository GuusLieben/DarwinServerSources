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

package org.dockbox.hartshorn.inject.annotations;

import org.dockbox.hartshorn.inject.QualifierKey;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Meta annotation to mark annotations as qualifiers. Qualifier annotations allow for the creation of
 * multiple bindings for the same type, which can be distinguished by the qualifier annotation and potential
 * attributes of the qualifier annotation.
 *
 * <p>For example, given an API that has multiple versions, you can create a qualifier annotation
 * that allows you to bind different implementations for each version:
 *
 * <pre>{@code
 * @Qualifier
 * @Retention(RetentionPolicy.RUNTIME)
 * @Target(...)
 * public @interface VersionQualifier {
 *    Version value();
 *    enum Version {
 *       V1, V2, V3
 *    }
 * }
 * }</pre>
 *
 * <p>Then, you can use this annotation to create and consume multiple bindings for the same type:
 *
 * <pre>{@code
 * @Singleton
 * @VersionQualifier(Version.V1)
 * public DataRepository testV1() { ... }
 *
 * @Inject
 * @VersionQualifier(Version.V1)
 * private DataRepository repository;
 * }</pre>
 *
 * @since 0.6.0
 *
 * @see QualifierKey
 *
 * @author Guus Lieben
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface Qualifier {
}
