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

package test.org.dockbox.hartshorn.launchpad.launch.scanning;

import org.dockbox.hartshorn.inject.annotations.Inject;
import org.dockbox.hartshorn.proxy.ProxyOrchestrator;
import org.dockbox.hartshorn.test.annotations.TestComponents;
import org.dockbox.hartshorn.test.junit.HartshornIntegrationTest;
import org.dockbox.hartshorn.util.introspect.scan.TypeReferenceCollector;
import org.dockbox.hartshorn.util.introspect.scan.TypeReferenceCollectorContext;
import org.dockbox.hartshorn.util.introspect.scan.classpath.ClasspathTypeReferenceCollector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import test.org.dockbox.hartshorn.launchpad.launch.scanning.discover.ConcreteDiscoverableComponent;
import test.org.dockbox.hartshorn.launchpad.launch.scanning.discover.DiscoverableComponentConfiguration;
import test.org.dockbox.hartshorn.launchpad.launch.scanning.discover.DiscoverableComponent;
import test.org.dockbox.hartshorn.launchpad.launch.scanning.discover.ServiceInterface;
import test.org.dockbox.hartshorn.launchpad.launch.scanning.discover.PackageScanningActivator;

@PackageScanningActivator
@HartshornIntegrationTest(includeBasePackages = false)
public class ActivatorScanningTests {

    @Test
    void testPrefixFromActivatorIsRegistered(@Inject TypeReferenceCollectorContext context) {
        for (TypeReferenceCollector collector : context.collectors()) {
            if(collector instanceof ClasspathTypeReferenceCollector referenceCollector
                    && PackageScanningActivator.PACKAGE.equals(referenceCollector.packageName())) {
                return;
            }
        }
        Assertions.fail("No collector found for package %s".formatted(PackageScanningActivator.PACKAGE));
    }

    @Test
    @TestComponents(components = DiscoverableComponentConfiguration.class)
    void testBindingsFromActivatorPrefixArePresent(@Inject DiscoverableComponent component) {
        Assertions.assertNotNull(component);
        Assertions.assertEquals("Demo", component.message());
        Assertions.assertInstanceOf(ConcreteDiscoverableComponent.class, component);
    }

    @Test
    @TestComponents(components = ServiceInterface.class)
    void testServicesFromActivatorPrefixArePresent(@Inject ServiceInterface service, @Inject ProxyOrchestrator proxyOrchestrator) {
        Assertions.assertNotNull(service);
        Assertions.assertTrue(proxyOrchestrator.isProxy(service));
    }
}
