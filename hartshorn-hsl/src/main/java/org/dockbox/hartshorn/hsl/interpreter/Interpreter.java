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

package org.dockbox.hartshorn.hsl.interpreter;

import org.dockbox.hartshorn.hsl.ScriptEvaluationError;
import org.dockbox.hartshorn.hsl.ast.MoveKeyword;
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
import org.dockbox.hartshorn.hsl.ast.statement.ForEachStatement;
import org.dockbox.hartshorn.hsl.ast.statement.ForStatement;
import org.dockbox.hartshorn.hsl.ast.statement.FunctionStatement;
import org.dockbox.hartshorn.hsl.ast.statement.IfStatement;
import org.dockbox.hartshorn.hsl.ast.statement.ModuleStatement;
import org.dockbox.hartshorn.hsl.ast.statement.NativeFunctionStatement;
import org.dockbox.hartshorn.hsl.ast.statement.RepeatStatement;
import org.dockbox.hartshorn.hsl.ast.statement.ReturnStatement;
import org.dockbox.hartshorn.hsl.ast.statement.Statement;
import org.dockbox.hartshorn.hsl.ast.statement.SwitchCase;
import org.dockbox.hartshorn.hsl.ast.statement.SwitchStatement;
import org.dockbox.hartshorn.hsl.ast.statement.TestStatement;
import org.dockbox.hartshorn.hsl.ast.statement.VariableStatement;
import org.dockbox.hartshorn.hsl.ast.statement.WhileStatement;
import org.dockbox.hartshorn.hsl.modules.HslLibrary;
import org.dockbox.hartshorn.hsl.modules.NativeModule;
import org.dockbox.hartshorn.hsl.objects.BindableNode;
import org.dockbox.hartshorn.hsl.objects.CallableNode;
import org.dockbox.hartshorn.hsl.objects.ClassReference;
import org.dockbox.hartshorn.hsl.objects.ExternalObjectReference;
import org.dockbox.hartshorn.hsl.objects.InstanceReference;
import org.dockbox.hartshorn.hsl.objects.MethodReference;
import org.dockbox.hartshorn.hsl.objects.PropertyContainer;
import org.dockbox.hartshorn.hsl.objects.external.ExternalClass;
import org.dockbox.hartshorn.hsl.objects.external.ExternalFunction;
import org.dockbox.hartshorn.hsl.objects.external.ExternalInstance;
import org.dockbox.hartshorn.hsl.objects.virtual.VirtualClass;
import org.dockbox.hartshorn.hsl.objects.virtual.VirtualClassBuilder;
import org.dockbox.hartshorn.hsl.objects.virtual.VirtualFunction;
import org.dockbox.hartshorn.hsl.objects.virtual.VirtualMemberFunction;
import org.dockbox.hartshorn.hsl.runtime.DiagnosticMessage;
import org.dockbox.hartshorn.hsl.runtime.Phase;
import org.dockbox.hartshorn.hsl.runtime.Return;
import org.dockbox.hartshorn.hsl.runtime.RuntimeError;
import org.dockbox.hartshorn.hsl.runtime.Yield;
import org.dockbox.hartshorn.hsl.token.Token;
import org.dockbox.hartshorn.hsl.token.TokenType;
import org.dockbox.hartshorn.hsl.visitors.ExpressionVisitor;
import org.dockbox.hartshorn.hsl.visitors.StatementVisitor;
import org.dockbox.hartshorn.inject.binding.Bound;
import org.dockbox.hartshorn.util.ApplicationException;
import org.slf4j.Logger;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

/**
 * Standard interpreter for HSL. This interpreter is capable of executing HSL code by visiting the AST
 * step by step. The interpreter is capable of handling all types of statements and expressions. The
 * interpreter is also capable of handling external classes and modules, as well as virtual classes and
 * functions.
 *
 * <p>During the execution of a script, the interpreter will track its global variables in a
 * {@link VariableScope}, and report any results to the configured {@link ResultCollector}.
 *
 * <p><code>print</code> statements are handled by the configured {@link Logger}, and are not persisted
 * in a local state.
 *
 * <p>Any interpreter instance can only be used <b>once</b>, and should be disposed of after use. This
 * is to prevent scope pollution, and potential leaking of errors and results.
 *
 * <p>Interpretation starts with the {@link #interpret(List)} method, which takes a list of statements
 * which have been previously parsed by a {@link org.dockbox.hartshorn.hsl.parser.Parser}, and
 * preferably resolved by a {@link org.dockbox.hartshorn.hsl.semantic.Resolver}.
 *
 * @author Guus Lieben
 * @since 22.4
 */
public class Interpreter implements ExpressionVisitor<Object>, StatementVisitor<Void> {

    private final Map<String, ExternalInstance> externalVariables = new ConcurrentHashMap<>();
    private final Map<String, ExternalClass<?>> imports = new ConcurrentHashMap<>();
    private final Map<Expression, Integer> locals = new ConcurrentHashMap<>();

    private final Map<String, NativeModule> externalModules;
    private final ResultCollector resultCollector;

    private VariableScope global = new VariableScope();
    private VariableScope visitingScope = this.global;
    private boolean isRunning = false;

    @Bound
    public Interpreter(final ResultCollector resultCollector, final Map<String, NativeModule> externalModules) {
        this.resultCollector = resultCollector;
        this.externalModules = new ConcurrentHashMap<>(externalModules);
    }

    /**
     * Restores the interpreter to its initial state. This is to prevent scope pollution, and potential
     * leaking of errors and results. This does not clear the external modules and variables, nor the
     * dynamic imports, as these can be reused safely.
     *
     * <p>This method should be called before starting a new runtime. This should be at least before a
     * potential {@link org.dockbox.hartshorn.hsl.semantic.Resolver} is called, as the resolver will
     * typically modify the {@link #locals local variable} map.
     */
    public void restore() {
        this.global = new VariableScope();
        this.visitingScope = this.global;
        this.locals.clear();
        this.resultCollector.clear();
    }

    public void externalModule(final String name, final NativeModule module) {
        this.externalModules.put(name, module);
    }

    public void externalModules(final Map<String, NativeModule> externalModules) {
        this.externalModules.putAll(externalModules);
    }

    public Map<String, NativeModule> externalModules() {
        return this.externalModules;
    }

    public Map<String, Object> global() {
        return this.global.values();
    }

    public void interpret(final List<Statement> statements) {
        if (this.isRunning) {
            throw new IllegalAccessException("Cannot reuse the same interpreter instance for multiple executions");
        }
        this.isRunning = true;
        try {
            for (final Statement statement : statements) {
                this.execute(statement);
            }
        }
        catch (final RuntimeError error) {
            throw new ScriptEvaluationError(Phase.INTERPRETING, error.at(), error);
        }
        finally {
            this.isRunning = false;
        }
    }

    @Override
    public Object visit(final BinaryExpression expr) {
        Object left = this.evaluate(expr.leftExpression());
        Object right = this.evaluate(expr.rightExpression());

        left = this.unwrap(left);
        right = this.unwrap(right);

        switch (expr.operator().type()) {
            case PLUS -> {
                // Math plus
                if (left instanceof Number nl && right instanceof Number nr) {
                    return nl.doubleValue() + nr.doubleValue();
                }
                // String Addition
                if (left instanceof String || right instanceof String) {
                    // String.valueOf to handle nulls
                    return String.valueOf(left) + right;
                }

                // Special cases
                if ((left instanceof Character && right instanceof Character)) {
                    return String.valueOf(left) + right;
                }
                if ((left instanceof Character) && (right instanceof Number num)) {
                    final int value = (Character) left;
                    return num.doubleValue() + value;
                }
                if ((left instanceof Number num) && (right instanceof Character)) {
                    final int value = (Character) right;
                    return num.doubleValue() + value;
                }
                throw new RuntimeError(expr.operator(), DiagnosticMessage.UNSUPPORTED_CHILD, TokenType.PLUS.representation(), "%s %s %s"
                        .formatted(left.getClass().getSimpleName(), TokenType.PLUS.representation(), right.getClass().getSimpleName()));
            }
            case MINUS -> {
                this.checkNumberOperands(expr.operator(), left, right);
                return ((Number) left).doubleValue() - ((Number) right).doubleValue();
            }
            case STAR -> {
                if ((left instanceof String || left instanceof Character) && right instanceof Number num) {
                    final int times = num.intValue();
                    final int finalLen = left.toString().length() * times;
                    final StringBuilder result = new StringBuilder(finalLen);
                    final String strValue = left.toString();
                    result.append(strValue.repeat(Math.max(0, times)));
                    return result.toString();
                }
                else if (left instanceof Array array && right instanceof Number num) {
                    final int times = num.intValue();
                    final int finalLen = array.length() * times;
                    final Array result = new Array(finalLen);
                    for (int i = 0; i < times; i++) {
                        final int originalIndex = times % array.length();
                        result.value(array.value(originalIndex), i);
                    }
                    return result;
                }
                this.checkNumberOperands(expr.operator(), left, right);
                return number(left) * number(right);
            }
            case MODULO -> {
                this.checkNumberOperands(expr.operator(), left, right);
                return number(left) % number(right);
            }
            case SLASH -> {
                this.checkNumberOperands(expr.operator(), left, right);
                if (number(right) == 0) {
                    throw new RuntimeError(expr.operator(), DiagnosticMessage.ILLEGAL_ZERO_DIVISION);
                }
                return number(left) / number(right);
            }
            case GREATER -> {
                this.checkNumberOperands(expr.operator(), left, right);
                return number(left) > number(right);
            }
            case GREATER_EQUAL -> {
                this.checkNumberOperands(expr.operator(), left, right);
                return number(left) >= number(right);
            }
            case LESS -> {
                this.checkNumberOperands(expr.operator(), left, right);
                return number(left) < number(right);
            }
            case LESS_EQUAL -> {
                this.checkNumberOperands(expr.operator(), left, right);
                return number(left) <= number(right);
            }
            case BANG_EQUAL -> {
                return !this.isEqual(left, right);
            }
            case EQUAL_EQUAL -> {
                return this.isEqual(left, right);
            }
        }
        return null;
    }

    @Override
    public Object visit(final RangeExpression expr) {
        final Object start = this.unwrap(this.evaluate(expr.leftExpression()));
        final Object end = this.unwrap(this.evaluate(expr.rightExpression()));

        this.checkNumberOperands(expr.operator(), start, end);

        final int min = ((Number) start).intValue();
        final int max = ((Number) end).intValue();

        final int length = max - min + 1;
        final Object[] result = new Object[length];
        for (int i = 0; i < length; i++) {
            result[i] = (double) min + i;
        }
        return new Array(result);
    }

    @Override
    public Object visit(final GroupingExpression expr) {
        return this.evaluate(expr.expression());
    }

    @Override
    public Object visit(final LiteralExpression expr) {
        return expr.value();
    }

    @Override
    public Object visit(final AssignExpression expr) {
        final Token name = expr.name();
        final Object value = this.evaluate(expr.value());

        final Integer distance = this.locals.get(expr);
        if (distance != null) {
            this.variableScope().assignAt(distance, name, value);
        }
        else {
            this.global.assign(name, value);
        }
        return value;
    }

    @Override
    public Object visit(final LogicalAssignExpression expr) {
        final Token name = expr.name();
        final Object left = this.lookUpVariable(name, expr);

        final Object right = this.evaluate(expr.value());

        final Token op = expr.assignmentOperator();
        final TokenType bitwiseOperator = expr.logicalOperator();

        // Virtual token to indicate the position of the operator
        final Token token = new Token(bitwiseOperator, op.lexeme(), op.line(), op.column());
        final Object result = this.getBitwiseResult(token, left, right);

        final Integer distance = this.locals.get(expr);
        if (distance != null) {
            this.variableScope().assignAt(distance, name, result);
        }
        else {
            this.global.assign(name, result);
        }
        return result;
    }

    @Override
    public Object visit(final UnaryExpression expr) {
        final Object right = this.evaluate(expr.rightExpression());
        final Object newValue = switch (expr.operator().type()) {
            case MINUS -> {
                this.checkNumberOperand(expr.operator(), right);
                yield -number(right);
            }
            case PLUS_PLUS -> {
                this.checkNumberOperand(expr.operator(), right);
                yield number(right) + 1;
            }
            case MINUS_MINUS -> {
                this.checkNumberOperand(expr.operator(), right);
                yield number(right) - 1;
            }
            case BANG -> !this.truthy(right);
            case COMPLEMENT -> {
                this.checkNumberOperand(expr.operator(), right);
                final int value = (int) number(right);
                // Cast to int is redundant, but required to suppress false-positive inspections.
                //noinspection RedundantCast
                yield (int) ~value;
            }
            default -> null;
        };
        this.assignIfVariable(expr.rightExpression(), newValue);
        return newValue;
    }

    @Override
    public Object visit(final PostfixExpression expr) {
        final Object left = this.evaluate(expr.leftExpression());
        this.checkNumberOperand(expr.operator(), left);

        final float ln = number(left);
        final double newValue = switch (expr.operator().type()) {
            case PLUS_PLUS -> ln + 1;
            case MINUS_MINUS -> ln -1;
            default -> throw new RuntimeError(expr.operator(), DiagnosticMessage.ILLEGAL_POSTFIX, expr.operator().type());
        };
        this.assignIfVariable(expr.leftExpression(), newValue);
        return left;
    }

    private void assignIfVariable(final Expression expression, final Object value) {
        if (expression instanceof VariableExpression variable) {
            this.variableScope().assign(variable.name(), value);
        }
    }

    @Override
    public Object visit(final LogicalExpression expr) {
        final Object left = this.evaluate(expr.leftExpression());
        switch (expr.operator().type()) {
            case AND -> {
                if (!this.truthy(left)) {
                    return false;
                }
                // Don't evaluate right if left is not truthy
                final Object right = this.evaluate(expr.rightExpression());
                return this.truthy(right);
            }
            case OR -> {
                if (this.truthy(left)) {
                    return true;
                }
                // No need to evaluate right if left is already truthy
                final Object right = this.evaluate(expr.rightExpression());
                return this.truthy(right);
            }
            case XOR -> {
                final Object right = this.evaluate(expr.rightExpression());
                return this.xor(left, right);
            }
            default -> throw new RuntimeError(expr.operator(), DiagnosticMessage.UNSUPPORTED_LOGICAL, expr.operator().type().representation());
        }
    }

    @Override
    public Object visit(final BitwiseExpression expr) {
        final Object left = this.evaluate(expr.leftExpression());
        final Object right = this.evaluate(expr.rightExpression());
        return this.getBitwiseResult(expr.operator(), left, right);
    }

    private Object getBitwiseResult(final Token operator, final Object left, final Object right) {
        if (left instanceof Number && right instanceof Number) {
            final int iLeft = ((Number) left).intValue();
            final int iRight = ((Number) right).intValue();

            return switch (operator.type()) {
                case SHIFT_RIGHT -> iLeft >> iRight;
                case SHIFT_LEFT -> iLeft << iRight;
                case LOGICAL_SHIFT_RIGHT -> iLeft >>> iRight;
                case BITWISE_AND -> iLeft & iRight;
                case BITWISE_OR -> iLeft | iRight;
                case XOR -> this.xor(iLeft, iRight);
                default -> throw new RuntimeError(operator, DiagnosticMessage.UNSUPPORTED_BITWISE, operator.type().representation());
            };
        }
        final String leftType = left != null ? left.getClass().getSimpleName() : null;
        final String rightType = right != null ? right.getClass().getSimpleName() : null;
        throw new RuntimeError(operator, DiagnosticMessage.ILLEGAL_BITWISE_OP, left, leftType, right, rightType);
    }

    private Object xor(final Object left, final Object right) {
        if (left instanceof Number nleft && right instanceof Number nright) {
            final int iLeft = nleft.intValue();
            final int iRight = nright.intValue();
            return iLeft ^ iRight;
        }
        return this.truthy(left) ^ this.truthy(right);
    }

    @Override
    public Object visit(final ElvisExpression expr) {
        final Object condition = this.evaluate(expr.condition());
        if (this.truthy(condition)) {
            return condition;
        }
        return this.evaluate(expr.rightExpression());
    }

    @Override
    public Object visit(final TernaryExpression expr) {
        final Object condition = this.evaluate(expr.condition());
        if (this.truthy(condition)) {
            return this.evaluate(expr.firstExpression());
        }
        return this.evaluate(expr.secondExpression());
    }

    @Override
    public Object visit(final ArraySetExpression expr) {
        return this.accessArray(expr.name(), expr.index(), (array, index) -> {
            final Object value = this.evaluate(expr.value());
            array.value(value, index);
            return value;
        });
    }

    @Override
    public Object visit(final ArrayGetExpression expr) {
        return this.accessArray(expr.name(), expr.index(), Array::value);
    }

    @Override
    public Object visit(final ArrayLiteralExpression expr) {
        final List<Object> values = new ArrayList<>();
        for(final Expression expression : expr.elements()) {
            final Object evaluate = this.evaluate(expression);
            values.add(evaluate);
        }
        return new Array(values.toArray());
    }

    @Override
    public Object visit(final ArrayComprehensionExpression expr) {
        final List<Object> values = new ArrayList<>();
        final Object collection = this.evaluate(expr.collection());
        if (collection instanceof Iterable<?> iterable) {

            this.withNextScope(() -> {
                this.variableScope().define(expr.selector().lexeme(), null);

                this.withNextScope(() -> {
                    for (final Object element : iterable) {
                        this.variableScope().assign(expr.selector(), element);

                        if (expr.condition() != null) {
                            final Object condition = this.evaluate(expr.condition());
                            if (!this.truthy(condition)) {
                                if (expr.elseExpression() != null) {
                                    final Object elseValue = this.evaluate(expr.elseExpression());
                                    values.add(elseValue);
                                }
                                continue;
                            }
                        }

                        final Object result = this.evaluate(expr.expression());
                        values.add(result);
                    }
                });
            });
        }
        else {
            throw new RuntimeError(expr.open(), DiagnosticMessage.NON_ITERABLE_COLLECTION, collection);
        }
        return new Array(values.toArray());
    }

    private Object accessArray(final Token name, final Expression indexExp, final BiFunction<Array, Integer, Object> converter) {
        final Array array = (Array) this.variableScope().get(name);
        final Number indexValue = (Number) this.evaluate(indexExp);
        final int index = indexValue.intValue();

        if (index < 0 || array.length() < index) {
            throw new ArrayIndexOutOfBoundsException("Size can't be negative or bigger than array size");
        }

        return converter.apply(array, index);
    }

    @Override
    public Object visit(final PrefixExpression expr) {
        final CallableNode value = (CallableNode) this.variableScope().get(expr.prefixOperatorName());
        final List<Object> args = new ArrayList<>();
        args.add(this.evaluate(expr.rightExpression()));
        try {
            return value.call(expr.prefixOperatorName(), this, null, args);
        }
        catch (final ApplicationException e) {
            throw new RuntimeError(expr.prefixOperatorName(), e);
        }
    }

    @Override
    public Object visit(final InfixExpression expr) {
        final CallableNode value = (CallableNode) this.variableScope().get(expr.infixOperatorName());
        final List<Object> args = new ArrayList<>();
        args.add(this.evaluate(expr.leftExpression()));
        args.add(this.evaluate(expr.rightExpression()));

        try {
            return value.call(expr.infixOperatorName(), this, null, args);
        }
        catch (final ApplicationException e) {
            throw new RuntimeError(expr.infixOperatorName(), e);
        }
    }

    @Override
    public Object visit(final FunctionCallExpression expr) {
        final Object callee = this.evaluate(expr.callee());

        final List<Object> arguments = new ArrayList<>();
        for (final Expression argument : expr.arguments()) {
            Object evaluated = this.evaluate(argument);
            if (evaluated instanceof ExternalObjectReference external) evaluated = external.externalObject();
            arguments.add(evaluated);
        }

        // Can't call non-callable nodes..
        if (!(callee instanceof final CallableNode function)) {
            throw new RuntimeError(expr.openParenthesis(), DiagnosticMessage.NON_CALLABLE_FUNCTION, callee);
        }

        try {
            if (callee instanceof InstanceReference instance) {
                return function.call(expr.openParenthesis(), this, instance, arguments);
            }
            else if (callee instanceof BindableNode<?> bindable){
                return function.call(expr.openParenthesis(), this, bindable.bound(), arguments);
            }
            else {
                return function.call(expr.openParenthesis(), this, null, arguments);
            }
        }
        catch (final ApplicationException e) {
            throw new RuntimeError(expr.openParenthesis(), e);
        }
    }

    @Override
    public Object visit(final GetExpression expr) {
        final Object object = this.evaluate(expr.object());
        if (object instanceof PropertyContainer container) {
            Object result = container.get(this, expr.name(), this.variableScope());
            if (result instanceof ExternalObjectReference objectReference) result = objectReference.externalObject();
            if (result instanceof ExternalFunction bindableNode && object instanceof InstanceReference instance) {
                return bindableNode.bind(instance);
            }
            return result;
        }
        throw new RuntimeError(expr.name(), DiagnosticMessage.NON_PROPERTY_CONTAINER, object);
    }

    @Override
    public Object visit(final SetExpression expr) {
        final Object object = this.evaluate(expr.object());

        if (object instanceof PropertyContainer instance) {
            final Object value = this.evaluate(expr.value());
            instance.set(this, expr.name(), value, this.variableScope());
            return value;
        }
        throw new RuntimeError(expr.name(), DiagnosticMessage.NON_PROPERTY_CONTAINER, object);
    }

    @Override
    public Object visit(final ThisExpression expr) {
        return this.lookUpVariable(expr.keyword(), expr);
    }

    @Override
    public Object visit(final VariableExpression expr) {
        return this.lookUpVariable(expr.name(), expr);
    }

    @Override
    public Object visit(final SuperExpression expr) {
        final int distance = this.locals.get(expr);
        final ClassReference superClass = (ClassReference) this.variableScope().getAt(expr.method(), distance, TokenType.SUPER.representation());
        final InstanceReference object = (InstanceReference) this.variableScope().getAt(expr.method(), distance - 1, TokenType.THIS.representation());
        final MethodReference method = superClass.method(expr.method().lexeme());

        if (method == null) {
            throw new RuntimeError(expr.method(), DiagnosticMessage.UNDEFINED_PROPERTY, expr.method().lexeme(), superClass.name());
        }
        return method.bind(object);
    }

    @Override
    public Void visit(final ExpressionStatement statement) {
        this.evaluate(statement.expression());
        return null;
    }

    @Override
    public Void visit(final BlockStatement statement) {
        this.execute(statement.statements(), new VariableScope(this.variableScope()));
        return null;
    }

    @Override
    public Void visit(final IfStatement statement) {
        final Object conditionResult = this.evaluate(statement.condition());
        final VariableScope previous = this.variableScope();

        if (this.truthy(conditionResult)) {
            final VariableScope thenVariableScope = new VariableScope(previous);
            this.visitingScope = thenVariableScope;
            this.execute(statement.thenBranch(), thenVariableScope);
        }
        else if (statement.elseBranch() != null) {
            final VariableScope elseVariableScope = new VariableScope(previous);
            this.visitingScope = elseVariableScope;
            this.execute(statement.elseBranch(), elseVariableScope);
        }
        this.visitingScope = previous;
        return null;
    }

    @Override
    public Void visit(final WhileStatement statement) {
        while (this.truthy(this.evaluate(statement.condition()))) {
            try {
                this.execute(statement.body());
            }
            catch (final MoveKeyword moveKeyword) {
                if (moveKeyword.moveType() == MoveKeyword.MoveType.BREAK) {
                    break;
                }
            }
        }
        return null;
    }

    @Override
    public Void visit(final DoWhileStatement statement) {
        this.withNextScope(() -> {
            do {
                try {
                    this.execute(statement.body());
                }
                catch (final MoveKeyword moveKeyword) {
                    if (moveKeyword.moveType() == MoveKeyword.MoveType.BREAK) break;
                }
            }
            while (this.truthy(this.evaluate(statement.condition())));
        });
        return null;
    }

    @Override
    public Void visit(final ForStatement statement) {
        this.withNextScope(() -> {
            this.execute(statement.initializer());
            while (this.truthy(this.evaluate(statement.condition()))) {
                try {
                    this.execute(statement.body());
                }
                catch (final MoveKeyword moveKeyword) {
                    if (moveKeyword.moveType() == MoveKeyword.MoveType.BREAK) break;
                }
                this.execute(statement.increment());
            }
        });
        return null;
    }

    @Override
    public Void visit(final ForEachStatement statement) {
        this.withNextScope(() -> {
            Object collection = this.evaluate(statement.collection());
            collection = this.unwrap(collection);

            if (collection instanceof Iterable<?> iterable) {
                this.variableScope().define(statement.selector().name().lexeme(), null);
                for (final Object item : iterable) {
                    this.variableScope().assign(statement.selector().name(), item);
                    this.execute(statement.body());
                }
            }
            else {
                throw new RuntimeException("Only iterables are supported for for-each.");
            }
        });
        return null;
    }

    @Override
    public Void visit(final RepeatStatement statement) {
        this.withNextScope(() -> {
            final Object value = this.evaluate(statement.value());

            final boolean isNotNumber = !(value instanceof Number);

            if (isNotNumber) {
                throw new RuntimeException("Repeat Counter must be number");
            }

            final int counter = (int) number(value);
            for (int i = 0; i < counter; i++) {
                try {
                    this.execute(statement.body());
                }
                catch (final MoveKeyword moveKeyword) {
                    if (moveKeyword.moveType() == MoveKeyword.MoveType.BREAK) break;
                }
            }
        });
        return null;
    }

    @Override
    public Void visit(final VariableStatement statement) {
        Object value = null;
        if (statement.initializer() != null) {
            value = this.evaluate(statement.initializer());
        }
        this.variableScope().define(statement.name().lexeme(), value);
        return null;
    }

    @Override
    public Void visit(final ReturnStatement statement) {
        Object value = null;
        if (statement.value() != null) {
            value = this.evaluate(statement.value());
        }
        throw switch (statement.returnType()) {
            case RETURN -> new Return(value);
            case YIELD -> new Yield(value);
            default -> new RuntimeError(statement.keyword(), DiagnosticMessage.ILLEGAL_RETURN, statement.keyword().type().representation());
        };
    }

    @Override
    public Void visit(final ClassStatement statement) {
        Object superClass = null;
        // Because super class is a variable expression assert it's a class
        if (statement.superClass() != null) {
            superClass = this.evaluate(statement.superClass());
            if (!(superClass instanceof ClassReference virtualClass)) {
                throw new RuntimeError(statement.superClass(), DiagnosticMessage.INVALID_SUPER_TYPE, superClass);
            }
            if (virtualClass.isFinal()) {
                throw new ScriptEvaluationError(Phase.INTERPRETING, statement.superClass().name(), DiagnosticMessage.ILLEGAL_FINAL_SUPER_TYPE, virtualClass.name());
            }
        }

        this.variableScope().define(statement.name().lexeme(), null);

        final VirtualClassBuilder builder = new VirtualClassBuilder(statement.name());

        final ClassReference superClassReference = (ClassReference) superClass;
        builder.superClass(superClassReference);
        builder.isFinal(statement.isFinal());
        builder.dynamic(statement.isDynamic());

        this.withNextScope(builder, () -> {
            builder.variableScope(this.variableScope());

            if (statement.superClass() != null) {
                this.visitingScope = new VariableScope(this.variableScope());
                this.variableScope().define(TokenType.SUPER.representation(), superClassReference);
            }

            // Bind all method into the class
            for (final FunctionStatement method : statement.methods()) {
                final VirtualFunction function = new VirtualFunction(method, this.variableScope(), false);
                builder.methods().put(method.name().lexeme(), function);
            }

            if (statement.constructor() != null) {
                builder.constructor(new VirtualFunction(statement.constructor(), this.variableScope(), true));
            }

            statement.fields().forEach(field -> {
                builder.field(field);
                this.execute(field.getter());
                this.execute(field.setter());
            });

            final VirtualClass virtualClass = builder.build();

            if (superClassReference != null) {
                this.visitingScope = this.variableScope().enclosing();
            }

            this.variableScope().enclosing().assign(statement.name(), virtualClass);
        });

        return null;
    }

    @Override
    public Void visit(final NativeFunctionStatement statement) {
        final HslLibrary hslLibrary = new HslLibrary(statement, this.externalModules);
        this.variableScope().define(statement.name().lexeme(), hslLibrary);
        return null;
    }

    @Override
    public Void visit(final TestStatement statement) {
        final String name = statement.name().literal().toString();
        final VariableScope variableScope = new VariableScope(this.global);
        try {
            this.execute(statement.body(), variableScope);
        }
        catch (final Yield yield) {
            final Object value = yield.value();
            final boolean val = this.truthy(value);
            this.resultCollector.addResult(name, val);
        }
        return null;
    }

    @Override
    public Void visit(final ModuleStatement statement) {
        final String moduleName = statement.name().lexeme();
        final NativeModule module = this.externalModules().get(moduleName);
        for (final NativeFunctionStatement supportedFunction : module.supportedFunctions(statement.name())) {
            final HslLibrary library = new HslLibrary(supportedFunction, this.externalModules);
            // TODO #780: Support overloading of functions in same module (or throw error if duplicate from other module)
            this.global.define(supportedFunction.name().lexeme(), library);
        }
        return null;
    }

    @Override
    public Void visit(final BreakStatement statement) {
        throw new MoveKeyword(MoveKeyword.MoveType.BREAK);
    }

    @Override
    public Void visit(final ContinueStatement statement) {
        throw new MoveKeyword(MoveKeyword.MoveType.CONTINUE);
    }

    @Override
    public Void visit(final FunctionStatement statement) {
        final VirtualFunction function = new VirtualFunction(statement, this.variableScope(), false);
        this.variableScope().define(statement.name().lexeme(), function);
        return null;
    }

    @Override
    public Void visit(final FieldStatement statement) {
        final Object value = this.evaluate(statement.initializer());
        final int distance = this.locals.get(statement.initializer());
        final PropertyContainer object = (PropertyContainer) this.variableScope().getAt(statement.name(), distance - 1, TokenType.THIS.representation());
        object.set(this, statement.name(), value, this.variableScope());

        this.execute(statement.getter());
        this.execute(statement.setter());

        return null;
    }

    @Override
    public Void visit(final FieldGetStatement statement) {
        this.visitFieldMember(statement, VirtualClassBuilder::getter);
        return null;
    }

    @Override
    public Void visit(final FieldSetStatement statement) {
        this.visitFieldMember(statement, VirtualClassBuilder::setter);
        return null;
    }

    private void visitFieldMember(final FieldMemberStatement statement, final BiConsumer<VirtualClassBuilder, VirtualMemberFunction> applyMember) {
        final VariableScope variableScope = this.variableScope();
        if (variableScope.owner() instanceof VirtualClassBuilder builder) {
            final VirtualMemberFunction function = new VirtualMemberFunction(statement, this.variableScope(), false, statement.field().name(), statement.modifier());
            applyMember.accept(builder, function);
        }
        else {
            throw new RuntimeError(statement.name(), DiagnosticMessage.ILLEGAL_FIELD_DEFINITION, statement.field().name());
        }
    }

    @Override
    public Void visit(final ConstructorStatement statement) {
        final VirtualFunction function = new VirtualFunction(statement, this.variableScope(), true);
        this.variableScope().define(statement.initializerIdentifier().lexeme(), function);
        return null;
    }

    @Override
    public Void visit(final ExtensionStatement statement) {
        final VirtualClass extensionClass = (VirtualClass) this.variableScope().get(statement.className());
        if (extensionClass == null) {
            throw new RuntimeError(statement.functionStatement().name(), DiagnosticMessage.MISSING_EXTENSION_CLASS, statement.className());
        }
        final FunctionStatement functionStatement = statement.functionStatement();
        final VirtualFunction extension = new VirtualFunction(functionStatement, extensionClass.variableScope(), false);
        if (extensionClass.method(functionStatement.name().lexeme()) != null) {
            throw new ScriptEvaluationError(Phase.INTERPRETING, statement.functionStatement().name(), DiagnosticMessage.DUPLICATE_METHOD, extensionClass.name(), functionStatement.name().lexeme());
        }
        extensionClass.extensionMethod(functionStatement.name().lexeme(), extension);
        return null;
    }

    @Override
    public Void visit(final SwitchStatement statement) {
        Object value = this.evaluate(statement.expression());
        value = this.unwrap(value);
        for (final SwitchCase switchCase : statement.cases()) {
            if (this.isEqual(value, switchCase.expression().value())) {
                this.execute(switchCase);
                return null;
            }
        }
        if (statement.defaultCase() != null) {
            this.withNextScope(() -> this.execute(statement.defaultCase()));
        }
        return null;
    }

    @Override
    public Void visit(final SwitchCase statement) {
        this.withNextScope(() -> {
            try {
                this.execute(statement.body());
            } catch (final MoveKeyword moveKeyword) {
                if (moveKeyword.moveType() != MoveKeyword.MoveType.BREAK) {
                    throw new RuntimeException("Unexpected move keyword " + moveKeyword.moveType());
                }
            }
        });
        return null;
    }

    private Object evaluate(final Expression expr) {
        return expr.accept(this);
    }

    private boolean truthy(Object object) {
        object = this.unwrap(object);
        if (object == null) return false;
        if (object instanceof Boolean) return (boolean) object;
        return true;
    }

    private boolean isEqual(final Object a, final Object b) {
        if (a == null && b == null) return true;
        if (a == null) return false;
        if (a instanceof Number na && b instanceof Number nb) {
            final BigDecimal ba = BigDecimal.valueOf(na.doubleValue());
            final BigDecimal bb = BigDecimal.valueOf(nb.doubleValue());
            return ba.compareTo(bb) == 0;
        }
        return a.equals(b);
    }

    private Object unwrap(final Object object) {
        if (object instanceof ExternalInstance external) return external.externalObject();
        return object;
    }

    private void checkNumberOperand(final Token operator, final Object operand) {
        if (operand instanceof Number) return;
        throw new RuntimeError(operator, DiagnosticMessage.NON_NUMBER_OPERAND, operand);
    }

    private void checkNumberOperands(final Token operator, final Object left, final Object right) {
        if (left instanceof Number && right instanceof Number) return;
        throw new RuntimeError(operator, DiagnosticMessage.OPERAND_MISMATCH, "number",
                left == null ? "null" : left.getClass().getSimpleName(),
                right == null ? "null" : right.getClass().getSimpleName()
        );
    }

    private float number(final Object left) {
        if (left instanceof Number) return ((Number) left).floatValue();
        return (float) left;
    }

    private void execute(final Statement stmt) {
        if (stmt != null) stmt.accept(this);
    }

    public void execute(final BlockStatement blockStatement, final VariableScope localVariableScope) {
        this.execute(blockStatement.statements(), localVariableScope);
    }

    public void execute(final List<Statement> statementList, final VariableScope localVariableScope) {
        final VariableScope previous = this.variableScope();
        try {
            // Make current scope block local and not global
            this.visitingScope = localVariableScope;

            for (final Statement statement : statementList) {
                try {
                    this.execute(statement);
                }
                catch (final MoveKeyword type) {
                    if (type.moveType() == MoveKeyword.MoveType.CONTINUE) {
                        break;
                    }
                    // Handle in higher visitor call
                    throw type;
                }
            }
        }
        finally {
            // 'Pop' scope from stack
            this.visitingScope = previous;
        }
    }

    private Object lookUpVariable(final Token name, final Expression expr) {
        if (name.type() == TokenType.THIS) {
            return this.variableScope().getAt(name, 1);
        }

        final Integer distance = this.locals.get(expr);
        if (distance != null) {
            // Find variable value in locales score
            return this.variableScope().getAt(name, distance);
        }
        else if (this.global.contains(name)) {
            // Can't find distance in locales, so it must be global variable
            return this.global.get(name);
        }
        else if (this.externalVariables.containsKey(name.lexeme())) {
            return this.externalVariables.get(name.lexeme());
        }
        else if (this.imports.containsKey(name.lexeme())) {
            return this.imports.get(name.lexeme());
        }

        throw new ScriptEvaluationError(Phase.INTERPRETING, name, DiagnosticMessage.UNDEFINED_VARIABLE, name.lexeme());
    }

    public void resolve(final Expression expr, final int depth) {
        this.locals.put(expr, depth);
    }

    public VariableScope variableScope() {
        return this.visitingScope;
    }

    public void global(final Map<String, Object> globalVariables) {
        globalVariables.forEach((name, instance) -> this.externalVariables.put(name, new ExternalInstance(instance)));
    }

    public void imports(final Map<String, Class<?>> imports) {
        imports.forEach((name, type) -> this.imports.put(name, new ExternalClass<>(type)));
    }

    private void withNextScope(final ScopeOwner owner, final Runnable runnable) {
        final VariableScope nextScope = new VariableScope(this.variableScope(), owner);
        final VariableScope previous = this.variableScope();
        this.visitingScope = nextScope;
        runnable.run();
        this.visitingScope = previous;
    }

    private void withNextScope(final Runnable runnable) {
        this.withNextScope(null, runnable);
    }
}
