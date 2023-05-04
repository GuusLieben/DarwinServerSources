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

package org.dockbox.hartshorn.hsl.interpreter.statement;

import org.dockbox.hartshorn.hsl.ast.MoveKeyword;
import org.dockbox.hartshorn.hsl.ast.statement.RepeatStatement;
import org.dockbox.hartshorn.hsl.interpreter.ASTNodeInterpreter;
import org.dockbox.hartshorn.hsl.interpreter.InterpreterAdapter;

public class RepeatStatementInterpreter implements ASTNodeInterpreter<Void, RepeatStatement> {

    @Override
    public Void interpret(final RepeatStatement node, final InterpreterAdapter adapter) {
        adapter.withNextScope(() -> {
            final Object value = adapter.evaluate(node.value());

            final boolean isNotNumber = !(value instanceof Number);

            if (isNotNumber) {
                throw new RuntimeException("Repeat Counter must be number");
            }

            final int counter = (int) Double.parseDouble(value.toString());
            for (int i = 0; i < counter; i++) {
                try {
                    adapter.execute(node.body());
                }
                catch (final MoveKeyword moveKeyword) {
                    if (moveKeyword.moveType() == MoveKeyword.MoveType.BREAK) break;
                }
            }
        });
        return null;
    }
}