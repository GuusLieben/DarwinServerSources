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

package org.dockbox.hartshorn.util.introspect.reflect;

import org.dockbox.hartshorn.util.Tristate;
import org.dockbox.hartshorn.util.introspect.IntrospectionEnvironment;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * TODO: #1059 Add documentation
 *
 * @since 0.5.0
 *
 * @author Guus Lieben
 */
public class ReflectionIntrospectionEnvironment implements IntrospectionEnvironment {

    private Tristate parameterNamesAvailable = Tristate.UNDEFINED;

    @Override
    public boolean parameterNamesAvailable() {
        if (this.parameterNamesAvailable == Tristate.UNDEFINED) {
            try {
                Method method = ReflectionIntrospectionEnvironment.class.getDeclaredMethod("$__hartshorn$__ignore",
                        Object.class);
                Parameter[] parameters = method.getParameters();
                String name = parameters[0].getName();
                this.parameterNamesAvailable = "parameterCheck".equals(name)
                        ? Tristate.TRUE
                        : Tristate.FALSE;
            }
            catch (NoSuchMethodException e) {
                this.parameterNamesAvailable = Tristate.FALSE;
            }
        }
        return this.parameterNamesAvailable.booleanValue();
    }

    @SuppressWarnings({ "unused", "DollarSignInName" })
    private void $__hartshorn$__ignore(Object parameterCheck) {
        throw new UnsupportedOperationException("This method is a placeholder used to discover whether parameter names are available. It should never be called.");
    }
}
