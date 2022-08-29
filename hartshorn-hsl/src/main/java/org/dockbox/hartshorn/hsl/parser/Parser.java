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

package org.dockbox.hartshorn.hsl.parser;

import org.dockbox.hartshorn.hsl.ScriptEvaluationError;
import org.dockbox.hartshorn.hsl.ast.expression.ArrayComprehensionExpression;
import org.dockbox.hartshorn.hsl.ast.expression.ArrayGetExpression;
import org.dockbox.hartshorn.hsl.ast.expression.ArrayLiteralExpression;
import org.dockbox.hartshorn.hsl.ast.expression.ArraySetExpression;
import org.dockbox.hartshorn.hsl.ast.expression.AssignExpression;
import org.dockbox.hartshorn.hsl.ast.expression.BinaryExpression;
import org.dockbox.hartshorn.hsl.ast.expression.BitwiseExpression;
import org.dockbox.hartshorn.hsl.ast.expression.ElvisExpression;
import org.dockbox.hartshorn.hsl.ast.expression.Expression;
import org.dockbox.hartshorn.hsl.ast.expression.FunctionCallExpression;
import org.dockbox.hartshorn.hsl.ast.expression.GetExpression;
import org.dockbox.hartshorn.hsl.ast.expression.GroupingExpression;
import org.dockbox.hartshorn.hsl.ast.expression.InfixExpression;
import org.dockbox.hartshorn.hsl.ast.expression.LiteralExpression;
import org.dockbox.hartshorn.hsl.ast.expression.LogicalAssignExpression;
import org.dockbox.hartshorn.hsl.ast.expression.LogicalExpression;
import org.dockbox.hartshorn.hsl.ast.expression.PostfixExpression;
import org.dockbox.hartshorn.hsl.ast.expression.PrefixExpression;
import org.dockbox.hartshorn.hsl.ast.expression.RangeExpression;
import org.dockbox.hartshorn.hsl.ast.expression.SetExpression;
import org.dockbox.hartshorn.hsl.ast.expression.SuperExpression;
import org.dockbox.hartshorn.hsl.ast.expression.TernaryExpression;
import org.dockbox.hartshorn.hsl.ast.expression.ThisExpression;
import org.dockbox.hartshorn.hsl.ast.expression.UnaryExpression;
import org.dockbox.hartshorn.hsl.ast.expression.VariableExpression;
import org.dockbox.hartshorn.hsl.ast.statement.BlockStatement;
import org.dockbox.hartshorn.hsl.ast.statement.BreakStatement;
import org.dockbox.hartshorn.hsl.ast.statement.ClassStatement;
import org.dockbox.hartshorn.hsl.ast.statement.ConstructorStatement;
import org.dockbox.hartshorn.hsl.ast.statement.ContinueStatement;
import org.dockbox.hartshorn.hsl.ast.statement.DoWhileStatement;
import org.dockbox.hartshorn.hsl.ast.statement.ExpressionStatement;
import org.dockbox.hartshorn.hsl.ast.statement.ExtensionStatement;
import org.dockbox.hartshorn.hsl.ast.statement.FieldGetStatement;
import org.dockbox.hartshorn.hsl.ast.statement.FieldMemberStatement;
import org.dockbox.hartshorn.hsl.ast.statement.FieldSetStatement;
import org.dockbox.hartshorn.hsl.ast.statement.FieldStatement;
import org.dockbox.hartshorn.hsl.ast.statement.FinalizableStatement;
import org.dockbox.hartshorn.hsl.ast.statement.ForEachStatement;
import org.dockbox.hartshorn.hsl.ast.statement.ForStatement;
import org.dockbox.hartshorn.hsl.ast.statement.Function;
import org.dockbox.hartshorn.hsl.ast.statement.FunctionStatement;
import org.dockbox.hartshorn.hsl.ast.statement.IfStatement;
import org.dockbox.hartshorn.hsl.ast.statement.ModuleStatement;
import org.dockbox.hartshorn.hsl.ast.statement.NativeFunctionStatement;
import org.dockbox.hartshorn.hsl.ast.statement.ParametricExecutableStatement.Parameter;
import org.dockbox.hartshorn.hsl.ast.statement.RepeatStatement;
import org.dockbox.hartshorn.hsl.ast.statement.ReturnStatement;
import org.dockbox.hartshorn.hsl.ast.statement.ReturnStatement.ReturnType;
import org.dockbox.hartshorn.hsl.ast.statement.Statement;
import org.dockbox.hartshorn.hsl.ast.statement.SwitchCase;
import org.dockbox.hartshorn.hsl.ast.statement.SwitchStatement;
import org.dockbox.hartshorn.hsl.ast.statement.TestStatement;
import org.dockbox.hartshorn.hsl.ast.statement.VariableStatement;
import org.dockbox.hartshorn.hsl.ast.statement.WhileStatement;
import org.dockbox.hartshorn.hsl.customizer.CodeCustomizer;
import org.dockbox.hartshorn.hsl.runtime.DiagnosticMessage;
import org.dockbox.hartshorn.hsl.runtime.Phase;
import org.dockbox.hartshorn.hsl.token.Token;
import org.dockbox.hartshorn.hsl.token.TokenType;
import org.dockbox.hartshorn.inject.binding.Bound;
import org.dockbox.hartshorn.util.function.TriFunction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * The parser takes the tokens generated by the {@link org.dockbox.hartshorn.hsl.lexer.Lexer}
 * and transforms logical combinations of {@link Token}s into {@link Statement}s. This produces
 * the first syntax tree.
 *
 * @author Guus Lieben
 * @since 22.4
 */
public class Parser {

    private int current = 0;
    private List<Token> tokens;

    private static final int MAX_NUM_OF_ARGUMENTS = 8;
    private static final TokenType[] ASSIGNMENT_TOKENS = allTokensMatching(t -> t.assignsWith() != null);

    private final Set<String> prefixFunctions = new HashSet<>();
    private final Set<String> infixFunctions = new HashSet<>();

    @Bound
    public Parser(final List<Token> tokens) {
        this.tokens = tokens;
    }

    private static TokenType[] allTokensMatching(final Predicate<TokenType> predicate) {
        return Arrays.stream(TokenType.values())
                .filter(predicate)
                .toArray(TokenType[]::new);
    }

    /**
     * Gets all tokens which have been configured for this parser instance. This
     * allows {@link CodeCustomizer}s to modify the collected tokens before parsing
     * is performed.
     *
     * @return The configured tokens.
     */
    public List<Token> tokens() {
        return this.tokens;
    }

    /**
     * Sets the collection of tokens to parse. This can be used by {@link CodeCustomizer}s
     * to modify the tokens before parsing is performed.
     *
     * @param tokens The tokens to parse.
     */
    public void tokens(final List<Token> tokens) {
        this.tokens = tokens;
    }

    /**
     * Parses the configured tokens and transforms them into logical {@link Statement}s.
     *
     * @return The parsed {@link Statement}s.
     */
    public List<Statement> parse() {
        final List<Statement> statements = new ArrayList<>();
        while (!this.isAtEnd()) {
            statements.add(this.declaration());
        }
        return statements;
    }

    private Statement statement() {
        if (this.check(TokenType.LEFT_BRACE)) return this.block();

        if (this.match(TokenType.IF)) return this.ifStatement();
        if (this.match(TokenType.DO)) return this.doWhileStatement();
        if (this.match(TokenType.FOR)) return this.forStatement();
        if (this.match(TokenType.TEST)) return this.testStatement();
        if (this.match(TokenType.WHILE)) return this.whileStatement();
        if (this.match(TokenType.BREAK)) return this.breakStatement();
        if (this.match(TokenType.USING)) return this.moduleStatement();
        if (this.match(TokenType.REPEAT)) return this.repeatStatement();
        if (this.match(TokenType.SWITCH)) return this.switchStatement();
        if (this.match(TokenType.CONTINUE)) return this.continueStatement();
        if (this.match(TokenType.RETURN, TokenType.YIELD)) return this.returnStatement();

        final TokenType type = this.peek().type();
        if (type.standaloneStatement()) {
            throw new ScriptEvaluationError(Phase.PARSING, this.peek(), DiagnosticMessage.UNSUPPORTED_STANDALONE_STATEMENT, type);
        }

        return this.expressionStatement();
    }

    private Statement moduleStatement() {
        final Token name = this.expect(TokenType.IDENTIFIER, "module name");
        this.expectAfter(TokenType.SEMICOLON, TokenType.USING);
        return new ModuleStatement(name);
    }

    private Statement declaration() {
        if (this.match(TokenType.PREFIX, TokenType.INFIX) && this.match(TokenType.FUN)) return this.funcDeclaration(this.tokens.get(this.current - 2));
        if (this.match(TokenType.FUN)) return this.funcDeclaration(this.previous());
        if (this.match(TokenType.VAR)) return this.varDeclaration();
        if (this.match(TokenType.CLASS)) return this.classDeclaration();
        if (this.match(TokenType.NATIVE)) return this.nativeFuncDeclaration();
        if (this.match(TokenType.FINAL)) return this.finalDeclaration();
        return this.statement();
    }

    private ClassStatement classDeclaration() {
        final Token name = this.expect(TokenType.IDENTIFIER, "class name");

        final boolean isDynamic = this.match(TokenType.QUESTION_MARK);

        VariableExpression superClass = null;
        if (this.match(TokenType.EXTENDS)) {
            this.expect(TokenType.IDENTIFIER, "super class name");
            superClass = new VariableExpression(this.previous());
        }

        this.expectBefore(TokenType.LEFT_BRACE, "class body");

        final List<FunctionStatement> methods = new ArrayList<>();
        final List<FieldStatement> fields = new ArrayList<>();
        ConstructorStatement constructor = null;
        while (!this.check(TokenType.RIGHT_BRACE) && !this.isAtEnd()) {
            final Statement declaration = this.classBodyStatement(name);
            if (declaration instanceof ConstructorStatement constructorStatement) {
                constructor = constructorStatement;
            } else if (declaration instanceof FunctionStatement function){
                methods.add(function);
            } else if (declaration instanceof FieldStatement field) {
                fields.add(field);
            }
            else {
                throw new ScriptEvaluationError(Phase.PARSING, this.peek(), DiagnosticMessage.UNSUPPORTED_BODY_STATEMENT, declaration.getClass().getSimpleName());
            }
        }

        this.expectAfter(TokenType.RIGHT_BRACE, "class body");

        return new ClassStatement(name, superClass, constructor, methods, fields, isDynamic);
    }

    private Statement classBodyStatement(final Token className) {
        if (this.match(TokenType.CONSTRUCTOR)) {
            return this.constructorDeclaration(className);
        }

        if (this.check(TokenType.FUN)) {
            return this.function(TokenType.FUN, TokenType.LEFT_BRACE, (name, parameters) -> {
                final Token start = this.peek();
                final List<Statement> statements = this.consumeStatements();
                final BlockStatement body = new BlockStatement(start, statements);
                return new FunctionStatement(name, parameters, body);
            });
        } else {
            return this.fieldDeclaration();
        }
    }

    private ConstructorStatement constructorDeclaration(final Token className) {
        final Token keyword = this.peek();
        final List<Parameter> parameters = this.functionParameters("constructor", MAX_NUM_OF_ARGUMENTS, keyword);
        final BlockStatement body = this.block();
        return new ConstructorStatement(keyword, className, parameters, body);
    }

    private Function funcDeclaration(final Token token) {
        final Token name = this.expect(TokenType.IDENTIFIER, "function name");

        int expectedNumberOrArguments = MAX_NUM_OF_ARGUMENTS;
        if (token.type() == TokenType.PREFIX) {
            this.prefixFunctions.add(name.lexeme());
            expectedNumberOrArguments = 1;
        }
        else if (token.type() == TokenType.INFIX) {
            this.infixFunctions.add(name.lexeme());
            expectedNumberOrArguments = 2;
        }

        Token extensionName = null;

        if (this.peek().type() == TokenType.COLON) {
            this.expectAfter(TokenType.COLON, "class name");
            extensionName = this.expect(TokenType.IDENTIFIER, "extension name");
        }

        final List<Parameter> parameters = this.functionParameters("function name", expectedNumberOrArguments, token);
        final BlockStatement body = this.block();

        if (extensionName != null) {
            final FunctionStatement function = new FunctionStatement(extensionName, parameters, body);
            return new ExtensionStatement(name, function);
        }
        else {
            return new FunctionStatement(name, parameters, body);
        }
    }

    private List<Parameter> functionParameters(final String function_name, final int expectedNumberOrArguments, final Token token) {
        this.expectAfter(TokenType.LEFT_PAREN, function_name);
        final List<Parameter> parameters = new ArrayList<>();
        if (!this.check(TokenType.RIGHT_PAREN)) {
            do {
                if (parameters.size() >= expectedNumberOrArguments) {
                    if (token == null) throw new ScriptEvaluationError(Phase.PARSING, this.peek(), DiagnosticMessage.TOO_MANY_PARAMETERS, expectedNumberOrArguments);
                    else throw new ScriptEvaluationError(Phase.PARSING, token, DiagnosticMessage.TOO_MANY_PARAMETERS_FOR_X, expectedNumberOrArguments, token.type());
                }
                final Token parameterName = this.expect(TokenType.IDENTIFIER, "parameter name");
                parameters.add(new Parameter(parameterName));
            }
            while (this.match(TokenType.COMMA));
        }

        this.expectAfter(TokenType.RIGHT_PAREN, "parameters");
        return parameters;
    }

    private NativeFunctionStatement nativeFuncDeclaration() {
        this.expect(TokenType.FUN);
        final Token moduleName = this.expect(TokenType.IDENTIFIER, "module name");

        while (this.match(TokenType.COLON)) {
            final Token token = new Token(TokenType.DOT, ".", moduleName.line(), moduleName.column());
            moduleName.concat(token);
            final Token moduleName2 = this.expect(TokenType.IDENTIFIER, "module name");
            moduleName.concat(moduleName2);
        }

        return this.function(TokenType.DOT, TokenType.SEMICOLON, (name, parameters) ->
                new NativeFunctionStatement(name, moduleName, null, parameters)
        );
    }

    private <T extends Statement> T function(final TokenType keyword, final TokenType end, final BiFunction<Token, List<Parameter>, T> converter) {
        this.expectBefore(keyword, "method body");
        final Token funcName = this.expect(TokenType.IDENTIFIER, "function name");
        final List<Parameter> parameters = this.functionParameters("method name", MAX_NUM_OF_ARGUMENTS, null);
        this.expectAfter(end, "value");
        return converter.apply(funcName, parameters);
    }

    private FieldStatement fieldDeclaration() {
        final Token modifier = this.find(TokenType.PRIVATE, TokenType.PUBLIC);
        final boolean isFinal = this.match(TokenType.FINAL);
        final VariableStatement variable = this.varDeclaration();

        final FieldStatement fieldStatement = new FieldStatement(modifier, variable.name(), variable.initializer(), isFinal);

        if (this.match(TokenType.LEFT_BRACE)) {
            this.fieldMemberStatement(fieldStatement); // Get or set
            this.fieldMemberStatement(fieldStatement); // Get or set
            this.expectAfter(TokenType.RIGHT_BRACE, "field members");
        }

        return fieldStatement;
    }

    private FieldMemberStatement fieldMemberStatement(final FieldStatement fieldStatement) {
        final Token modifier = this.find(TokenType.PRIVATE, TokenType.PUBLIC);
        final Token member = this.find(TokenType.GET, TokenType.SET);
        if (member == null) {
            if (modifier != null) this.current -= 1;
            return null;
        }
        return switch (member.type()) {
            case GET -> {
                final FieldGetStatement statement = this.fieldGetStatement(modifier, member, fieldStatement);
                fieldStatement.withGetter(statement);
                yield statement;
            }
            case SET -> {
                final FieldSetStatement statement = this.fieldSetStatement(modifier, member, fieldStatement);
                fieldStatement.withSetter(statement);
                yield statement;
            }
            default -> throw new ScriptEvaluationError(Phase.PARSING, member, DiagnosticMessage.UNSUPPORTED_FIELD_MEMBER, member.type());
        };
    }

    private FieldGetStatement fieldGetStatement(final Token modifier, final Token get, final FieldStatement field) {
        final List<Statement> body;
        if (this.match(TokenType.LEFT_PAREN)) {
            this.expectAfter(TokenType.RIGHT_PAREN, "field get declaration");
            body = this.fieldMemberBody();
        }
        else {
            this.expectAfter(TokenType.SEMICOLON, "field get declaration");
            body = null;
        }
        return new FieldGetStatement(modifier, get, field, body);
    }

    private FieldSetStatement fieldSetStatement(final Token modifier, final Token set, final FieldStatement field) {
        if (this.match(TokenType.LEFT_PAREN)) {
            final Token parameterName = this.expect(TokenType.IDENTIFIER, "parameter name");
            final Parameter parameter = new Parameter(parameterName);
            this.expectAfter(TokenType.RIGHT_PAREN, "field set declaration");

            final List<Statement> body = this.fieldMemberBody();
            return new FieldSetStatement(modifier, set, field, body, parameter);
        }
        else{
            this.expectAfter(TokenType.SEMICOLON, "field set declaration");
            return new FieldSetStatement(modifier, set, field, null, null);
        }
    }

    private List<Statement> fieldMemberBody() {
        final Statement statement = this.blockOrExpression(TokenType.LEFT_BRACE, TokenType.RIGHT_BRACE);
        if (statement instanceof BlockStatement block) {
            this.match(TokenType.RIGHT_BRACE); // blockOrExpression doesn't consume closing tokens
            return block.statements();
        }
        else if (statement instanceof ExpressionStatement expressionStatement) {
            return List.of(new ReturnStatement(new Token(TokenType.YIELD, TokenType.YIELD.representation(), -1, -1), expressionStatement.expression(), ReturnType.YIELD));
        }
        else {
            throw new ScriptEvaluationError(Phase.PARSING, statement, DiagnosticMessage.UNSUPPORTED_FIELD_MEMBER_BODY, statement.getClass().getSimpleName());
        }
    }

    private VariableStatement varDeclaration() {
        final Token name = this.expect(TokenType.IDENTIFIER, "variable name");

        Expression initializer = null;
        if (this.match(TokenType.EQUAL)) {
            initializer = this.expression();
        }

        this.expectAfter(TokenType.SEMICOLON, "variable declaration");
        return new VariableStatement(name, initializer);
    }

    private Statement finalDeclaration() {
        final FinalizableStatement finalizable;

        if (this.match(TokenType.PREFIX, TokenType.INFIX) && this.match(TokenType.FUN)) finalizable = this.funcDeclaration(this.tokens.get(this.current - 2));
        else if (this.match(TokenType.FUN)) finalizable = this.funcDeclaration(this.previous());
        else if (this.match(TokenType.VAR)) finalizable = this.varDeclaration();
        else if (this.match(TokenType.CLASS)) finalizable = this.classDeclaration();
        else if (this.match(TokenType.NATIVE)) finalizable = this.nativeFuncDeclaration();
        else throw new ScriptEvaluationError(Phase.PARSING, this.peek(), DiagnosticMessage.ILLEGAL_USE_OF_X, TokenType.FINAL.representation(), this.peek().type());

        finalizable.makeFinal();
        return finalizable;
    }

    private Statement breakStatement() {
        final Token keyword = this.previous();
        this.expectAfter(TokenType.SEMICOLON, "value");
        return new BreakStatement(keyword);
    }

    private Statement continueStatement() {
        final Token keyword = this.previous();
        this.expectAfter(TokenType.SEMICOLON, "value");
        return new ContinueStatement(keyword);
    }

    private Statement ifStatement() {
        this.expectAfter(TokenType.LEFT_PAREN, TokenType.IF);
        final Expression condition = this.expression();
        this.expectAfter(TokenType.RIGHT_PAREN, "if condition");
        final BlockStatement thenBlock = this.block();
        BlockStatement elseBlock = null;
        if (this.match(TokenType.ELSE)) {
            elseBlock = this.block();
        }
        return new IfStatement(condition, thenBlock, elseBlock);
    }

    private Statement whileStatement() {
        this.expectAfter(TokenType.LEFT_PAREN, TokenType.WHILE);
        final Expression condition = this.expression();
        this.expectAfter(TokenType.RIGHT_PAREN, "while condition");
        final BlockStatement loopBody = this.block();
        return new WhileStatement(condition, loopBody);
    }

    private Statement forStatement() {
        this.expectAfter(TokenType.LEFT_PAREN, TokenType.FOR);

        this.expect(TokenType.VAR);
        final VariableStatement initializer = this.varDeclaration();

        if (this.match(TokenType.IN)) {
            final Expression collection = this.expression();
            this.expectAfter(TokenType.RIGHT_PAREN, "for collection");

            final BlockStatement loopBody = this.block();
            return new ForEachStatement(initializer, collection, loopBody);

        } else {
            this.expectAfter(TokenType.SEMICOLON, "for assignment");

            final Expression condition = this.expression();
            this.expectAfter(TokenType.SEMICOLON, "for condition");

            final Statement increment = this.expressionStatement();
            this.expectAfter(TokenType.RIGHT_PAREN, "for increment");

            final BlockStatement loopBody = this.block();
            return new ForStatement(initializer, condition, increment, loopBody);
        }
    }

    private Statement doWhileStatement() {
        final BlockStatement loopBody = this.block();
        this.expect(TokenType.WHILE);
        this.expectAfter(TokenType.LEFT_PAREN, TokenType.WHILE);
        final Expression condition = this.expression();
        this.expectAfter(TokenType.RIGHT_PAREN, "do while condition");
        this.expectAfter(TokenType.SEMICOLON, "do while condition");
        return new DoWhileStatement(condition, loopBody);
    }

    private Statement repeatStatement() {
        this.expectAfter(TokenType.LEFT_PAREN, "repeat");
        final Expression value = this.expression();
        this.expectAfter(TokenType.RIGHT_PAREN, "repeat value");
        final BlockStatement loopBody = this.block();
        return new RepeatStatement(value, loopBody);
    }

    private Statement returnStatement() {
        final Token keyword = this.previous();
        Expression value = null;
        if (!this.check(TokenType.SEMICOLON)) {
            value = this.expression();
        }
        this.expectAfter(TokenType.SEMICOLON, "return value");
        final ReturnType returnType = switch (keyword.type()) {
            case RETURN -> ReturnType.RETURN;
            case YIELD -> ReturnType.YIELD;
            default -> throw new ScriptEvaluationError(Phase.PARSING, keyword, DiagnosticMessage.UNSUPPORTED_RETURN_TYPE, keyword.type());
        };
        return new ReturnStatement(keyword, value, returnType);
    }

    private Statement testStatement() {
        this.expectAfter(TokenType.LEFT_PAREN, "test statement");
        final Token name = this.expect(TokenType.STRING, "test name");
        this.expectAfter(TokenType.RIGHT_PAREN, "test statement name value");
        this.expectBefore(TokenType.LEFT_BRACE, "test body");
        final List<Statement> statements = new ArrayList<>();
        final Token bodyStart = this.peek();
        while (!this.match(TokenType.RIGHT_BRACE) && !this.isAtEnd()) {
            final Statement statement = this.declaration();
            statements.add(statement);
        }
        final BlockStatement body = new BlockStatement(bodyStart, statements);
        return new TestStatement(name, body);
    }

    private ExpressionStatement expressionStatement() {
        final Expression expr = this.expression();
        this.expectAfter(TokenType.SEMICOLON, "expression");
        return new ExpressionStatement(expr);
    }

    private List<Statement> consumeStatements() {
        final List<Statement> statements = new ArrayList<>();
        while (!this.check(TokenType.RIGHT_BRACE) && !this.isAtEnd()) {
            statements.add(this.declaration());
        }
        this.expectAfter(TokenType.RIGHT_BRACE, "block");
        return statements;
    }

    private BlockStatement block() {
        final Token start = this.peek();
        this.expectBefore(TokenType.LEFT_BRACE, "block");

        final List<Statement> statements = this.consumeStatements();
        return new BlockStatement(start, statements);
    }

    private Expression expression() {
        return this.assignment();
    }

    private Expression assignment() {
        final Expression expr = this.elvisExp();

        if (this.match(TokenType.EQUAL)) {
            final Token equals = this.previous();
            final Expression value = this.assignment();

            if (expr instanceof VariableExpression) {
                final Token name = ((VariableExpression) expr).name();
                return new AssignExpression(name, value);
            }
            else if (expr instanceof final ArrayGetExpression arrayGetExpression) {
                final Token name = arrayGetExpression.name();
                return new ArraySetExpression(name, arrayGetExpression.index(), value);
            }
            else if (expr instanceof final GetExpression get) {
                return new SetExpression(get.object(), get.name(), value);
            }
            throw new ScriptEvaluationError(Phase.PARSING, equals, DiagnosticMessage.INVALID_ASSIGNMENT_TARGET, expr.getClass().getSimpleName());
        }
        return expr;
    }

    private Expression elvisExp() {
        final Expression expr = this.ternaryExp();
        if (this.match(TokenType.ELVIS)) {
            final Token elvis = this.previous();
            final Expression rightExp = this.ternaryExp();
            return new ElvisExpression(expr, elvis, rightExp);
        }
        return expr;
    }

    private Expression ternaryExp() {
        final Expression expr = this.bitwise();

        if (this.match(TokenType.QUESTION_MARK)) {
            final Token question = this.previous();
            final Expression firstExp = this.logical();
            final Token colon = this.peek();
            if (this.match(TokenType.COLON)) {
                final Expression secondExp = this.logical();
                return new TernaryExpression(expr, question, firstExp, colon, secondExp);
            }
            throw new ScriptEvaluationError(Phase.PARSING, colon, DiagnosticMessage.EXPECTED_EXPRESSION_AFTER_X, TokenType.COLON.representation());
        }
        return expr;
    }

    private Expression logicalOrBitwise(final Supplier<Expression> next, final TriFunction<Expression, Token, Expression, Expression> step, final TokenType... whileMatching) {
        Expression expression = next.get();
        while(this.match(whileMatching)) {
            final Token operator = this.previous();
            final Expression right = next.get();
            expression = step.accept(expression, operator, right);
        }
        return expression;
    }

    private Expression bitwise() {
        return this.logicalOrBitwise(this::logical, BitwiseExpression::new,
                TokenType.SHIFT_LEFT,
                TokenType.SHIFT_RIGHT,
                TokenType.LOGICAL_SHIFT_RIGHT,
                TokenType.BITWISE_OR,
                TokenType.BITWISE_AND
        );
    }

    private Expression logical() {
        return this.logicalOrBitwise(this::equality, LogicalExpression::new,
                TokenType.OR,
                TokenType.XOR,
                TokenType.AND
        );
    }

    private Expression equality() {
        return this.logicalOrBitwise(this::range, BinaryExpression::new,
                TokenType.BANG_EQUAL,
                TokenType.EQUAL_EQUAL
        );
    }

    private Expression range() {
        return this.logicalOrBitwise(this::logicalAssign, RangeExpression::new, TokenType.RANGE);
    }

    private Expression logicalAssign() {
        return this.logicalOrBitwise(this::parsePrefixFunctionCall, (left, token, right) -> {
                    if (left instanceof VariableExpression variable) {
                        return new LogicalAssignExpression(variable.name(), token, right);
                    }
                    else {
                        throw new ScriptEvaluationError(Phase.PARSING, token, DiagnosticMessage.INVALID_ASSIGNMENT_TARGET, left.getClass().getSimpleName());
                    }
                }, ASSIGNMENT_TOKENS);
    }

    private boolean match(final TokenType... types) {
        return this.find(types) != null;
    }

    private Token find(final TokenType... types) {
        for (final TokenType type : types) {
            if (this.check(type)) {
                final Token token = this.peek();
                this.advance();
                return token;
            }
        }
        return null;
    }

    private boolean check(final TokenType type) {
        if (this.isAtEnd()) return false;
        return this.peek().type() == type;
    }

    private boolean check(final TokenType... type) {
        for (final TokenType t : type) {
            if (this.check(t)) return true;
        }
        return false;
    }

    private Token advance() {
        if (!this.isAtEnd()) this.current++;
        return this.previous();
    }

    private boolean isAtEnd() {
        return this.peek().type() == TokenType.EOF;
    }

    private Token peek() {
        return this.tokens.get(this.current);
    }

    private Token previous() {
        return this.tokens.get(this.current - 1);
    }

    private Expression parsePrefixFunctionCall() {
        if (this.check(TokenType.IDENTIFIER) && this.prefixFunctions.contains(this.peek().lexeme())) {
            this.current++;
            final Token prefixFunctionName = this.previous();
            final Expression right = this.comparison();
            return new PrefixExpression(prefixFunctionName, right);
        }
        return this.comparison();
    }

    private Expression comparison() {
        Expression expr = this.addition();

        while (this.match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) {
            final Token operator = this.previous();
            final Expression right = this.addition();
            expr = new BinaryExpression(expr, operator, right);
        }

        return expr;
    }

    private Expression addition() {
        Expression expr = this.multiplication();
        while (this.match(TokenType.MINUS, TokenType.PLUS)) {
            final Token operator = this.previous();
            final Expression right = this.multiplication();
            expr = new BinaryExpression(expr, operator, right);
        }
        return expr;
    }

    private Expression multiplication() {
        Expression expr = this.parseInfixExpressions();

        while (this.match(TokenType.SLASH, TokenType.STAR, TokenType.MODULO)) {
            final Token operator = this.previous();
            final Expression right = this.parseInfixExpressions();
            expr = new BinaryExpression(expr, operator, right);
        }
        return expr;
    }

    private Expression parseInfixExpressions() {
        Expression expr = this.unary();

        while (this.check(TokenType.IDENTIFIER) && this.infixFunctions.contains(this.peek().lexeme())) {
            this.current++;
            final Token operator = this.previous();
            final Expression right = this.unary();
            expr = new InfixExpression(expr, operator, right);
        }
        return expr;
    }

    private Expression unary() {
        if (this.match(TokenType.BANG, TokenType.MINUS, TokenType.PLUS_PLUS, TokenType.MINUS_MINUS, TokenType.COMPLEMENT)) {
            final Token operator = this.previous();
            final Expression right = this.unary();
            return new UnaryExpression(operator, right);
        }
        return this.call();
    }

    private Expression call() {
        Expression expr = this.primary();
        while (true) {
            if (this.match(TokenType.LEFT_PAREN)) {
                expr = this.finishCall(expr);
            }
            else if (this.match(TokenType.DOT)) {
                final Token name = this.expectAround(TokenType.IDENTIFIER, "property name", "after", "'%s'".formatted(TokenType.DOT.representation()));
                expr = new GetExpression(name, expr);
            }
            else if (this.match(TokenType.PLUS_PLUS, TokenType.MINUS_MINUS)) {
                final Token operator = this.previous();
                expr = new PostfixExpression(operator, expr);
            }
            else {
                break;
            }
        }
        return expr;
    }

    private Expression finishCall(final Expression callee) {
        final List<Expression> arguments = new ArrayList<>();
        final Token parenOpen = this.previous();
        // For zero arguments
        if (!this.check(TokenType.RIGHT_PAREN)) {
            do {
                if (arguments.size() >= MAX_NUM_OF_ARGUMENTS) {
                    throw new ScriptEvaluationError(Phase.PARSING, this.peek(), DiagnosticMessage.TOO_MANY_PARAMETERS, MAX_NUM_OF_ARGUMENTS);
                }
                arguments.add(this.expression());
            }
            while (this.match(TokenType.COMMA));
        }
        final Token parenClose = this.expectAfter(TokenType.RIGHT_PAREN, "arguments");
        return new FunctionCallExpression(callee, parenOpen, parenClose, arguments);
    }

    private Expression primary() {
        if (this.match(TokenType.FALSE)) return new LiteralExpression(this.peek(), false);
        if (this.match(TokenType.TRUE)) return new LiteralExpression(this.peek(), true);
        if (this.match(TokenType.NULL)) return new LiteralExpression(this.peek(), null);
        if (this.match(TokenType.THIS)) return new ThisExpression(this.previous());
        if (this.match(TokenType.NUMBER, TokenType.STRING, TokenType.CHAR)) return new LiteralExpression(this.peek(), this.previous().literal());
        if (this.match(TokenType.IDENTIFIER)) return this.identifierExpression();
        if (this.match(TokenType.LEFT_PAREN)) return this.groupingExpression();
        if (this.match(TokenType.SUPER)) return this.superExpression();
        if (this.match(TokenType.ARRAY_OPEN)) return this.complexArray();

        throw new ScriptEvaluationError(Phase.PARSING, this.peek(), DiagnosticMessage.EXPECTED_EXPRESSION, this.peek());
    }

    private SuperExpression superExpression() {
        final Token keyword = this.previous();
        this.expectAfter(TokenType.DOT, TokenType.SUPER);
        final Token method = this.expect(TokenType.IDENTIFIER, "super class method name");
        return new SuperExpression(keyword, method);
    }

    private GroupingExpression groupingExpression() {
        final Expression expr = this.expression();
        this.expectAfter(TokenType.RIGHT_PAREN, "expression");
        return new GroupingExpression(expr);
    }

    private Expression identifierExpression() {
        final Token next = this.peek();
        if (next.type() == TokenType.ARRAY_OPEN) {
            final Token name = this.previous();
            this.expect(TokenType.ARRAY_OPEN);
            final Expression index = this.expression();
            this.expect(TokenType.ARRAY_CLOSE);
            return new ArrayGetExpression(name, index);
        }
        return new VariableExpression(this.previous());
    }

    private Expression complexArray() {
        final Token open = this.previous();
        final Expression expr = this.expression();

        if (this.match(TokenType.ARRAY_CLOSE)) {
            final List<Expression> elements = new ArrayList<>();
            elements.add(expr);
            return new ArrayLiteralExpression(open, this.previous(), elements);
        }
        else if (this.match(TokenType.COMMA)) return this.arrayLiteralExpression(open, expr);
        else return this.arrayComprehensionExpression(open, expr);
    }

    private ArrayLiteralExpression arrayLiteralExpression(final Token open, final Expression expr) {
        final List<Expression> elements = new ArrayList<>();
        elements.add(expr);
        do {
            elements.add(this.expression());
        }
        while (this.match(TokenType.COMMA));
        final Token close = this.expectAfter(TokenType.ARRAY_CLOSE, "array");
        return new ArrayLiteralExpression(open, close, elements);
    }

    private ArrayComprehensionExpression arrayComprehensionExpression(final Token open, final Expression expr) {
        final Token forToken = this.expectAfter(TokenType.FOR, "expression");
        final Token name = this.expect(TokenType.IDENTIFIER, "variable name");

        final Token inToken = this.expectAfter(TokenType.IN, "variable name");
        final Expression iterable = this.expression();

        Token ifToken = null;
        Expression condition = null;
        if (this.match(TokenType.IF)) {
            ifToken = this.previous();
            condition = this.expression();
        }

        Token elseToken = null;
        Expression elseExpr = null;
        if (this.match(TokenType.ELSE)) {
            elseToken = this.previous();
            elseExpr = this.expression();
        }

        final Token close = this.expectAfter(TokenType.ARRAY_CLOSE, "array");

        return new ArrayComprehensionExpression(iterable, expr, name, forToken, inToken, open, close, ifToken, condition, elseToken, elseExpr);
    }

    private SwitchStatement switchStatement() {
        final Token switchToken = this.previous();
        this.expectAfter(TokenType.LEFT_PAREN, "switch");
        final Expression expr = this.expression();
        this.expectAfter(TokenType.RIGHT_PAREN, "expression");

        this.expectAfter(TokenType.LEFT_BRACE, "switch");

        SwitchCase defaultBody = null;
        final List<SwitchCase> cases = new ArrayList<>();
        final Set<Object> matchedLiterals = new HashSet<>();

        while (this.match(TokenType.CASE, TokenType.DEFAULT)) {
            final Token caseToken = this.previous();

            if (caseToken.type() == TokenType.CASE) {
                final Expression caseExpr = this.primary();
                if (!(caseExpr instanceof final LiteralExpression literal)) {
                    throw new ScriptEvaluationError(Phase.PARSING, caseToken, DiagnosticMessage.NON_LITERAL_CASE_EXPRESSION, caseExpr);
                }

                if (matchedLiterals.contains(literal.value())) {
                    throw new ScriptEvaluationError(Phase.PARSING, caseToken, DiagnosticMessage.DUPLICATE_CASE_EXPRESSION, literal.value());
                }
                matchedLiterals.add(literal.value());

                final Statement body = this.caseBody();
                cases.add(new SwitchCase(caseToken, body, literal, false));
            }
            else {
                final Statement body = this.caseBody();
                defaultBody = new SwitchCase(caseToken, body, null, true);
            }
        }

        this.expectAfter(TokenType.RIGHT_BRACE, "switch");

        return new SwitchStatement(switchToken, expr, cases, defaultBody);
    }

    private Statement caseBody() {
        return this.blockOrExpression(TokenType.COLON, TokenType.CASE, TokenType.DEFAULT);
    }

    private Statement blockOrExpression(final TokenType blockOpen, final TokenType... blockClose) {
        if (this.match(blockOpen)) {
            final Token colon = this.previous();
            final List<Statement> statements = new ArrayList<>();
            while (!this.check(blockClose)) {
                statements.add(this.declaration());
            }
            return new BlockStatement(colon, statements);
        }
        else if (this.match(TokenType.ARROW)) {
            return this.expressionStatement();
        }
        else {
            throw new ScriptEvaluationError(Phase.PARSING, this.peek(), DiagnosticMessage.EXPECTED_X_OR_Y, blockOpen.representation(), TokenType.ARROW.representation(), this.peek().type().representation());
        }
    }

    private Token consume(final TokenType type, final DiagnosticMessage message, Object... args) {
        if (this.check(type))
            return this.advance();
        if (type != TokenType.SEMICOLON) {
            final Object[] arguments = new Object[args.length + 1];
            System.arraycopy(args, 0, arguments, 1, args.length);
            arguments[args.length] = this.peek().type().representation();
            throw new ScriptEvaluationError(Phase.PARSING, this.peek(), message, arguments);
        }
        return null;
    }

    private Token expect(final TokenType type) {
        return this.expect(type, type.representation() + (type.keyword() ? " keyword" : ""));
    }

    private Token expect(final TokenType type, final String what) {
        return this.consume(type, DiagnosticMessage.EXPECTED_X, what);
    }

    private Token expectBefore(final TokenType type, final String before) {
        return this.expectAround(type, before, "before");
    }

    private Token expectAfter(final TokenType type, final TokenType after) {
        return this.expectAround(type, after.representation(), "after");
    }

    private Token expectAfter(final TokenType type, final String after) {
        return this.expectAround(type, after, "after");
    }

    private Token expectAround(final TokenType type, final String where, final String position) {
        return this.expectAround(type, type.representation(), where, position);
    }

    private Token expectAround(final TokenType type, final String what, final String where, final String position) {
        return this.consume(type, DiagnosticMessage.EXPECTED_X_AT_Z, what, position, where);
    }
}
