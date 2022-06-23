package org.dockbox.hartshorn.hsl.ast.expression;

import org.dockbox.hartshorn.hsl.visitors.ExpressionVisitor;

public abstract class Expression {
    public abstract <R> R accept(ExpressionVisitor<R> visitor);
}