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

package org.dockbox.hartshorn.launchpad.environment;

import java.util.List;
import java.util.Properties;

/**
 * Argument parsers are responsible for parsing the arguments passed to the application. The arguments are typically
 * passed to the application as command line arguments, but may also be passed in other ways. The arguments are parsed
 * into a {@link Properties} object, which is typically passed to the {@link ApplicationEnvironment} as part of the
 * bootstrap process.
 *
 * @since 0.4.9
 * @author Guus Lieben
 */
@FunctionalInterface
public interface ApplicationArgumentParser {

    /**
     * Parses the provided arguments into a {@link Properties} object. The expected form of the arguments is
     * determined by the implementation. The returned {@link Properties} object is expected to contain the parsed
     * arguments as key-value pairs, but can be empty if no matching arguments were provided.
     *
     * @param arguments The arguments to parse
     * @return The parsed arguments
     */
    Properties parse(List<String> arguments);
}
