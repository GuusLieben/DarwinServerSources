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

package org.dockbox.hartshorn.hsl.ast.expression;

import org.dockbox.hartshorn.hsl.ast.NamedNode;
import org.dockbox.hartshorn.hsl.token.Token;
import org.dockbox.hartshorn.hsl.visitors.ExpressionVisitor;

public class GetExpression extends Expression implements NamedNode {

    private final Token name;
    private final Expression object;

    public GetExpression(final Token name, final Expression object) {
        super(name);
        this.name = name;
        this.object = object;
    }

    @Override
    public Token name() {
        return this.name;
    }

    public Expression object() {
        return this.object;
    }

    @Override
    public <R> R accept(final ExpressionVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
