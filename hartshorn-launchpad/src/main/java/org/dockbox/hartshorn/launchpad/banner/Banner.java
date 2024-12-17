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

package org.dockbox.hartshorn.launchpad.banner;

import org.slf4j.Logger;

/**
 * Represents a banner that can be printed to the active logging system. Banners are usually printed
 * when the application starts.
 *
 * @see HartshornLogoBanner
 *
 * @since 0.4.13
 *
 * @author Guus Lieben
 */
@FunctionalInterface
public interface Banner {

    /**
     * Prints the banner to the given {@link Logger}. The logger is expected to be the primary logger
     * of the application.
     *
     * @param logger The logger to print the banner to.
     */
    void print(Logger logger);
}
