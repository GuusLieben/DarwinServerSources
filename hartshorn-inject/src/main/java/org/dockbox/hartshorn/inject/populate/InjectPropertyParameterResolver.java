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

import org.dockbox.hartshorn.inject.annotations.Value;
import org.dockbox.hartshorn.inject.provider.ComponentProvider;
import org.dockbox.hartshorn.inject.targets.InjectionPoint;
import org.dockbox.hartshorn.properties.ListProperty;
import org.dockbox.hartshorn.properties.ObjectProperty;
import org.dockbox.hartshorn.properties.PropertyRegistry;
import org.dockbox.hartshorn.properties.ValueProperty;
import org.dockbox.hartshorn.util.introspect.convert.ConversionService;
import org.dockbox.hartshorn.util.introspect.view.TypeView;
import org.dockbox.hartshorn.util.option.Option;
import org.jetbrains.annotations.Nullable;

public class InjectPropertyParameterResolver implements InjectParameterResolver {

    private final ComponentProvider componentProvider;
    private ConversionService conversionService;

    public InjectPropertyParameterResolver(ComponentProvider componentProvider) {
        this.componentProvider = componentProvider;
    }

    @Override
    public boolean accepts(InjectionPoint injectionPoint) {
        return injectionPoint.injectionPoint().annotations().has(Value.class);
    }

    @Override
    public Object resolve(InjectionPoint injectionPoint, PopulateComponentContext<?> context) {
        PropertyRegistry propertyRegistry = context.application().environment().propertyRegistry();
        Value valueAnnotation = injectionPoint.injectionPoint().annotations().get(Value.class).get();
        String propertyName = valueAnnotation.name();
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
        return this.conversionService().convert(valueAnnotation.defaultValue(), targetType.type());
    }

    @Nullable
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
