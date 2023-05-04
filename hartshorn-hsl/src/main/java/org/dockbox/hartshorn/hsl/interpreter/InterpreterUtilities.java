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

package org.dockbox.hartshorn.hsl.interpreter;

import org.dockbox.hartshorn.hsl.objects.external.ExternalInstance;
import org.dockbox.hartshorn.hsl.runtime.RuntimeError;
import org.dockbox.hartshorn.hsl.token.Token;

import java.math.BigDecimal;

public final class InterpreterUtilities {

    public static boolean isTruthy(Object object) {
        object = InterpreterUtilities.unwrap(object);
        if (object == null) return false;
        if (object instanceof Boolean) return (boolean) object;
        return true;
    }

    public static boolean isEqual(final Object a, final Object b) {
        if (a == null && b == null) return true;
        if (a == null) return false;
        if (a instanceof Number na && b instanceof Number nb) {
            final BigDecimal ba = BigDecimal.valueOf(na.doubleValue());
            final BigDecimal bb = BigDecimal.valueOf(nb.doubleValue());
            return ba.compareTo(bb) == 0;
        }
        return a.equals(b);
    }

    public static Object unwrap(final Object object) {
        if (object instanceof ExternalInstance external) return external.instance();
        return object;
    }

    public static void checkNumberOperand(final Token operator, final Object operand) {
        if (operand instanceof Double) return;
        throw new RuntimeError(operator, "Operand must be a number.");
    }

    public static void checkNumberOperands(final Token operator, final Object left, final Object right) {
        if (left instanceof Number && right instanceof Number) return;
        throw new RuntimeError(operator, "Operands must the same type -> number.");
    }
}