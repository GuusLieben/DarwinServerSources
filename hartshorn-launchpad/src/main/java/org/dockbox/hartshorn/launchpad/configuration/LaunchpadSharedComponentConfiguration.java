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

package org.dockbox.hartshorn.launchpad.configuration;

import org.dockbox.hartshorn.inject.annotations.CompositeMember;
import org.dockbox.hartshorn.inject.annotations.InfrastructurePriority;
import org.dockbox.hartshorn.inject.annotations.Strict;
import org.dockbox.hartshorn.inject.annotations.configuration.Configuration;
import org.dockbox.hartshorn.inject.annotations.configuration.Prototype;
import org.dockbox.hartshorn.inject.annotations.configuration.Singleton;
import org.dockbox.hartshorn.inject.collection.ComponentCollection;
import org.dockbox.hartshorn.inject.component.ComponentContainer;
import org.dockbox.hartshorn.inject.component.ComponentRegistry;
import org.dockbox.hartshorn.inject.targets.InjectionPoint;
import org.dockbox.hartshorn.launchpad.annotations.UseLaunchpad;
import org.dockbox.hartshorn.launchpad.condition.RequiresActivator;
import org.dockbox.hartshorn.properties.ValueProperty;
import org.dockbox.hartshorn.properties.convert.ValuePropertyToObjectConverterFactory;
import org.dockbox.hartshorn.util.introspect.Introspector;
import org.dockbox.hartshorn.util.introspect.convert.ConversionService;
import org.dockbox.hartshorn.util.introspect.convert.Converter;
import org.dockbox.hartshorn.util.introspect.convert.ConverterFactory;
import org.dockbox.hartshorn.util.introspect.convert.ConvertersCustomizer;
import org.dockbox.hartshorn.util.introspect.convert.GenericConverter;
import org.dockbox.hartshorn.util.introspect.convert.StandardConversionService;
import org.dockbox.hartshorn.util.introspect.view.ExecutableElementView;
import org.dockbox.hartshorn.util.introspect.view.FieldView;
import org.dockbox.hartshorn.util.introspect.view.ParameterView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuration for core components that are required by the framework. This includes the {@link Logger} and
 * {@link ConversionService}.
 *
 * @since 0.4.6
 *
 * @author Guus Lieben
 */
@Configuration
@RequiresActivator(UseLaunchpad.class)
public class LaunchpadSharedComponentConfiguration {

    @Prototype
    @InfrastructurePriority
    public Logger logger(InjectionPoint injectionPoint, ComponentRegistry componentRegistry) {
        Class<?> declaringType = switch(injectionPoint.injectionPoint()) {
            case ExecutableElementView<?> executableElementView -> executableElementView.declaredBy().type();
            case FieldView<?, ?> fieldView -> fieldView.declaredBy().type();
            case ParameterView<?> parameterView -> parameterView.declaredBy().declaredBy().type();
            default -> throw new IllegalStateException("Unexpected value: " + injectionPoint.injectionPoint());
        };

        return componentRegistry.container(declaringType)
            .map(ComponentContainer::name)
            .map(LoggerFactory::getLogger)
            .orElseGet(() -> LoggerFactory.getLogger(declaringType));
    }

    @Singleton
    @InfrastructurePriority
    public ConversionService conversionService(
            Introspector introspector,
            @Strict(false) ComponentCollection<GenericConverter> genericConverters,
            @Strict(false) ComponentCollection<ConverterFactory<?, ?>> converterFactories,
            @Strict(false) ComponentCollection<Converter<?, ?>> converters,
            @Strict(false) ComponentCollection<ConvertersCustomizer> customizers
    ) {
        StandardConversionService service = new StandardConversionService(introspector).withDefaults();

        genericConverters.forEach(service::addConverter);
        converterFactories.forEach(service::addConverterFactory);
        converters.forEach(service::addConverter);

        customizers.forEach(customizer -> customizer.configure(service, service));

        return service;
    }

    @Singleton
    @CompositeMember
    public ConvertersCustomizer additionalInternalConvertersCustomizer() {
        return (conversionService, converterRegistry) -> {
            // From Hartshorn Properties
            converterRegistry.addConverterFactory(ValueProperty.class, new ValuePropertyToObjectConverterFactory(conversionService));
        };
    }
}
