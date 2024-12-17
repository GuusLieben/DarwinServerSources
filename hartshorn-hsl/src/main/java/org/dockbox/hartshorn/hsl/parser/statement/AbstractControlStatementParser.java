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

package org.dockbox.hartshorn.hsl.parser.statement;

import org.dockbox.hartshorn.hsl.ScriptEvaluationError;
import org.dockbox.hartshorn.hsl.ast.statement.Statement;
import org.dockbox.hartshorn.hsl.parser.ASTNodeParser;
import org.dockbox.hartshorn.hsl.parser.TokenParser;
import org.dockbox.hartshorn.hsl.parser.TokenStepValidator;
import org.dockbox.hartshorn.hsl.token.Token;
import org.dockbox.hartshorn.hsl.token.type.TokenType;
import org.dockbox.hartshorn.util.option.Option;

/**
 * Represents a parser for a control statement. This is a statement that controls the flow of the
 * script through a single keyword without consequent tokens, such as 'break' or 'continue'.
 *
 * @param <T> the type of statement that is parsed by this parser
 *
 * @since 0.6.0
 *
 * @author Guus Lieben
 */
public abstract class AbstractControlStatementParser<T extends Statement> implements ASTNodeParser<T> {

    /**
     * The keyword that represents the control statement.
     *
     * @return the keyword that represents the control statement
     */
    protected abstract TokenType keyword();

    /**
     * Creates a new instance of the control statement that is parsed by this parser.
     *
     * @param keyword the keyword that represents the control statement
     * @return a new instance of the control statement that is parsed by this parser
     */
    protected abstract T create(Token keyword);

    @Override
    public Option<? extends T> parse(TokenParser parser, TokenStepValidator validator) throws ScriptEvaluationError {
        TokenType tokenType = this.keyword();
        if (parser.match(tokenType)) {
            Token keyword = parser.previous();
            validator.expectAfter(parser.tokenRegistry().statementEnd(), tokenType);
            return Option.of(this.create(keyword));
        }
        return Option.empty();
    }
}
