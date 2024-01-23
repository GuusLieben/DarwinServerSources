/*
 * Copyright 2019-2023 the original author or authors.
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

package org.dockbox.hartshorn.config.jackson;

import org.dockbox.hartshorn.application.context.ApplicationContext;
import org.dockbox.hartshorn.component.Configuration;
import org.dockbox.hartshorn.component.condition.RequiresActivator;
import org.dockbox.hartshorn.component.condition.RequiresClass;
import org.dockbox.hartshorn.component.processing.Binds;
import org.dockbox.hartshorn.config.ObjectMapper;
import org.dockbox.hartshorn.config.annotations.UseConfigurations;
import org.dockbox.hartshorn.config.jackson.mapping.JavaPropsDataMapper;
import org.dockbox.hartshorn.config.jackson.mapping.JsonDataMapper;
import org.dockbox.hartshorn.config.jackson.mapping.TomlDataMapper;
import org.dockbox.hartshorn.config.jackson.mapping.XmlDataMapper;
import org.dockbox.hartshorn.config.jackson.mapping.YamlDataMapper;
import org.dockbox.hartshorn.inject.Named;
import org.dockbox.hartshorn.util.introspect.Introspector;

@Configuration
@RequiresActivator(UseConfigurations.class)
@RequiresClass("com.fasterxml.jackson.databind.ObjectMapper")
public class JacksonConfiguration {

    @Binds
    public ObjectMapper objectMapper(ApplicationContext applicationContext) {
        return new JacksonObjectMapper(applicationContext);
    }

    @Binds(before = ObjectMapper.class)
    public JacksonObjectMapperConfigurator mapperConfigurator(Introspector introspector) {
        return new StandardJacksonObjectMapperConfigurator(introspector);
    }

    @Configuration
    @RequiresClass("com.fasterxml.jackson.dataformat.javaprop.JavaPropsMapper")
    public static class JacksonPropertiesMapperConfiguration {

        @Named("properties")
        @Binds(before = ObjectMapper.class)
        public JacksonDataMapper properties() {
            return new JavaPropsDataMapper();
        }
    }

    @Configuration
    @RequiresClass("com.fasterxml.jackson.databind.json.JsonMapper")
    public static class JacksonJsonMapperConfiguration {

        @Named("json")
        @Binds(before = ObjectMapper.class)
        public JacksonDataMapper json() {
            return new JsonDataMapper();
        }
    }

    @Configuration
    @RequiresClass("com.fasterxml.jackson.dataformat.toml.TomlMapper")
    public static class JacksonTomlMapperConfiguration {

        @Named("toml")
        @Binds(before = ObjectMapper.class)
        public JacksonDataMapper toml() {
            return new TomlDataMapper();
        }
    }

    @Configuration
    @RequiresClass("com.fasterxml.jackson.dataformat.xml.XmlMapper")
    public static class JacksonXmlMapperConfiguration {

        @Named("xml")
        @Binds(before = ObjectMapper.class)
        public JacksonDataMapper xml() {
            return new XmlDataMapper();
        }
    }

    @Configuration
    @RequiresClass("com.fasterxml.jackson.dataformat.yaml.YAMLMapper")
    public static class JacksonYamlMapperConfiguration {

        @Named("yaml")
        @Binds(before = ObjectMapper.class)
        public JacksonDataMapper yml() {
            return new YamlDataMapper();
        }
    }
}