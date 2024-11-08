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

package org.dockbox.hartshorn.test.junit.cleanup;

import java.lang.reflect.Method;

import org.dockbox.hartshorn.util.TypeUtils;
import org.dockbox.hartshorn.util.option.Option;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * A callback that clears the caches of Mockito after the test has been executed.
 *
 * @since 0.7.0
 *
 * @author Guus Lieben
 */
public class ClearMockitoCachesCallback implements HartshornCleanupCallback {

    @Override
    public void closeAfterLifecycle(ExtensionContext context) throws Exception {
        Option<Class<?>> mockito = TypeUtils.forName("org.mockito.Mockito");
        if (mockito.present()) {
            Method clearAllCaches = mockito.get().getMethod("clearAllCaches");
            clearAllCaches.invoke(null);
        }
    }
}
