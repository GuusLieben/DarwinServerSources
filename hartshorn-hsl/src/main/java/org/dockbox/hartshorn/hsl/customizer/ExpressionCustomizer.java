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

package org.dockbox.hartshorn.hsl.customizer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.dockbox.hartshorn.hsl.ast.statement.BlockStatement;
import org.dockbox.hartshorn.hsl.ast.statement.ExpressionStatement;
import org.dockbox.hartshorn.hsl.ast.statement.ModuleStatement;
import org.dockbox.hartshorn.hsl.ast.statement.ReturnStatement;
import org.dockbox.hartshorn.hsl.ast.statement.Statement;
import org.dockbox.hartshorn.hsl.ast.statement.TestStatement;
import org.dockbox.hartshorn.hsl.callable.module.NativeModule;
import org.dockbox.hartshorn.hsl.runtime.Phase;
import org.dockbox.hartshorn.hsl.token.Token;
import org.dockbox.hartshorn.hsl.token.TokenType;

/**
 * Customizer to simplify the validation of standalone expressions. This customizer is used by the
 * {@link org.dockbox.hartshorn.hsl.runtime.ValidateExpressionRuntime} to wrap the script body in
 * a {@link TestStatement} and inline any required {@link NativeModule}s without the need for a
 * standard library or a module header.
 *
 * @author Guus Lieben
 * @since 22.4
 */
public class ExpressionCustomizer extends AbstractCodeCustomizer {

    public static final String VALIDATION_ID = "$__validation__$";

    public ExpressionCustomizer() {
        super(Phase.RESOLVING);
    }

    @Override
    public void call(final ScriptContext context) {
        final List<Statement> statements = context.statements();
        this.verifyIsExpression(statements);
        final List<Statement> testStatements = this.enhanceTestStatement(statements);
        final List<Statement> enhancedStatements = this.enhanceModuleStatements(testStatements, context.interpreter().externalModules());
        context.statements(enhancedStatements);
    }

    private void verifyIsExpression(final List<Statement> statements) {
        if (statements.size() != 1) {
            throw new IllegalArgumentException("Expected only one statement, but found " + statements.size());
        }

        final Statement statement = statements.get(0);
        if (!(statement instanceof ExpressionStatement)) {
            throw new IllegalArgumentException("Expected statement to be a valid expression, but found " + statement);
        }
    }

    private List<Statement> enhanceTestStatement(final List<Statement> statements) {
        final ExpressionStatement statement = (ExpressionStatement) statements.get(0);

        final Token returnToken = new Token(TokenType.RETURN, VALIDATION_ID, -1);
        final ReturnStatement returnStatement = new ReturnStatement(returnToken, statement.expression());

        final Token testToken = new Token(TokenType.STRING, VALIDATION_ID, VALIDATION_ID, -1);
        final BlockStatement blockStatement = new BlockStatement(returnToken, statements);
        final TestStatement testStatement = new TestStatement(testToken, blockStatement, returnStatement);

        final List<Statement> validationStatements = new ArrayList<>();
        validationStatements.add(testStatement);

        return validationStatements;
    }

    private List<Statement> enhanceModuleStatements(final List<Statement> statements, final Map<String, NativeModule> modules) {
        for (final String module : modules.keySet()) {
            final Token moduleToken = new Token(TokenType.IDENTIFIER, module, -1);
            final ModuleStatement moduleStatement = new ModuleStatement(moduleToken);
            statements.add(0, moduleStatement);
        }
        return statements;
    }
}
