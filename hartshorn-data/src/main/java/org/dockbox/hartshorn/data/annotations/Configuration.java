/*
 * Copyright 2019-2022 the original author or authors.
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

package org.dockbox.hartshorn.data.annotations;

import org.dockbox.hartshorn.data.ConfigurationServicePreProcessor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Component type to specify a source for a configuration file. This supports files with any registered
 * {@link org.dockbox.hartshorn.data.ResourceLookupStrategy}. For example a {@link org.dockbox.hartshorn.data.ClassPathResourceLookupStrategy}
 * will accept a source formatted as {@code classpath:filename}.
 *
 * <p>The {@link #value()} does not have to include the file extension, the file format is automatically adjusted based on available files if
 * no explicit file extension is provided.
 *
 * <p>Assuming there is a file called `demo.yml` on the classpath, the following will adapt to {@link org.dockbox.hartshorn.data.FileFormats#YAML}.
 * <pre>{@code
 * @Component
 * @Configuration("classpath:demo")
 * public class SampleClassPathConfiguration {
 *    @Value("sample.value")
 *    private final String value = "default value if key does not exist";
 * }
 * }</pre>
 *
 * @see Value
 * @see org.dockbox.hartshorn.data.mapping.ObjectMapper
 * @see org.dockbox.hartshorn.data.ResourceLookupStrategy
 * @see ConfigurationServicePreProcessor
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Configuration {
    String[] value();
    boolean failOnMissing() default false;
}