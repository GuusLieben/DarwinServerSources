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

package org.dockbox.hartshorn.hsl.parser.statement;

import org.dockbox.hartshorn.hsl.ast.expression.Expression;
import org.dockbox.hartshorn.hsl.ast.statement.FieldStatement;
import org.dockbox.hartshorn.hsl.ast.statement.VariableStatement;
import org.dockbox.hartshorn.hsl.parser.ASTNodeParser;
import org.dockbox.hartshorn.hsl.parser.TokenParser;
import org.dockbox.hartshorn.hsl.parser.TokenStepValidator;
import org.dockbox.hartshorn.hsl.token.Token;
import org.dockbox.hartshorn.hsl.token.TokenType;
import org.dockbox.hartshorn.util.option.Option;

import java.util.Set;

public class FieldStatementParser implements ASTNodeParser<FieldStatement> {

    @Override
    public Option<FieldStatement> parse(final TokenParser parser, final TokenStepValidator validator) {
        final Token modifier = parser.find(TokenType.PUBLIC, TokenType.PRIVATE);
        final boolean isFinal = parser.match(TokenType.FINAL);
        final Token name = validator.expect(TokenType.IDENTIFIER, "variable name");

        Expression initializer = null;
        if(parser.match(TokenType.EQUAL)) {
            initializer = parser.expression();
        }

        validator.expectAfter(TokenType.SEMICOLON, "variable declaration");
        final VariableStatement variable = new VariableStatement(name, initializer);

        return Option.of(new FieldStatement(modifier, variable.name(), variable.initializer(), isFinal));
    }

    @Override
    public Set<Class<? extends FieldStatement>> types() {
        // Only for direct used, should not be used for dynamic parsing
        return Set.of();
    }
}
