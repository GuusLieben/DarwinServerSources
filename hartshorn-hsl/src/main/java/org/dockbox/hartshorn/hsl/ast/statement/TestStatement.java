package org.dockbox.hartshorn.hsl.ast.statement;

import org.dockbox.hartshorn.hsl.token.Token;
import org.dockbox.hartshorn.hsl.visitors.StatementVisitor;

import java.util.List;

public class TestStatement extends Statement {

    private final Token name;
    private final List<Statement> body;
    private final Statement returnValue;

    public TestStatement(final Token name, final List<Statement> body, final Statement returnValue) {
        this.name = name;
        this.body = body;
        this.returnValue = returnValue;
    }

    public Token name() {
        return this.name;
    }

    public List<Statement> body() {
        return this.body;
    }

    public Statement returnValue() {
        return this.returnValue;
    }

    @Override
    public <R> R accept(final StatementVisitor<R> visitor) {
        return visitor.visit(this);
    }
}