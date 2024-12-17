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

package test.org.dockbox.hartshorn.util.introspect;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.dockbox.hartshorn.util.CollectionUtilities;
import org.dockbox.hartshorn.util.TypeConversionException;
import org.dockbox.hartshorn.util.TypeUtils;
import org.dockbox.hartshorn.util.introspect.Introspector;
import org.dockbox.hartshorn.util.introspect.ParameterizableType;
import org.dockbox.hartshorn.util.introspect.TypeParametersIntrospector;
import org.dockbox.hartshorn.util.introspect.view.ConstructorView;
import org.dockbox.hartshorn.util.introspect.view.FieldView;
import org.dockbox.hartshorn.util.introspect.view.MethodView;
import org.dockbox.hartshorn.util.introspect.view.ParameterView;
import org.dockbox.hartshorn.util.introspect.view.TypeParameterView;
import org.dockbox.hartshorn.util.introspect.view.TypeView;
import org.dockbox.hartshorn.util.option.Option;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public abstract class IntrospectorTests {

    private static Stream<Arguments> fields() {
        return Stream.of(
                Arguments.of("privateField"),
                Arguments.of("publicField"),
                Arguments.of("finalPrivateField"),
                Arguments.of("finalPublicField"),
                Arguments.of("publicStaticField"),
                Arguments.of("privateStaticField"),
                Arguments.of("accessorField")
        );
    }

    private static Stream<Arguments> methods() {
        return Stream.of(
                Arguments.of("publicMethod"),
                Arguments.of("privateMethod")
        );
    }

    private static Stream<Arguments> assignablePrimitives() {
        return Stream.of(
                Arguments.of(boolean.class, Boolean.class),
                Arguments.of(byte.class, Byte.class),
                Arguments.of(char.class, Character.class),
                Arguments.of(double.class, Double.class),
                Arguments.of(float.class, Float.class),
                Arguments.of(int.class, Integer.class),
                Arguments.of(long.class, Long.class),
                Arguments.of(short.class, Short.class)
        );
    }

    private static Stream<Arguments> nonVoidTypes() {
        return Stream.of(
                Arguments.of(boolean.class),
                Arguments.of(Boolean.class),
                Arguments.of(byte.class),
                Arguments.of(Byte.class),
                Arguments.of(char.class),
                Arguments.of(Character.class),
                Arguments.of(double.class),
                Arguments.of(Double.class),
                Arguments.of(float.class),
                Arguments.of(Float.class),
                Arguments.of(int.class),
                Arguments.of(Integer.class),
                Arguments.of(long.class),
                Arguments.of(Long.class),
                Arguments.of(short.class),
                Arguments.of(Short.class),
                Arguments.of(String.class)
        );
    }

    private static Stream<Arguments> primitiveValues() {
        return Stream.of(
                Arguments.of(boolean.class, "true", true),
                Arguments.of(byte.class, "0", (byte) 0),
                Arguments.of(char.class, "a", 'a'),
                Arguments.of(double.class, "10.5", 10.5D),
                Arguments.of(float.class, "10.5", 10.5F),
                Arguments.of(int.class, "10", 10),
                Arguments.of(long.class, "10", 10L),
                Arguments.of(short.class, "10", (short) 10),
                Arguments.of(TestEnumType.class, "A", TestEnumType.A)
        );
    }

    protected abstract Introspector introspector();

    @ParameterizedTest
    @MethodSource("fields")
    void testFieldValueReturnsValue(String field) throws Throwable {
        ReflectTestType instance = new ReflectTestType();
        TypeView<ReflectTestType> type = this.introspector().introspect(instance);
        Option<?> value = type.fields().named(field).get().get(instance);
        Assertions.assertTrue(value.present());
        Assertions.assertEquals(field, value.get());
    }

    @ParameterizedTest
    @MethodSource("methods")
    void testRunMethodReturnsValue(String method) throws Throwable {
        ReflectTestType instance = new ReflectTestType();
        TypeView<ReflectTestType> type = this.introspector().introspect(instance);
        Option<?> value = type.methods().named(method, List.of(String.class)).get().invoke(instance, "value");
        Assertions.assertTrue(value.present());
        Assertions.assertEquals("VALUE", value.get());
    }

    @ParameterizedTest
    @MethodSource("assignablePrimitives")
    void testAssignableFromPrimitives(Class<?> primitive, Class<?> wrapper) {
        TypeView<?> pt = this.introspector().introspect(primitive);
        TypeView<?> wt = this.introspector().introspect(wrapper);
        Assertions.assertTrue(pt.isChildOf(wt.type()));
        Assertions.assertTrue(wt.isChildOf(pt.type()));
    }

    @Test
    void testAssignableFromSuper() {
        Assertions.assertTrue(this.introspector().introspect(ReflectTestType.class).isChildOf(ParentTestType.class));
    }

    @Test
    void testAssignableFromSame() {
        Assertions.assertTrue(this.introspector().introspect(ReflectTestType.class).isChildOf(ReflectTestType.class));
    }

    @Test
    void testAssignableFromChild() {
        Assertions.assertFalse(this.introspector().introspect(ParentTestType.class).isChildOf(ReflectTestType.class));
    }

    @Test
    void testAnnotatedMethodsReturnsAllModifiers() {
        TypeView<ReflectTestType> type = this.introspector().introspect(ReflectTestType.class);
        List<MethodView<ReflectTestType, ?>> methods = type.methods().annotatedWith(Demo.class);
        Assertions.assertEquals(3, methods.size());

        List<String> names = methods.stream().map(MethodView::name).toList();
        Assertions.assertTrue(names.contains("publicAnnotatedMethod"));
        Assertions.assertTrue(names.contains("privateAnnotatedMethod"));
    }

    @Test
    void testStaticFieldsReturnsAllModifiers() {
        List<FieldView<ReflectTestType, ?>> fields = this.introspector().introspect(ReflectTestType.class).fields().all().stream()
                .filter(field -> field.modifiers().isStatic())
                .toList();
        Assertions.assertEquals(2, fields.size());
    }

    @Test
    void testHasAnnotationOnMethod() {
        Option<MethodView<ReflectTestType, ?>> method = this.introspector().introspect(ReflectTestType.class)
                .methods()
                .named("publicAnnotatedMethod");
        Assertions.assertTrue(method.present());
        Assertions.assertTrue(method.get().annotations().has(Demo.class));
    }

    @Test
    void testSuperTypesReturnsAllSuperTypesWithoutObject() {
        TypeView<?> parent = this.introspector().introspect(ReflectTestType.class).superClass();
        Assertions.assertFalse(parent.isVoid());
        Assertions.assertSame(ParentTestType.class, parent.type());
    }

    @Test
    void testMethodsReturnsAllDeclaredAndParentMethods() {
        TypeView<ReflectTestType> type = this.introspector().introspect(ReflectTestType.class);
        List<MethodView<ReflectTestType, ?>> methods = type.methods().all();
        boolean fail = true;
        for (MethodView<ReflectTestType, ?> method : methods) {
            if ("parentMethod".equals(method.name())) {
                fail = false;
            }
        }
        if (fail) {
            Assertions.fail("Parent types were not included");
        }
    }

    @Test
    void testTypeContextMethodsDoNotIncludeBridgeMethods() {
        TypeView<BridgeElement> bridge = this.introspector().introspect(BridgeElement.class);
        List<MethodView<BridgeElement, ?>> methods = bridge.methods().all();
        for (MethodView<BridgeElement, ?> method : methods) {
            Option<Method> nativeMethod = method.method();
            Assertions.assertTrue(nativeMethod.present());
            Assertions.assertFalse(nativeMethod.get().isBridge());
        }
    }

    @Test
    void testTypeContextBridgeMethodsCanBeObtained() {
        TypeView<BridgeElement> bridge = this.introspector().introspect(BridgeElement.class);
        List<MethodView<BridgeElement, ?>> methods = bridge.methods().bridges();
        Assertions.assertEquals(1, methods.size());
        Assertions.assertSame(Object.class, methods.get(0).returnType().type());
        Assertions.assertTrue(methods.get(0).method().present());
        Assertions.assertTrue(methods.get(0).method().get().isBridge());
    }

    @Test
    void testLookupReturnsClassIfPresent() {
        TypeView<?> lookup = this.introspector().introspect(ReflectTestType.class.getName());
        Assertions.assertNotNull(lookup);
        Assertions.assertSame(ReflectTestType.class, lookup.type());
    }

    @Test
    void testLookupReturnsVoidIfAbsent() {
        TypeView<?> lookup = this.introspector().introspect("org.dockbox.hartshorn.util.AnotherClass");
        Assertions.assertTrue(lookup.isVoid());
    }

    @Test
    void testHasMethodIsTrueIfMethodExists() {
        Assertions.assertTrue(this.introspector().introspect(ReflectTestType.class)
                .methods()
                .named("publicMethod", String.class)
                .present());
    }

    @Test
    void testHasMethodIsFalseIfMethodDoesNotExist() {
        Assertions.assertFalse(this.introspector().introspect(ReflectTestType.class)
                .methods()
                .named("otherMethod")
                .present());
    }

    @Test
    void testInstanceHasMethodIsTrueIfMethodExists() {
        Assertions.assertTrue(this.introspector().introspect(new ReflectTestType())
                .methods()
                .named("publicMethod", String.class)
                .present());
    }

    @Test
    void testInstanceHasMethodIsFalseIfMethodDoesNotExist() {
        Assertions.assertFalse(this.introspector().introspect(new ReflectTestType())
                .methods()
                .named("otherMethod")
                .present());
    }

    @ParameterizedTest
    @MethodSource("nonVoidTypes")
    void testVoidIsFalseIfTypeIsNotVoid(Class<?> type) {
        Assertions.assertFalse(this.introspector().introspect(type).isVoid());
    }

    @Test
    void testVoidIsTrueIfTypeIsVoid() {
        Assertions.assertTrue(this.introspector().introspect(Void.class).isVoid());
    }

    @Test
    void testVoidIsTrueIfTypeIsVoidPrimitive() {
        Assertions.assertTrue(this.introspector().introspect(void.class).isVoid());
    }

    @ParameterizedTest
    @MethodSource("fields")
    void testHasFieldReturnsTrue(String field) {
        Assertions.assertTrue(this.introspector().introspect(ReflectTestType.class)
                .fields()
                .named(field)
                .present());
    }

    @ParameterizedTest
    @MethodSource("fields")
    void testFieldsConsumesAllFields(String field) {
        boolean activated = false;
        TypeView<ReflectTestType> type = this.introspector().introspect(ReflectTestType.class);
        for (FieldView<ReflectTestType, ?> fieldView : type.fields().all()) {
            if (fieldView.name().equals(field)) {
                activated = true;
            }
        }
        Assertions.assertTrue(activated);
    }

    @Test
    void testSetFieldUpdatesAccessorField() throws Throwable {
        Field fieldRef = ReflectTestType.class.getDeclaredField("accessorField");
        FieldView<?, ?> field = this.introspector().introspect(fieldRef);
        ReflectTestType instance = new ReflectTestType();
        field.set(instance, "newValue");

        Assertions.assertTrue(instance.activatedSetter());
    }

    @Test
    void testSetFieldUpdatesNormalField() throws Throwable {
        Field fieldRef = ReflectTestType.class.getDeclaredField("publicField");
        FieldView<?, ?> field = this.introspector().introspect(fieldRef);
        ReflectTestType instance = new ReflectTestType();
        field.set(instance, "newValue");

        Assertions.assertEquals("newValue", instance.publicField);
    }

    @Test
    void testAnnotatedFieldsIncludesStatic() {
        List<FieldView<ReflectTestType, ?>> fields = this.introspector().introspect(ReflectTestType.class).fields().annotatedWith(Demo.class);
        Assertions.assertEquals(2, fields.size());
        int statics = 0;
        for (FieldView<ReflectTestType, ?> field : fields) {
            if (field.modifiers().isStatic()) {
                statics++;
            }
        }
        Assertions.assertEquals(1, statics);
    }

    @Test
    void testAnnotatedConstructors() {
        TypeView<ReflectTestType> type = this.introspector().introspect(ReflectTestType.class);
        List<ConstructorView<ReflectTestType>> constructors = type.constructors().annotatedWith(Demo.class);
        Assertions.assertEquals(1, constructors.size());
    }

    @ParameterizedTest
    @MethodSource("primitiveValues")
    void testStringToPrimitive(Class<?> type, String value, Object expected) throws TypeConversionException {
        Object o = TypeUtils.toPrimitive(type, value);
        Assertions.assertNotNull(o);
        Assertions.assertEquals(expected, o);
    }

    @Test
    void testRedefinedAnnotationsTakePriority() {
        TypeView<AnnotatedObject> typeContext = this.introspector().introspect(AnnotatedObject.class);
        Option<Base> annotation = typeContext.annotations().get(Base.class);
        Assertions.assertTrue(annotation.present());
        Assertions.assertEquals("impl", annotation.get().value());
    }

    @Test
    public void genericTypeTests() {
        ParameterView<?> parameter = this.introspector().introspect(this)
                .methods()
                .named("genericTestMethod", List.class)
                .get()
                .parameters()
                .at(0)
                .orNull();

        TypeView<?> first = parameter.genericType();

        Assertions.assertTrue(first.is(List.class));
        Assertions.assertEquals(1, first.typeParameters().allInput().count());

        TypeView<?> second = first.typeParameters().atIndex(0)
                .orElseGet(Assertions::fail)
                .resolvedType().orNull();
        Assertions.assertNotNull(second);
        Assertions.assertTrue(second.is(List.class));
        Assertions.assertEquals(1, second.typeParameters().allInput().count());

        TypeView<?> third = second.typeParameters().atIndex(0)
                .orElseGet(Assertions::fail)
                .resolvedType().orNull();
        Assertions.assertNotNull(third);
        Assertions.assertTrue(third.is(String.class));
        Assertions.assertEquals(0, third.typeParameters().allInput().count());
    }

    @SuppressWarnings("unused") // Used by genericTypeTests
    public void genericTestMethod(List<List<String>> nestedGeneric) { }

    @SuppressWarnings("unused") // Used by testWildcardsWithUpperBounds
    public void methodWithWildcardUpperbounds(List<String> list) { }

    @Test
    void testWildcardsWithUpperBounds() {
        ParameterView<?> parameter = this.introspector().introspect(this)
                .methods()
                .named("methodWithWildcardUpperbounds", List.class)
                .get()
                .parameters()
                .at(0)
                .orNull();

        Assertions.assertNotNull(parameter);
        TypeView<?> first = parameter.genericType();

        Assertions.assertTrue(first.is(List.class));
        Assertions.assertEquals(1, first.typeParameters().allInput().count());

        TypeView<?> second = first.typeParameters().atIndex(0)
                .orElseGet(Assertions::fail)
                .resolvedType().orNull();
        Assertions.assertNotNull(second);
        Assertions.assertTrue(second.is(String.class));
        Assertions.assertFalse(second.isWildcard());
    }

    @Test
    void concreteClassIsCorrectlyIdentified() {
        TypeView<ConcreteClass> type = this.introspector().introspect(ConcreteClass.class);
        Assertions.assertFalse(type.modifiers().isAbstract());
        Assertions.assertFalse(type.isInterface());
        Assertions.assertFalse(type.isRecord());
        Assertions.assertFalse(type.isEnum());
        Assertions.assertFalse(type.isAnnotation());
    }

    @Test
    void abstractClassIsCorrectlyIdentified() {
        TypeView<AbstractClass> type = this.introspector().introspect(AbstractClass.class);
        Assertions.assertTrue(type.modifiers().isAbstract());
        Assertions.assertFalse(type.isInterface());
        Assertions.assertFalse(type.isRecord());
        Assertions.assertFalse(type.isEnum());
        Assertions.assertFalse(type.isAnnotation());
    }

    @Test
    void interfaceIsCorrectlyIdentified() {
        TypeView<Interface> type = this.introspector().introspect(Interface.class);
        Assertions.assertTrue(type.modifiers().isAbstract());
        Assertions.assertTrue(type.isInterface());
        Assertions.assertFalse(type.isRecord());
        Assertions.assertFalse(type.isEnum());
        Assertions.assertFalse(type.isAnnotation());
    }

    @Test
    void recordIsCorrectlyIdentified() {
        TypeView<RecordType> type = this.introspector().introspect(RecordType.class);
        Assertions.assertFalse(type.modifiers().isAbstract());
        Assertions.assertFalse(type.isInterface());
        Assertions.assertTrue(type.isRecord());
        Assertions.assertFalse(type.isEnum());
        Assertions.assertFalse(type.isAnnotation());
    }

    @Test
    void enumIsCorrectlyIdentified() {
        TypeView<EnumType> type = this.introspector().introspect(EnumType.class);
        Assertions.assertFalse(type.modifiers().isAbstract());
        Assertions.assertFalse(type.isInterface());
        Assertions.assertFalse(type.isRecord());
        Assertions.assertTrue(type.isEnum());
        Assertions.assertFalse(type.isAnnotation());
    }

    @Test
    void annotationIsCorrectlyIdentified() {
        TypeView<AnnotationType> type = this.introspector().introspect(AnnotationType.class);
        Assertions.assertTrue(type.modifiers().isAbstract());
        Assertions.assertTrue(type.isInterface());
        Assertions.assertFalse(type.isRecord());
        Assertions.assertFalse(type.isEnum());
        Assertions.assertTrue(type.isAnnotation());
    }

    private static class ConcreteClass {}
    private abstract static class AbstractClass {}
    private interface Interface {}
    private record RecordType() {}
    private enum EnumType {}
    private @interface AnnotationType {}

    @Test
    void testGenericTypeWithWildcardUsesUpperbounds() {
        Option<FieldView<IntrospectorTests, ?>> field = this.introspector().introspect(this).fields().named("genericType");
        Assertions.assertTrue(field.present());

        TypeParametersIntrospector parametersIntrospector = field.get().genericType().typeParameters();
        Assertions.assertEquals(1, parametersIntrospector.allInput().count());

        TypeParameterView typeParameter = parametersIntrospector.atIndex(0)
                .orElseGet(Assertions::fail);

        TypeView<?> parameter = typeParameter
                .resolvedType().orNull();
        Assertions.assertTrue(parameter.isWildcard());

        Set<TypeView<?>> upperBounds = typeParameter.upperBounds();
        Assertions.assertEquals(1, upperBounds.size());
        Assertions.assertSame(Object.class, CollectionUtilities.first(upperBounds).type());
    }

    @SuppressWarnings("unused") // Used by testGenericTypeWithWildcardUsesUpperbounds
    private final List<?> genericType = new ArrayList<>();

    @Test
    void testParameterizableTypeCanBeIntrospected() {
        ParameterizableType argumentType = ParameterizableType.create(String.class);
        ParameterizableType collectionType = ParameterizableType.builder(List.class)
                .parameters(argumentType)
                .build();

        TypeView<?> typeView = this.introspector().introspect(collectionType);
        Assertions.assertTrue(typeView.is(List.class));
        Assertions.assertEquals(1, typeView.typeParameters().allInput().count());

        TypeParameterView typeParameterView = typeView.typeParameters().atIndex(0)
                .orElseGet(Assertions::fail);

        TypeView<?> argumentView = typeParameterView.resolvedType()
                .orElseGet(Assertions::fail);

        Assertions.assertTrue(argumentView.is(String.class));
    }
}
