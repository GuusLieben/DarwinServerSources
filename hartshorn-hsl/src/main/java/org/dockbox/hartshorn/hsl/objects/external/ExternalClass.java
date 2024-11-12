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

package org.dockbox.hartshorn.hsl.objects.external;

import java.util.List;
import java.util.Map;

import org.dockbox.hartshorn.hsl.ScriptEvaluationError;
import org.dockbox.hartshorn.hsl.interpreter.Interpreter;
import org.dockbox.hartshorn.hsl.objects.ClassReference;
import org.dockbox.hartshorn.hsl.objects.InstanceReference;
import org.dockbox.hartshorn.hsl.objects.MethodReference;
import org.dockbox.hartshorn.hsl.objects.virtual.VirtualFunction;
import org.dockbox.hartshorn.hsl.runtime.Phase;
import org.dockbox.hartshorn.hsl.runtime.ScriptRuntime;
import org.dockbox.hartshorn.hsl.token.Token;
import org.dockbox.hartshorn.util.ApplicationException;
import org.dockbox.hartshorn.util.ObjectDescriber;
import org.dockbox.hartshorn.util.introspect.view.ConstructorView;
import org.dockbox.hartshorn.util.introspect.view.TypeView;

/**
 * Represents a Java class that can be called from an HSL runtime. This class can be
 * used to create a new instance of the class. This requires the class to be imported
 * by the responsible {@link ScriptRuntime} through {@link ScriptRuntime#imports(Map)}.
 *
 * <pre>{@code
 * AbstractHslRuntime runtime = ...;
 * runtime.imports(Map.of("MyClass", MyClass.class));
 * runtime.run("var instance = MyClass();");
 * }</pre>
 *
 * @param type The type represented by this reference.
 * @param <T> The type of the class.
 *
 * @since 0.4.12
 *
 * @author Guus Lieben
 */
public record ExternalClass<T>(TypeView<T> type) implements ClassReference {

    @Override
    public Object call(Token at, Interpreter interpreter, InstanceReference instance, List<Object> arguments) throws ApplicationException {
        if (instance != null) {
            throw new ScriptEvaluationError("Cannot call a class with an instance", Phase.INTERPRETING, at);
        }
        ConstructorView<T> executable = ExecutableLookup.executable(this.type.constructors().all(), arguments);
        if (executable != null) {
            try {
                T objectInstance = executable.create(arguments.toArray());
                return new ExternalInstance(objectInstance,
                        interpreter.applicationContext().environment().introspector().introspect(objectInstance));
            }
            catch (ApplicationException e) {
                throw e;
            }
            catch (Throwable throwable) {
                throw new ApplicationException(throwable);
            }
        }
        throw new ScriptEvaluationError("No constructor found for class " + this.type.name() + " with arguments " + arguments, Phase.INTERPRETING, at);
    }

    @Override
    public String toString() {
        return ObjectDescriber.of(this)
                .field("type", this.type.qualifiedName())
                .describe();
    }

    @Override
    public VirtualFunction constructor() {
        return null;
    }

    @Override
    public MethodReference method(String name) {
        return new ExternalFunction(this.type(), name);
    }

    @Override
    public ClassReference superClass() {
        TypeView<?> parent = this.type().superClass();
        if (parent.isVoid()) {
            return null;
        }
        return new ExternalClass<>(parent);
    }

    @Override
    public String name() {
        // TODO #1000: Return alias if imported with non-original name
        return this.type().name();
    }

    @Override
    public boolean isFinal() {
        return this.type().modifiers().isFinal();
    }

    @Override
    public void makeFinal() {
        throw new UnsupportedOperationException("Cannot change modifiers of external class");
    }
}
