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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A simple implementation of {@link ApplicationArgumentParser} that parses arguments in the form of
 * {@code --key=value}. Keys are expected to be alphanumeric, and may contain the characters {@code .}, {@code :},
 * {@code _} and {@code -}. Values may be any string of any length (including empty). Arguments are not trimmed.
 *
 * @since 0.4.11
 * @author Guus Lieben
 */
public class StandardApplicationArgumentParser implements ApplicationArgumentParser {

    private static final Pattern ARGUMENTS = Pattern.compile("--(.+)=(.*)");

    @Override
    public Properties parse(List<String> arguments) {
        Properties properties = new Properties();
        for (String arg : arguments) {
            Matcher matcher = ARGUMENTS.matcher(arg);
            if (matcher.find()) {
                properties.put(matcher.group(1), matcher.group(2));
            }
        }
        return properties;
    }
}
