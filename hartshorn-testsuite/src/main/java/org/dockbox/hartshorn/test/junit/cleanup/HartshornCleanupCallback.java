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

import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * A callback that cleans up resources after a test lifecycle has been completed.
 *
 * @since 0.7.0
 *
 * @author Guus Lieben
 */
public interface HartshornCleanupCallback {

    /**
     * Close resources after the test lifecycle has been completed.
     *
     * @param context the extension context of the test
     * @throws Exception if an error occurs while closing resources
     */
    void closeAfterLifecycle(ExtensionContext context) throws Exception;
}
