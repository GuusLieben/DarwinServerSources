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

package org.dockbox.hartshorn.hsl.extension;

import org.dockbox.hartshorn.hsl.ast.ASTNode;
import org.dockbox.hartshorn.hsl.ast.expression.Expression;
import org.dockbox.hartshorn.hsl.visitors.ExpressionVisitor;

/**
 * Base class for custom expressions. This class is non-sealed to allow for custom implementations
 * for various use cases. The {@link ExpressionModule} is used to provide the custom expression
 * with the appropriate token type, parser, interpreter, and resolver.
 *
 * @param <T> The type of the custom expression.
 *
 * @since 0.6.0
 *
 * @author Guus Lieben
 */
public abstract non-sealed class CustomExpression<T extends CustomExpression<T>> extends Expression implements CustomASTNode<T, Object> {

    private final ExpressionModule<T> module;

    protected CustomExpression(ASTNode at, ExpressionModule<T> module) {
        super(at);
        this.module = module;
    }

    @Override
    public ExpressionModule<T> module() {
        return this.module;
    }

    @Override
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return this.module().accept(visitor);
    }
}
