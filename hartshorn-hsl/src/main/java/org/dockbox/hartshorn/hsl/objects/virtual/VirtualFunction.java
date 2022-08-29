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

package org.dockbox.hartshorn.hsl.objects.virtual;

import org.dockbox.hartshorn.hsl.ast.statement.ParametricExecutableStatement;
import org.dockbox.hartshorn.hsl.ast.statement.ParametricExecutableStatement.Parameter;
import org.dockbox.hartshorn.hsl.interpreter.Interpreter;
import org.dockbox.hartshorn.hsl.interpreter.VariableScope;
import org.dockbox.hartshorn.hsl.objects.AbstractFinalizable;
import org.dockbox.hartshorn.hsl.objects.InstanceReference;
import org.dockbox.hartshorn.hsl.objects.MethodReference;
import org.dockbox.hartshorn.hsl.runtime.DiagnosticMessage;
import org.dockbox.hartshorn.hsl.runtime.Return;
import org.dockbox.hartshorn.hsl.runtime.RuntimeError;
import org.dockbox.hartshorn.hsl.token.Token;
import org.dockbox.hartshorn.hsl.token.TokenType;

import java.util.List;

/**
 * Represents a function definition inside a script. The function is identified by its name, and
 * parameters. The function can carry a variety of additional information such as the body, and
 * its body.
 *
 * @author Guus Lieben
 * @since 22.4
 */
public class VirtualFunction extends AbstractFinalizable implements MethodReference {
    
    private final ParametricExecutableStatement declaration;
    private final VariableScope closure;
    private final InstanceReference instance;
    private final boolean isInitializer;

    public VirtualFunction(final ParametricExecutableStatement declaration, final VariableScope closure, final boolean isInitializer) {
        this(declaration, closure, null, isInitializer);
    }

    public VirtualFunction(final ParametricExecutableStatement declaration, final VariableScope closure, final InstanceReference instance, final boolean isInitializer) {
        super(declaration.isFinal());
        this.declaration = declaration;
        this.closure = closure;
        this.instance = instance;
        this.isInitializer = isInitializer;
    }

    public ParametricExecutableStatement declaration() {
        return this.declaration;
    }

    public VariableScope closure() {
        return this.closure;
    }

    public InstanceReference instance() {
        return this.instance;
    }

    public boolean isInitializer() {
        return this.isInitializer;
    }

    /**
     * Creates a new {@link VirtualFunction} bound to the given instance. This will cause
     * the function to use the given instance when invoking.
     * @param instance The instance to bind to.
     * @return A new {@link VirtualFunction} bound to the given instance.
     */
    @Override
    public VirtualFunction bind(final InstanceReference instance) {
        final VariableScope variableScope = new VariableScope(this.closure);
        variableScope.define(TokenType.THIS.representation(), instance);
        return new VirtualFunction(this.declaration, variableScope, this.isInitializer);
    }

    @Override
    public Object call(final Token at, final Interpreter interpreter, final InstanceReference instance, final List<Object> arguments) {
        final VariableScope variableScope = new VariableScope(this.closure);
        final List<Parameter> parameters = this.declaration.parameters();
        if (parameters.size() != arguments.size()) {
            throw new RuntimeError(at, DiagnosticMessage.EXPECTED_N_X_BUT_GOT_Y,
                    parameters.size(),
                    (parameters.size() == 1 ? "argument" : "arguments"),
                    arguments.size());
        }
        for (int i = 0; i < parameters.size(); i++) {
            variableScope.define(parameters.get(i).name().lexeme(), arguments.get(i));
        }
        try {
            interpreter.execute(this.declaration.statements(), variableScope);
        }
        catch (final Return returnValue) {
            if (this.isInitializer) return this.closure.getAt(at, 0, TokenType.THIS.representation());
            return returnValue.value();
        }
        if (this.isInitializer) return this.closure.getAt(at, 0, TokenType.THIS.representation());
        return null;
    }

    @Override
    public InstanceReference bound() {
        return this.instance;
    }
}