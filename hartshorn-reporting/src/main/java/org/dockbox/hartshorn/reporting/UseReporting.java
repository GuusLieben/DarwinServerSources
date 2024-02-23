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

package org.dockbox.hartshorn.reporting;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.dockbox.hartshorn.component.processing.ServiceActivator;

/**
 * A {@link ServiceActivator} annotation that enables the default configurations for reporting
 * capabilities.
 *
 * @since 0.5.0
 *
 * @see ReportingConfiguration
 *
 * @author Guus Lieben
 */
@ServiceActivator
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface UseReporting {
}
