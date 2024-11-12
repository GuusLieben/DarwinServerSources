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

package org.dockbox.hartshorn.test.junit;

import org.dockbox.hartshorn.launchpad.ApplicationContext;
import org.dockbox.hartshorn.test.junit.cleanup.ClearMockitoCachesCallback;
import org.dockbox.hartshorn.test.junit.cleanup.HartshornCleanupCallback;
import org.dockbox.hartshorn.util.option.Option;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * A callback that cleans up resources after a test lifecycle has been completed. On its own this callback only
 * closes the {@link ApplicationContext}. The callback can be extended to add additional cleanup logic by registering
 * additional {@link HartshornCleanupCallback} instances to the extension context through {@link
 * HartshornJUnitNamespace#registerCleanupCallback(HartshornCleanupCallback, ExtensionContext)}.
 *
 * @see HartshornCleanupCallback
 * @see HartshornJUnitNamespace
 * @see ClearMockitoCachesCallback
 *
 * @since 0.7.0
 *
 * @author Guus Lieben
 */
public class HartshornJUnitCleanupCallback implements BeforeTestExecutionCallback, AfterAllCallback, AfterEachCallback {

    @Override
    public void beforeTestExecution(ExtensionContext context) throws Exception {
        HartshornJUnitNamespace.registerCleanupCallback(new ClearMockitoCachesCallback(), context);
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        this.afterLifecycle(context);
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        if (JUnitTestUtilities.isClassLifecycle(context)) {
            this.afterLifecycle(context);
        }
    }

    private void afterLifecycle(ExtensionContext context) throws Exception {
        for(HartshornCleanupCallback callback : HartshornJUnitNamespace.collectCleanupCallbacks(context)) {
            callback.closeAfterLifecycle(context);
        }
        // Always last, in case the application is used by the callbacks
        this.tryCloseApplication(context);
    }

    private void tryCloseApplication(ExtensionContext context) throws Exception {
        Option<AutoCloseable> closeableApplication = HartshornJUnitNamespace
                .applicationIfPresent(context)
                .ofType(AutoCloseable.class);

        if (closeableApplication.present()) {
            closeableApplication.get().close();
        }
    }
}
