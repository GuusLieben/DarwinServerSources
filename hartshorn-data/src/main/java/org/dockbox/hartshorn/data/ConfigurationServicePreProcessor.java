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

package org.dockbox.hartshorn.data;

import org.dockbox.hartshorn.core.Key;
import org.dockbox.hartshorn.core.annotations.activate.AutomaticActivation;
import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.dockbox.hartshorn.core.services.ComponentPreProcessor;
import org.dockbox.hartshorn.core.services.ProcessingOrder;
import org.dockbox.hartshorn.data.annotations.Configuration;
import org.dockbox.hartshorn.data.annotations.UseConfigurations;
import org.dockbox.hartshorn.data.mapping.ObjectMapper;

import java.io.File;
import java.net.URI;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Processes all services annotated with {@link Configuration} by loading the indicated file and registering the
 * properties to {@link ApplicationContext#property(String, Object)}. To support different file sources
 * {@link ResourceLookupStrategy strategies} are used. Each strategy is able to define behavior specific to sources
 * defined with its name. Strategies can be indicated in the {@link Configuration#value()} of a {@link Configuration}
 * in the format {@code strategy_name:source_name}. If a strategy is not registered, or no name is defined, behavior
 * defaults to {@link FileSystemLookupStrategy}.
 */
@AutomaticActivation
public class ConfigurationServicePreProcessor implements ComponentPreProcessor<UseConfigurations> {

    private final Pattern STRATEGY_PATTERN = Pattern.compile("(.+):(.+)");
    private final Map<String, ResourceLookupStrategy> strategies = new ConcurrentHashMap<>();

    public ConfigurationServicePreProcessor() {
        this.addStrategy(new ClassPathResourceLookupStrategy());
        this.addStrategy(new FileSystemLookupStrategy());
    }

    public void addStrategy(final ResourceLookupStrategy strategy) {
        this.strategies.put(strategy.name(), strategy);
    }

    @Override
    public Class<UseConfigurations> activator() {
        return UseConfigurations.class;
    }

    @Override
    public boolean modifies(final ApplicationContext context, final Key<?> key) {
        return key.type().annotation(Configuration.class).present();
    }

    @Override
    public <T> void process(final ApplicationContext context, final Key<T> key) {
        final Configuration configuration = key.type().annotation(Configuration.class).get();

        final String[] sources = configuration.value();

        for (final String source : sources) {
            if (this.processSource(source, context, key)) return;
            context.log().debug("Skipped configuration source '{}', proceeding to next source if available", source);
        }

        if (configuration.failOnMissing()) {
            throw new IllegalStateException("None of the configured sources in " + key.type().name() + " were found");
        } else {
            context.log().warn("None of the configured sources in {} were found, proceeding without configuration", key.type().name());
        }
    }

    private <T> boolean processSource(final String source, final ApplicationContext context, final Key<T> key) {
        String matchedSource = source;
        final Matcher matcher = this.STRATEGY_PATTERN.matcher(matchedSource);
        ResourceLookupStrategy strategy = new FileSystemLookupStrategy();
        if (matcher.find()) {
            strategy = this.strategies.getOrDefault(matcher.group(1), strategy);
            matchedSource = matcher.group(2);
        }
        context.log().debug("Determined strategy " + TypeContext.of(strategy).name() + " for " + matchedSource + ", declared by " + key.type().name());

        final Set<URI> config = strategy.lookup(context, matchedSource);
        if (config.isEmpty()) {
            context.log().warn("No configuration file found for " + key.type().name());
            return false;
        }
        else if (config.size() > 1) {
            context.log().warn("Found multiple configuration files for " + key.type().name() + ": " + config);
        }

        for (final URI uri : config) {
            final FileFormat format = this.lookupFileFormat(uri, matchedSource, context, strategy);

            if (format == null) {
                context.log().error("Unknown file format: " + matchedSource + ", declared by " + key.type().name());
                return false;
            }

            final Map<String, Object> cache = context.get(ObjectMapper.class)
                    .fileType(format)
                    .flat(uri);

            context.log().debug("Located " + cache.size() + " properties in " + uri.getPath());
            context.properties(cache);
        }
        return true;
    }

    protected FileFormat lookupFileFormat(final URI uri, final String source, final ApplicationContext context, final ResourceLookupStrategy strategy) {
        // If the source has a file extension, use that
        final FileFormats lookup = FileFormats.lookup(source);
        if (lookup != null) {
            return lookup;
        }
        else {
            final String fileName = new File(uri).getName();
            return FileFormats.lookup(fileName);
        }
    }

    @Override
    public Integer order() {
        return ProcessingOrder.FIRST;
    }
}