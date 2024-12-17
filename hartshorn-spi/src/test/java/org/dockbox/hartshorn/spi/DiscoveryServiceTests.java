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

package org.dockbox.hartshorn.spi;

import com.google.common.collect.ImmutableList;
import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DiscoveryServiceTests {

    private static final String IMPLEMENTATION_NAME = "HelloWorldServiceImplementation";
    private static final String IMPLEMENTATION_QUALIFIED_NAME = "%s.%s".formatted(DiscoveryServiceTests.class.getPackageName(), IMPLEMENTATION_NAME);
    private static final String HELLO_WORLD_MESSAGE = "Hello World!";
    private static final String HELLO_WORLD_SERVICE_IMPLEMENTATION_SOURCE = """
            package org.dockbox.hartshorn.spi;

            public class HelloWorldServiceImplementation implements HelloWorldService {
                        
                @Override
                public String getHelloWorld() {
                    return "%s";
                }
            }
            """.formatted(HELLO_WORLD_MESSAGE);

    @AfterEach
    void tearDown() throws NoSuchFieldException {
        Field discoveryService = DiscoveryService.class.getDeclaredField("DISCOVERY_SERVICE");
        discoveryService.setAccessible(true);
        try {
            discoveryService.set(null, null);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testDiscoveryServiceWithOverride() throws IOException, ServiceDiscoveryException {
        ByteClassLoader classLoader = createImplementationAwareClassLoader();

        DiscoveryService.instance().addClassLoader(classLoader);
        DiscoveryService.instance().override(HelloWorldService.class, IMPLEMENTATION_QUALIFIED_NAME);

        HelloWorldService helloWorldService = DiscoveryService.instance().discover(HelloWorldService.class);
        Assertions.assertEquals(HELLO_WORLD_MESSAGE, helloWorldService.getHelloWorld());
    }

    @NonNull
    private static ByteClassLoader createImplementationAwareClassLoader() throws IOException {
        JavaFileObject implementationClassFile = compileAndGetRuntimeImplementation();

        // Class is compiled, but not loaded, so we need to load it manually.
        return new ByteClassLoader(
                new URL[]{},
                DiscoveryServiceTests.class.getClassLoader(),
                Collections.singletonMap(IMPLEMENTATION_QUALIFIED_NAME, implementationClassFile.openInputStream().readAllBytes()));
    }

    @Test
    void testRegistryContainsTypeAfterRegistration() throws IOException {
        ByteClassLoader classLoader = createImplementationAwareClassLoader();
        DiscoveryService.instance().addClassLoader(classLoader);

        Assertions.assertFalse(DiscoveryService.instance().contains(HelloWorldService.class));

        DiscoveryService.instance().override(HelloWorldService.class, IMPLEMENTATION_QUALIFIED_NAME);
        Assertions.assertTrue(DiscoveryService.instance().contains(HelloWorldService.class));
    }

    @Test
    void testDiscoveryFailsIfNoImplementationExists() {
        Assertions.assertThrows(ServiceDiscoveryException.class, () -> DiscoveryService.instance().discover(HelloWorldService.class));
    }

    @Test
    void testDiscoveryFailsIfImplementationClassDoesNotExist() {
        Assertions.assertThrows(ServiceDiscoveryException.class, () -> {
            DiscoveryService.instance().override(HelloWorldService.class, "org.dockbox.hartshorn.spi.DoesNotExist");
            DiscoveryService.instance().discover(HelloWorldService.class);
        });
    }

    @Test
    void testOverrideFailsIfImplementationNotAssignable() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> DiscoveryService.instance().override(HelloWorldService.class, DiscoveryServiceTests.class));
    }

    @Test
    void testDiscoveryFailsIfImplementationHasNoDefaultConstructor() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            DiscoveryService.instance().override(HelloWorldService.class, HelloWorldServiceImplementationWithNoDefaultConstructor.class);
            DiscoveryService.instance().discover(HelloWorldService.class);
        });
    }

    private static JavaFileObject compileAndGetRuntimeImplementation() {
        Compilation compilation = Compiler.javac().compile(JavaFileObjects.forSourceString(IMPLEMENTATION_NAME, HELLO_WORLD_SERVICE_IMPLEMENTATION_SOURCE));
        Assertions.assertTrue(compilation.errors().isEmpty());

        ImmutableList<JavaFileObject> generatedFiles = compilation.generatedFiles();
        Assertions.assertEquals(1, generatedFiles.size());

        Map<Kind, List<JavaFileObject>> generatedByKind = generatedFiles.stream().collect(Collectors.groupingBy(JavaFileObject::getKind));
        Assertions.assertEquals(1, generatedByKind.get(Kind.CLASS).size()); // HelloWorldServiceImplementation.class

        return generatedByKind.get(Kind.CLASS).get(0);
    }

    public static class ByteClassLoader extends URLClassLoader {
        private final Map<String, byte[]> extraClassDefs;

        public ByteClassLoader(URL[] urls, ClassLoader parent, Map<String, byte[]> extraClassDefs) {
            super(urls, parent);
            this.extraClassDefs = Collections.unmodifiableMap(extraClassDefs);
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            byte[] classBytes = this.extraClassDefs.get(name);
            if (classBytes != null) {
                return this.defineClass(name, classBytes, 0, classBytes.length);
            }
            return super.findClass(name);
        }
    }

    public static class HelloWorldServiceImplementationWithNoDefaultConstructor implements HelloWorldService {
        private final String arg;

        public HelloWorldServiceImplementationWithNoDefaultConstructor(String arg) {
            this.arg = arg;
        }

        @Override
        public String getHelloWorld() {
            return this.arg;
        }
    }
}
