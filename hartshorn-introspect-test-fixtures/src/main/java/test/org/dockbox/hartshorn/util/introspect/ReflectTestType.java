/*
 * Copyright 2019-2023 the original author or authors.
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

package test.org.dockbox.hartshorn.util.introspect;

import java.util.Locale;

import org.dockbox.hartshorn.util.introspect.annotations.Property;
import org.junit.jupiter.api.Assertions;

@Demo
@SuppressWarnings({ "FieldMayBeFinal", "unused" })
public class ReflectTestType extends ParentTestType {

    @Demo
    public static String publicStaticField = "publicStaticField";
    private static String privateStaticField = "privateStaticField";
    public final String finalPublicField = "finalPublicField";
    private final String finalPrivateField = "finalPrivateField";
    @Property(name = "propertyField")
    public String publicField = "publicField";
    @Demo
    private String privateField = "privateField";
    @Property(getter = "field", setter = "field")
    private String accessorField;

    /* Begin: Test utilities, do not read or edit these through introspection */
    private boolean activatedSetter;
    private boolean activatedMethod;
    private boolean activatedConstructor;

    public boolean activatedSetter() {
        return this.activatedSetter;
    }

    public boolean activatedMethod() {
        return this.activatedMethod;
    }

    public boolean activatedConstructor() {
        return this.activatedConstructor;
    }
    /* End: Test utilities */

    @Demo
    public ReflectTestType() {
        this.activatedConstructor = true;
    }

    public String field() {
        return "accessorField";
    }

    public void field(String value) {
        this.activatedSetter = true;
    }

    public String publicMethod(String argument) {
        Assertions.assertEquals("value", argument);
        return argument.toUpperCase(Locale.ROOT);
    }

    public String privateMethod(String argument) {
        Assertions.assertEquals("value", argument);
        return argument.toUpperCase(Locale.ROOT);
    }

    @Demo
    public void publicAnnotatedMethod() {}

    @Demo
    private void privateAnnotatedMethod() {}
}
