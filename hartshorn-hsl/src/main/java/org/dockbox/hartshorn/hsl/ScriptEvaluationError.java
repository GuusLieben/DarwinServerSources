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

package org.dockbox.hartshorn.hsl;

import org.dockbox.hartshorn.hsl.ast.ASTNode;
import org.dockbox.hartshorn.hsl.runtime.Phase;

public class ScriptEvaluationError extends RuntimeException {

    private final Phase phase;
    private final int line;
    private final int column;
    private final ASTNode at;

    public ScriptEvaluationError(final String message, final Phase phase, final int line, final int column) {
        this(null, message, phase, null, line, column);
    }

    public ScriptEvaluationError(final String message, final Phase phase, final ASTNode at) {
        this(null, message, phase, at, at.line(), at.column());
    }

    public ScriptEvaluationError(final Throwable cause, final Phase phase, final ASTNode at) {
        this(cause, cause.getMessage(), phase, at, at.line(), at.column());
    }

    public ScriptEvaluationError(final Throwable cause, final String message, final Phase phase, final ASTNode at, final int line, final int column) {
        super(message, cause);
        this.phase = phase;
        this.at = at;
        this.line = line;
        this.column = column;
    }

    public ASTNode at() {
        return at;
    }

    public int line() {
        return line;
    }

    public int column() {
        return column;
    }

    public Phase phase() {
        return phase;
    }
}