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

package org.dockbox.hartshorn.hsl.modules;

import org.dockbox.hartshorn.hsl.ast.statement.NativeFunctionStatement;
import org.dockbox.hartshorn.hsl.ast.statement.ParametricExecutableStatement.Parameter;
import org.dockbox.hartshorn.hsl.interpreter.Interpreter;
import org.dockbox.hartshorn.hsl.objects.NativeExecutionException;
import org.dockbox.hartshorn.hsl.objects.external.ExecutableLookup;
import org.dockbox.hartshorn.hsl.objects.external.ExternalInstance;
import org.dockbox.hartshorn.hsl.runtime.DiagnosticMessage;
import org.dockbox.hartshorn.hsl.runtime.RuntimeError;
import org.dockbox.hartshorn.hsl.token.Token;
import org.dockbox.hartshorn.hsl.token.TokenType;
import org.dockbox.hartshorn.util.reflect.MethodContext;
import org.dockbox.hartshorn.util.reflect.ParameterContext;
import org.dockbox.hartshorn.util.reflect.TypeContext;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents one or more Java methods that can be called from an HSL runtime. The methods
 * are pre-filtered based on their access-level. The methods are provided as {@link NativeFunctionStatement}s.
 *
 * <p>Methods can be executed based on the supported functions of the module. If the method
 * is not known at compile-time, it is looked up at run-time. If the method is not found, is
 * not accessible, or if it is not supported by the module, a {@link RuntimeError} is thrown.
 *
 * <p>If the method is not accessible, or any cannot be invoked, a {@link NativeExecutionException}
 * is thrown. For all other errors, a {@link RuntimeError} is thrown.
 *
 * <p>All execution calls are performed on the instance provided by {@link #instance()}. If the instance
 * is {@code null}, the method must be static.
 *
 * @author Guus Lieben
 * @since 22.4
 */
public abstract class AbstractNativeModule implements NativeModule {

    private List<NativeFunctionStatement> supportedFunctions;

    /**
     * Gets the type of the module class that is being represented.
     * @return The type of the module class.
     */
    protected abstract Class<?> moduleClass();

    /**
     * Gets or creates the instance of the module class. This instance is used to invoke the methods.
     * @return The instance of the module class.
     */
    protected abstract Object instance();

    @Override
    public Object call(final Token at, final Interpreter interpreter, final NativeFunctionStatement function, final List<Object> arguments) throws NativeExecutionException {
        try {

            final TypeContext<Object> type = TypeContext.of((Class<Object>) this.moduleClass());
            final MethodContext<?, Object> method;
            if (function.method() == null) {
                final String functionName = function.name().lexeme();
                if (arguments.isEmpty()) {
                    method = type.method(functionName).rethrow().get();
                }
                else {
                    method = ExecutableLookup.method(at, type, functionName, arguments);
                }
            }
            else {
                method = (MethodContext<?, Object>) function.method();
            }
            if (this.supportedFunctions.stream().anyMatch(sf -> function.method().equals(method))) {
                final Object result = method.invoke(this.instance(), arguments.toArray(new Object[0]))
                        .rethrowUnchecked()
                        .orNull();
                return new ExternalInstance(result);
            } else throw new RuntimeError(at, DiagnosticMessage.UNSUPPORTED_NATIVE_FUNCTION, function.name().lexeme(), this.moduleClass().getSimpleName());
        }
        catch (final InvocationTargetException e) {
            throw new NativeExecutionException("Invalid Module Loader", e);
        }
        catch (final NoSuchMethodException e) {
            throw new NativeExecutionException("Module Loader : Can't find function with name : " + function, e);
        }
        catch (final IllegalAccessException e) {
            throw new NativeExecutionException("Module Loader : Can't access function with name : " + function, e);
        }
        catch (final Throwable e) {
            throw new RuntimeError(at, e);
        }
    }

    @Override
    public List<NativeFunctionStatement> supportedFunctions(final Token moduleName) {
        if (this.supportedFunctions == null) {
            final List<NativeFunctionStatement> functionStatements = new ArrayList<>();

            for (final MethodContext<?, ?> method : TypeContext.of(this.moduleClass()).methods()) {
                if (!method.isPublic()) continue;
                final Token token = new Token(TokenType.IDENTIFIER, method.name(), -1, -1);

                final List<Parameter> parameters = new ArrayList<>();
                for (final ParameterContext<?> parameter : method.parameters()) {
                    final Token parameterName = new Token(TokenType.IDENTIFIER, parameter.name(), -1, -1);
                    parameters.add(new Parameter(parameterName));
                }
                final NativeFunctionStatement functionStatement = new NativeFunctionStatement(token, moduleName, method, parameters);
                functionStatements.add(functionStatement);
            }
            this.supportedFunctions = functionStatements;
        }
        return this.supportedFunctions;
    }
}