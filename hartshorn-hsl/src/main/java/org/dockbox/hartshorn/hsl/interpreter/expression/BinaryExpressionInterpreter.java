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

package org.dockbox.hartshorn.hsl.interpreter.expression;

import java.util.function.BiPredicate;

import org.dockbox.hartshorn.hsl.ScriptEvaluationError;
import org.dockbox.hartshorn.hsl.ast.expression.BinaryExpression;
import org.dockbox.hartshorn.hsl.interpreter.ASTNodeInterpreter;
import org.dockbox.hartshorn.hsl.interpreter.Array;
import org.dockbox.hartshorn.hsl.interpreter.Interpreter;
import org.dockbox.hartshorn.hsl.interpreter.InterpreterUtilities;
import org.dockbox.hartshorn.hsl.runtime.Phase;
import org.dockbox.hartshorn.hsl.token.Token;
import org.dockbox.hartshorn.hsl.token.type.ArithmeticTokenType;
import org.dockbox.hartshorn.hsl.token.type.ConditionTokenType;

/**
 * TODO: #1061 Add documentation
 *
 * @since 0.5.0
 *
 * @author Guus Lieben
 */
public class BinaryExpressionInterpreter implements ASTNodeInterpreter<Object, BinaryExpression> {

    @Override
    public Object interpret(BinaryExpression node, Interpreter interpreter) {
        Object left = interpreter.evaluate(node.leftExpression());
        Object right = interpreter.evaluate(node.rightExpression());

        left = InterpreterUtilities.unwrap(left);
        right = InterpreterUtilities.unwrap(right);

        Token operator = node.operator();
        return switch (operator.type()) {
            case ArithmeticTokenType.PLUS -> {
                // Math plus
                if (left instanceof Double leftDouble && right instanceof Double rightDouble) {
                    yield leftDouble + rightDouble;
                }
                // String Addition
                if (left instanceof String || right instanceof String) {
                    // String.valueOf to handle nulls
                    yield String.valueOf(left) + right;
                }

                // Special cases
                if (left instanceof Character && right instanceof Character) {
                    yield String.valueOf(left) + right;
                }
                if (left instanceof Character leftCharacter && right instanceof Double rightDouble) {
                    int value = leftCharacter;
                    yield rightDouble + value;
                }
                if (left instanceof Double leftDouble && right instanceof Character rightCharacter) {
                    int value = rightCharacter;
                    yield leftDouble + value;
                }
                throw new ScriptEvaluationError("Unsupported child for PLUS.\n", Phase.INTERPRETING, operator);
            }
            case ArithmeticTokenType.MINUS -> {
                InterpreterUtilities.checkNumberOperands(operator, left, right);
                yield (double) left - (double) right;
            }
            case ArithmeticTokenType.STAR -> {
                if ((left instanceof String || left instanceof Character) && right instanceof Double rightDouble) {
                    int times = rightDouble.intValue();
                    int length = left.toString().length() * times;
                    StringBuilder result = new StringBuilder(length);
                    String value = left.toString();
                    result.append(value.repeat(Math.max(0, times)));
                    yield result.toString();
                }
                else if (left instanceof Array array && right instanceof Double rightDouble) {
                    int times = rightDouble.intValue();
                    int length = array.length() * times;
                    Array result = new Array(length);
                    for (int i = 0; i < times; i++) {
                        int originalIndex = times % array.length();
                        result.value(array.value(originalIndex), i);
                    }
                    yield result;
                }
                InterpreterUtilities.checkNumberOperands(operator, left, right);
                yield (double) left * (double) right;
            }
            case ArithmeticTokenType.MODULO -> {
                InterpreterUtilities.checkNumberOperands(operator, left, right);
                yield (double) left % (double) right;
            }
            case ArithmeticTokenType.SLASH -> {
                InterpreterUtilities.checkNumberOperands(operator, left, right);
                if ((double) right == 0) {
                    throw new ScriptEvaluationError("Can't use slash with zero double.", Phase.INTERPRETING, operator);
                }
                yield (double) left / (double) right;
            }
            case ConditionTokenType.GREATER -> this.compareNumbers(node, left, right, (l, r) -> l > r);
            case ConditionTokenType.GREATER_EQUAL -> this.compareNumbers(node, left, right, (l, r) -> l >= r);
            case ConditionTokenType.LESS -> this.compareNumbers(node, left, right, (l, r) -> l < r);
            case ConditionTokenType.LESS_EQUAL -> this.compareNumbers(node, left, right, (l, r) -> l <= r);
            case ConditionTokenType.BANG_EQUAL -> !InterpreterUtilities.isEqual(left, right);
            case ConditionTokenType.EQUAL_EQUAL -> InterpreterUtilities.isEqual(left, right);
            default -> null;
        };
    }

    private boolean compareNumbers(BinaryExpression expression, Object left, Object right, BiPredicate<Double, Double> predicate) {
        InterpreterUtilities.checkNumberOperands(expression.operator(), left, right);
        return predicate.test(Double.parseDouble(left.toString()), Double.parseDouble(right.toString()));
    }
}
