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

package org.dockbox.hartshorn.inject.populate;

import org.dockbox.hartshorn.inject.annotations.PropertyValue;
import org.dockbox.hartshorn.inject.provider.ComponentProvider;
import org.dockbox.hartshorn.inject.targets.InjectionPoint;
import org.dockbox.hartshorn.properties.ListProperty;
import org.dockbox.hartshorn.properties.ObjectProperty;
import org.dockbox.hartshorn.properties.PropertyRegistry;
import org.dockbox.hartshorn.properties.ValueProperty;
import org.dockbox.hartshorn.util.introspect.convert.ConversionService;
import org.dockbox.hartshorn.util.introspect.view.TypeView;
import org.dockbox.hartshorn.util.option.Option;

/**
 * A parameter resolver that resolves parameters annotated with {@link PropertyValue}. Values are resolved from the {@link
 * org.dockbox.hartshorn.properties.PropertyRegistry}, or from the {@link PropertyValue#defaultValue()} if no value is found.
 *
 * <p>If the parameter type is {@link ValueProperty}, {@link ListProperty}, or {@link ObjectProperty}, the value is
 * resolved directly from the registry. Otherwise, the raw value is resolved and attempted to be converted to the
 * target type.
 *
 * @since 0.7.0
 *
 * @author Guus Lieben
 */
public class InjectPropertyParameterResolver implements InjectParameterResolver {

    private final ComponentProvider componentProvider;
    private ConversionService conversionService;

    public InjectPropertyParameterResolver(ComponentProvider componentProvider) {
        this.componentProvider = componentProvider;
    }

    @Override
    public boolean accepts(InjectionPoint injectionPoint) {
        return injectionPoint.injectionPoint().annotations().has(PropertyValue.class);
    }

    @Override
    public Object resolve(InjectionPoint injectionPoint, PopulateComponentContext<?> context) {
        PropertyRegistry propertyRegistry = context.application().environment().propertyRegistry();
        PropertyValue propertyValueAnnotation = injectionPoint.injectionPoint().annotations().get(PropertyValue.class).get();
        String propertyName = propertyValueAnnotation.name();
        TypeView<?> targetType = injectionPoint.type();
        if (propertyRegistry.contains(propertyName)) {
            Option<?> value;
            if (targetType.is(ValueProperty.class)) {
                value = propertyRegistry.get(propertyName);
            }
            else if (targetType.is(ListProperty.class)) {
                value = propertyRegistry.list(propertyName);
            }
            else if (targetType.is(ObjectProperty.class)) {
                value = propertyRegistry.object(propertyName);
            }
            else {
                value = this.getPropertyValue(injectionPoint, propertyRegistry, propertyName);
            }
            if (value.present()) {
                return value.get();
            }
        }
        return this.conversionService().convert(propertyValueAnnotation.defaultValue(), targetType.type());
    }

    private Option<?> getPropertyValue(InjectionPoint injectionPoint, PropertyRegistry propertyRegistry, String propertyName) {
        return propertyRegistry.value(propertyName, property -> {
            return Option.of(this.conversionService().convert(property, injectionPoint.type().type()));
        });
    }

    private ConversionService conversionService() {
        if (null == this.conversionService) {
            this.conversionService = this.componentProvider.get(ConversionService.class);
        }
        return this.conversionService;
    }
}
