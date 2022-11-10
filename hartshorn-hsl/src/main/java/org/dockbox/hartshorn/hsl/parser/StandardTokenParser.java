package org.dockbox.hartshorn.hsl.parser;

import org.dockbox.hartshorn.context.DefaultContext;
import org.dockbox.hartshorn.hsl.ScriptEvaluationError;
import org.dockbox.hartshorn.hsl.ast.ASTNode;
import org.dockbox.hartshorn.hsl.ast.expression.Expression;
import org.dockbox.hartshorn.hsl.ast.statement.ExpressionStatement;
import org.dockbox.hartshorn.hsl.ast.statement.Statement;
import org.dockbox.hartshorn.hsl.parser.expression.ComplexExpressionParserAdapter;
import org.dockbox.hartshorn.hsl.parser.expression.ExpressionParser;
import org.dockbox.hartshorn.hsl.runtime.Phase;
import org.dockbox.hartshorn.hsl.token.Token;
import org.dockbox.hartshorn.hsl.token.TokenType;
import org.dockbox.hartshorn.inject.binding.Bound;
import org.dockbox.hartshorn.util.TypeUtils;
import org.dockbox.hartshorn.util.option.Option;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.inject.Inject;

public class StandardTokenParser extends DefaultContext implements TokenParser {

    private int current = 0;
    private final List<Token> tokens;

    private final Set<ASTNodeParser<? extends Statement>> statementParsers = ConcurrentHashMap.newKeySet();
    private final TokenStepValidator validator;
    private final ExpressionParser expressionParser;

    @Inject
    public StandardTokenParser() {
        this(new ArrayList<>());
    }

    @Inject
    public StandardTokenParser(final ExpressionParser parser, final TokenStepValidator validator) {
        this(new ArrayList<>(), parser, validator);
    }

    @Bound
    public StandardTokenParser(final List<Token> tokens) {
        this.expressionParser = new ComplexExpressionParserAdapter();
        this.validator = new StandardTokenStepValidator(this);
        this.tokens = tokens;
    }

    @Bound
    public StandardTokenParser(final List<Token> tokens, final ExpressionParser parser, final TokenStepValidator validator) {
        this.tokens = tokens;
        this.expressionParser = parser;
        this.validator = validator;
    }

    @Override
    public StandardTokenParser statementParser(final ASTNodeParser<? extends Statement> parser) {
        if (parser != null) {
            for(final Class<? extends Statement> type : parser.types()) {
                if (!Statement.class.isAssignableFrom(type)) {
                    throw new IllegalArgumentException("Parser " + parser.getClass().getName() + " indicated potential yield of type type " + type.getName() + " which is not a child of Statement");
                }
            }
            this.statementParsers.add(parser);
        }
        return this;
    }

    @Override
    public List<Statement> parse() {
        final List<Statement> statements = new ArrayList<>();
        while (!this.isAtEnd()) {
            statements.add(this.statement());
        }
        return statements;
    }

    @Override
    public boolean match(final TokenType... types) {
        return this.find(types) != null;
    }

    @Override
    public Token find(final TokenType... types) {
        for (final TokenType type : types) {
            if (this.check(type)) {
                final Token token = this.peek();
                this.advance();
                return token;
            }
        }
        return null;
    }

    @Override
    public boolean check(final TokenType... types) {
        if (this.isAtEnd()) return false;
        for (final TokenType type : types) {
            if (this.peek().type() == type) return true;
        }
        return false;
    }

    @Override
    public Token advance() {
        if (!this.isAtEnd()) this.current++;
        return this.previous();
    }

    @Override
    public boolean isAtEnd() {
        return this.peek().type() == TokenType.EOF;
    }

    @Override
    public Token peek() {
        return this.tokens.get(this.current);
    }

    @Override
    public Token previous() {
        return this.tokens.get(this.current - 1);
    }

    @Override
    public Token consume(final TokenType type, final String message) {
        if (this.check(type))
            return this.advance();
        if (type != TokenType.SEMICOLON) throw new ScriptEvaluationError(message, Phase.PARSING, this.peek());
        return null;
    }

    @Override
    public Statement statement() {
        for (final ASTNodeParser<? extends Statement> parser : this.statementParsers) {
            final Option<? extends Statement> statement = parser.parse(this, this.validator)
                    .attempt(ScriptEvaluationError.class)
                    .rethrow();
            if (statement.present()) return statement.get();
        }

        final TokenType type = this.peek().type();
        if (type.standaloneStatement()) {
            throw new ScriptEvaluationError("Unsupported standalone statement type: " + type, Phase.PARSING, this.peek());
        }

        return this.expressionStatement();
    }

    @Override
    public ExpressionStatement expressionStatement() {
        final Expression expr = this.expression();
        this.validator.expectAfter(TokenType.SEMICOLON, "expression");
        return new ExpressionStatement(expr);
    }

    @Override
    public Expression expression() {
        return this.expressionParser.parse(this, this.validator)
                .attempt(ScriptEvaluationError.class)
                .rethrow()
                .orElseThrow(() -> new ScriptEvaluationError("Unsupported expression type", Phase.PARSING, this.peek()));
    }

    @Override
    public List<Statement> consume() {
        final List<Statement> statements = new ArrayList<>();
        while (!this.check(TokenType.RIGHT_BRACE) && !this.isAtEnd()) {
            statements.add(this.statement());
        }
        this.validator.expectAfter(TokenType.RIGHT_BRACE, "block");
        return statements;
    }

    @Override
    public <T extends Statement> Set<ASTNodeParser<T>> compatibleParsers(final Class<T> type) {
        return this.compatibleParserStream(type)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public <T extends Statement> Option<ASTNodeParser<T>> firstCompatibleParser(final Class<T> type) {
        return Option.of(this.compatibleParserStream(type).findFirst());
    }

    private <T extends Statement> Stream<ASTNodeParser<T>> compatibleParserStream(final Class<T> type) {
        if (Statement.class.isAssignableFrom(type)) {
            return this.compatibleParserStream(this.statementParsers, type);
        }
        return Stream.empty();
    }

    private <T extends ASTNode, N extends ASTNode> Stream<ASTNodeParser<T>> compatibleParserStream(final Collection<? extends ASTNodeParser<? extends N>> parsers, final Class<T> type) {
        return parsers.stream()
                .filter(parser -> parser.types().contains(type))
                .map(parser -> TypeUtils.adjustWildcards(parser, ASTNodeParser.class));
    }
}
