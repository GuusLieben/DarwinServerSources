/*
 * Copyright (C) 2020 Guus Lieben
 *
 * This framework is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library. If not, see {@literal<http://www.gnu.org/licenses/>}.
 */

package org.dockbox.hartshorn.proxy;

import org.dockbox.hartshorn.di.context.element.TypeContext;
import org.dockbox.hartshorn.proxy.handle.ProxyHandler;
import org.dockbox.hartshorn.proxy.types.ConcreteProxyTarget;
import org.dockbox.hartshorn.proxy.types.FinalProxyTarget;
import org.dockbox.hartshorn.proxy.types.GlobalProxyTarget;
import org.dockbox.hartshorn.proxy.types.ProviderService;
import org.dockbox.hartshorn.proxy.types.SampleType;
import org.dockbox.hartshorn.test.ApplicationAwareTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;

public class ProxyTests extends ApplicationAwareTest {

    @Test
    void testConcreteMethodsCanBeProxied() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        final ProxyAttribute<ConcreteProxyTarget, String> property = ProxyAttribute.of(
                ConcreteProxyTarget.class,
                ConcreteProxyTarget.class.getMethod("name"),
                (instance, args, proxyContext) -> "Hartshorn");
        final ProxyHandler<ConcreteProxyTarget> handler = new ProxyHandler<>(new ConcreteProxyTarget());
        handler.delegate(property);
        final ConcreteProxyTarget proxy = handler.proxy();

        Assertions.assertNotNull(proxy);
        Assertions.assertNotNull(proxy.name());
        Assertions.assertEquals("Hartshorn", proxy.name());
    }

    @Test
    void testFinalMethodsCanNotBeProxied() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        final ProxyAttribute<FinalProxyTarget, String> property = ProxyAttribute.of(
                FinalProxyTarget.class,
                FinalProxyTarget.class.getMethod("name"),
                (instance, args, proxyContext) -> "Hartshorn");
        final ProxyHandler<FinalProxyTarget> handler = new ProxyHandler<>(new FinalProxyTarget());
        Assertions.assertThrows(RuntimeException.class, () -> handler.delegate(property));

        // Ensure the exception isn't thrown after registration
        final FinalProxyTarget proxy = handler.proxy();

        Assertions.assertNotNull(proxy);
        Assertions.assertNotNull(proxy.name());
        Assertions.assertNotEquals("Hartshorn", proxy.name());
        Assertions.assertEquals("NotHartshorn", proxy.name());
    }

    @Test
    void testProviderPropertiesAreApplied() throws NoSuchMethodException {
        final ProxyAttribute<ConcreteProxyTarget, String> property = ProxyAttribute.of(
                ConcreteProxyTarget.class,
                ConcreteProxyTarget.class.getMethod("name"),
                (instance, args, proxyContext) -> "Hartshorn");
        final ConcreteProxyTarget proxy = this.context().get(ConcreteProxyTarget.class, property);

        Assertions.assertNotNull(proxy);
        Assertions.assertNotNull(proxy.name());
        Assertions.assertEquals("Hartshorn", proxy.name());
    }

    @Test
    void testGlobalProxiesCanApply() {
        final GlobalProxyTarget target = this.context().get(GlobalProxyTarget.class);
        Assertions.assertTrue(TypeContext.of(target).isProxy());
        Assertions.assertEquals("GlobalHartshorn", target.name());
    }

    @Test
    void testProviderService() {
        final ProviderService service = this.context().get(ProviderService.class);
        final SampleType type = service.get();
        Assertions.assertNotNull(type);
    }
}
