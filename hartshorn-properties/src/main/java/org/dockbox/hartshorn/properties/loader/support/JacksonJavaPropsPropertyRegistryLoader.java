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

package org.dockbox.hartshorn.properties.loader.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.javaprop.JavaPropsMapper;

import org.dockbox.hartshorn.properties.loader.StandardPropertyPathFormatter;
import org.dockbox.hartshorn.properties.loader.path.PropertyPathFormatter;
import org.dockbox.hartshorn.util.Customizer;

import java.util.Set;

public class JacksonJavaPropsPropertyRegistryLoader extends JacksonPropertyRegistryLoader {

    private final Customizer<JavaPropsMapper.Builder> customizer;

    public JacksonJavaPropsPropertyRegistryLoader() {
        this(new StandardPropertyPathFormatter());
    }

    public JacksonJavaPropsPropertyRegistryLoader(PropertyPathFormatter formatter) {
        this(formatter, Customizer.useDefaults());
    }

    public JacksonJavaPropsPropertyRegistryLoader(PropertyPathFormatter formatter, Customizer<JavaPropsMapper.Builder> customizer) {
        super(formatter);
        this.customizer = customizer;
    }

    @Override
    protected ObjectMapper createObjectMapper() {
        JavaPropsMapper.Builder builder = JavaPropsMapper.builder();
        this.customizer.configure(builder);
        return builder.build();
    }

    @Override
    protected Set<String> supportedExtensions() {
        return Set.of("properties");
    }
}
