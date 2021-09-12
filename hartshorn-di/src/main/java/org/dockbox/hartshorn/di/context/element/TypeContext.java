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

package org.dockbox.hartshorn.di.context.element;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import org.dockbox.hartshorn.api.domain.Exceptional;
import org.dockbox.hartshorn.api.domain.tuple.Tristate;
import org.dockbox.hartshorn.api.exceptions.ApplicationException;
import org.dockbox.hartshorn.di.GenericType;
import org.dockbox.hartshorn.di.NotPrimitiveException;
import org.dockbox.hartshorn.di.TypeConversionException;
import org.dockbox.hartshorn.di.annotations.inject.Bound;
import org.dockbox.hartshorn.di.context.ApplicationContext;
import org.dockbox.hartshorn.util.HartshornUtils;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;

import javassist.util.proxy.ProxyFactory;
import lombok.Getter;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class TypeContext<T> extends AnnotatedElementContext<Class<T>> {

    private static final Map<Class<?>, TypeContext<?>> CACHE = HartshornUtils.emptyConcurrentMap();

    private static final Map<Class<?>, Class<?>> PRIMITIVE_WRAPPERS = HartshornUtils.ofEntries(
            HartshornUtils.entry(boolean.class, Boolean.class),
            HartshornUtils.entry(byte.class, Byte.class),
            HartshornUtils.entry(char.class, Character.class),
            HartshornUtils.entry(double.class, Double.class),
            HartshornUtils.entry(float.class, Float.class),
            HartshornUtils.entry(int.class, Integer.class),
            HartshornUtils.entry(long.class, Long.class),
            HartshornUtils.entry(short.class, Short.class)
    );
    private static final Map<?, Function<String, ?>> PRIMITIVE_FROM_STRING = HartshornUtils.ofEntries(
            HartshornUtils.entry(boolean.class, Boolean::valueOf),
            HartshornUtils.entry(byte.class, Byte::valueOf),
            HartshornUtils.entry(char.class, s -> s.charAt(0)),
            HartshornUtils.entry(double.class, Double::valueOf),
            HartshornUtils.entry(float.class, Float::valueOf),
            HartshornUtils.entry(int.class, Integer::valueOf),
            HartshornUtils.entry(long.class, Long::valueOf),
            HartshornUtils.entry(short.class, Short::valueOf)
    );
    private static final List<Class<?>> NATIVE_SUPPORTED = HartshornUtils.asList(
            boolean.class, byte.class, short.class,
            int.class, long.class, float.class, double.class,
            byte[].class, int[].class, long[].class,
            String.class, List.class, Map.class
    );

    public static TypeContext<Void> VOID = TypeContext.of(Void.class);

    @Getter private final Class<T> type;
    @Getter private final boolean isVoid;
    @Getter private final boolean isAnonymous;
    @Getter private final boolean isPrimitive;
    @Getter private final boolean isEnum;
    @Getter private final boolean isAnnotation;

    private final Map<String, FieldContext<?>> fields = HartshornUtils.emptyConcurrentMap();

    @Nullable
    private Boolean isNative;
    private List<T> enumConstants;
    private TypeContext<?> parent;
    private List<TypeContext<?>> interfaces;
    private List<MethodContext<?, T>> flatMethods;
    private List<TypeContext<?>> typeParameters;
    private List<ConstructorContext<T>> constructors;
    private Multimap<String, MethodContext<?, T>> methods;
    private Exceptional<ConstructorContext<T>> defaultConstructor;
    private Tristate isProxy = Tristate.UNDEFINED;

    protected TypeContext(final Class<T> type) {
        if (TypeContext.class.equals(type)) {
            throw new IllegalArgumentException("TypeContext can not be reflected on");
        }
        this.type = type;
        this.isVoid = Void.TYPE.equals(type) || Void.class.equals(type);
        this.isAnonymous = type.isAnonymousClass();
        this.isPrimitive = type.isPrimitive();
        this.isEnum = type.isEnum();
        this.isAnnotation = type.isAnnotation();
    }

    public static <T> TypeContext<T> unproxy(final ApplicationContext context, final T instance) {
        if (instance == null) {
            return (TypeContext<T>) VOID;
        }
        if (isProxy(instance.getClass())) {
            return context.environment().application().real(instance)
                    .orThrow(() -> new ApplicationException("Could not derive real type of instance " + instance).runtime());
        }
        else return of(instance);
    }

    public static <T> TypeContext<T> of(final T instance) {
        if (instance == null) {
            return (TypeContext<T>) VOID;
        }
        return of((Class<T>) instance.getClass());
    }

    public static <T> TypeContext<T> of(final Class<T> type) {
        if (type == null) {
            return (TypeContext<T>) VOID;
        }
        if (CACHE.containsKey(type))
            return (TypeContext<T>) CACHE.get(type);

        final TypeContext<T> context = new TypeContext<>(type);
        CACHE.put(type, context);
        return context;
    }

    public static TypeContext<?> lookup(final String name) {
        try {
            return TypeContext.of(Class.forName(name));
        }
        catch (final ClassNotFoundException e) {
            return VOID;
        }
    }

    public List<TypeContext<?>> interfaces() {
        if (this.interfaces == null) {
            this.verifyMetadataAvailable();
            this.interfaces = Arrays.stream(this.type().getInterfaces())
                    .map(TypeContext::of)
                    .collect(Collectors.toList());
        }
        return this.interfaces;
    }

    public TypeContext<?> parent() {
        if (this.parent == null) {
            this.verifyMetadataAvailable();
            final Class<? super T> parent = this.type().getSuperclass();
            if (parent == null) this.parent = VOID;
            else this.parent = TypeContext.of(parent);
        }
        return this.parent;
    }

    public List<MethodContext<?, T>> flatMethods() {
        if (this.flatMethods == null) {
            this.verifyMetadataAvailable();
            final Method[] methods = this.type().getMethods();
            // Note that .getMethods does not include abstract methods, while .getDeclaredMethods does, as
            // abstract methods are as relevant as any other within this context they should be included.
            final Method[] declaredMethods = this.type().getDeclaredMethods();
            final Method[] allMethods = HartshornUtils.merge(methods, declaredMethods);
            this.flatMethods = Arrays.stream(allMethods)
                    .map(MethodContext::of)
                    .map(ctx -> (MethodContext<?, T>) ctx)
                    .collect(Collectors.toList());
        }
        return this.flatMethods;
    }

    public List<MethodContext<?, T>> flatMethods(final Class<? extends Annotation> annotation) {
        return this.flatMethods().stream()
                .filter(method -> method.annotation(annotation).present())
                .toList();
    }

    public List<TypeContext<?>> typeParameters() {
        if (this.typeParameters == null) {
            this.verifyMetadataAvailable();
            final Type genericSuper = this.type().getGenericSuperclass();
            if (genericSuper instanceof ParameterizedType parameterized) {
                final Type[] arguments = parameterized.getActualTypeArguments();

                this.typeParameters = Arrays.stream(arguments)
                        .filter(Class.class::isInstance)
                        .map(type -> (Class<?>) type)
                        .map(TypeContext::of)
                        .collect(Collectors.toList());
            } else {
                this.typeParameters = HartshornUtils.emptyList();
            }
        }
        return this.typeParameters;
    }

    public Exceptional<FieldContext<?>> field(final String field) {
        this.collectFields();
        if (this.fields.containsKey(field))
            return Exceptional.of(this.fields.get(field));
        else
            return this.parent().field(field);
    }

    public List<FieldContext<?>> fields() {
        this.collectFields();
        return HartshornUtils.asUnmodifiableList(this.fields.values());
    }

    public List<FieldContext<?>> fields(final Class<? extends Annotation> annotation) {
        return this.fields().stream()
                .filter(field -> field.annotation(annotation).present())
                .toList();
    }

    public <P> List<FieldContext<P>> fieldsOf(final Class<P> type) {
        return this.fields().stream()
                .filter(field -> field.type().childOf(type))
                .map(field -> (FieldContext<P>) field)
                .collect(Collectors.toList());
    }

    public <P> List<FieldContext<P>> fieldsOf(final GenericType<P> type) {
        final Exceptional<Class<P>> real = type.asClass();
        if (real.absent()) return HartshornUtils.emptyList();
        else return this.fieldsOf(real.get());
    }

    private void collectFields() {
        if (this.fields.isEmpty()) {
            this.verifyMetadataAvailable();
            for (final Field declared : this.type().getDeclaredFields()) {
                this.fields.put(declared.getName(), FieldContext.of(declared));
            }
            if (!(this.parent().isVoid() || Object.class.equals(this.parent().type()))) {
                for (final FieldContext<?> field : this.parent().fields()) {
                    this.fields.put(field.name(), field);
                }
            }
        }
    }

    public boolean childOf(final TypeContext<?> type) {
        this.verifyMetadataAvailable();
        if (type instanceof WildcardTypeContext) return true;
        return this.childOf(type.type());
    }

    public boolean childOf(final Class<?> to) {
        this.verifyMetadataAvailable();
        final Class<T> from = this.type();

        if (null == to || null == from) return false;
        //noinspection ConstantConditions
        if (to == from || to.equals(from)) return true;

        if (to.isAssignableFrom(from)) {
            return true;
        }
        if (from.isPrimitive()) {
            return isPrimitiveWrapper(to, from);
        }
        if (to.isPrimitive()) {
            return isPrimitiveWrapper(from, to);
        }
        return false;
    }

    private static boolean isPrimitiveWrapper(final Class<?> targetClass, final Class<?> primitive) {
        if (!primitive.isPrimitive()) {
            throw new IllegalArgumentException("First argument has to be isPrimitiveWrapper type");
        }
        return PRIMITIVE_WRAPPERS.get(primitive) == targetClass;
    }

    public boolean isAbstract() {
        return this.type.isInterface() || Modifier.isAbstract(this.type.getModifiers());
    }

    public boolean isProxy() {
        if (Tristate.UNDEFINED == this.isProxy) {
            this.isProxy = isProxy(this.type()) ? Tristate.TRUE : Tristate.FALSE;
        }
        return this.isProxy.booleanValue();
    }

    private static boolean isProxy(final Class<?> type) {
        return (ProxyFactory.isProxyClass(type) || Proxy.isProxyClass(type));
    }

    public boolean isNative() {
        if (this.isNative == null) {
            this.isNative = false;
            for (final Class<?> supported : NATIVE_SUPPORTED) {
                if (this.childOf(supported)) {
                    this.isNative = true;
                    break;
                }
            }
        }
        return this.isNative;
    }

    public List<ConstructorContext<T>> constructors() {
        if (this.constructors == null) {
            this.verifyMetadataAvailable();
            this.constructors = Arrays.stream(this.type().getConstructors())
                    .map(constructor -> (Constructor<T>) constructor)
                    .map(ConstructorContext::of)
                    .collect(Collectors.toList());
        }
        return this.constructors;
    }

    public List<ConstructorContext<T>> constructors(final Class<? extends Annotation> annotation) {
        return this.constructors().stream()
                .filter(constructor -> constructor.annotation(annotation).present())
                .collect(Collectors.toList());
    }

    public List<ConstructorContext<T>> boundConstructors() {
        return this.constructors(Bound.class);
    }

    public List<ConstructorContext<T>> injectConstructors() {
        return this.constructors(Inject.class);
    }

    public Exceptional<ConstructorContext<T>> defaultConstructor() {
        if (this.defaultConstructor == null) {
            this.verifyMetadataAvailable();
            this.defaultConstructor = Exceptional.of(() -> ConstructorContext.of(this.type.getDeclaredConstructor()));
        }
        return this.defaultConstructor;
    }

    public String name() {
        return this.type().getSimpleName();
    }

    public String qualifiedName() {
        return this.type().getCanonicalName();
    }

    public List<T> enumConstants() {
        if (this.enumConstants == null) {
            this.verifyMetadataAvailable();
            if (!this.isEnum) this.enumConstants = HartshornUtils.asUnmodifiableList(HartshornUtils.emptyList());
            else {
                this.enumConstants = HartshornUtils.asUnmodifiableList(this.type().getEnumConstants());
            }
        }
        return this.enumConstants;
    }

    @Override
    protected Class<T> element() {
        return this.type();
    }

    public static <T> T toPrimitive(TypeContext<?> type, final String value) throws TypeConversionException, NotPrimitiveException {
        try {
            if (type.isEnum()) {
                return (T) Enum.valueOf((Class<? extends Enum>) type.type(), String.valueOf(value).toUpperCase());
            }
            else {
                if (!type.isPrimitive()) {
                    for (final Entry<Class<?>, Class<?>> entry : PRIMITIVE_WRAPPERS.entrySet()) {
                        if (isPrimitiveWrapper(type.type(), entry.getKey())) type = TypeContext.of(entry.getKey());
                    }
                }
                if (!type.isPrimitive()) throw new NotPrimitiveException(type);
                else {
                    final Function<String, ?> converter = PRIMITIVE_FROM_STRING.get(type.type());
                    return (T) converter.apply(value);
                }
            }
        }
        catch (final NotPrimitiveException e) {
            throw e;
        }
        catch (final Throwable t) {
            throw new TypeConversionException(type, value, t);
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof final TypeContext<?> that)) return false;
        return this.type.equals(that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.type);
    }

    @Override
    public String toString() {
        return "TypeContext{%s}".formatted(this.type);
    }

    public T populate(final T instance, final Map<String, Object> data) {
        if (instance == null) return null;
        final TypeContext<T> type = TypeContext.of(instance);
        for (final Entry<String, Object> field : data.entrySet()) {
            final Exceptional<FieldContext<?>> declaredField = type.field(field.getKey());
            if (declaredField.present()) {
                declaredField.get().set(instance, field.getValue());
            }
        }
        return instance;
    }

    public boolean is(final Class<?> type) {
        return this.type().equals(type);
    }

    public Exceptional<MethodContext<?, T>> method(final String name) {
        return this.method(name, HartshornUtils.emptyList());
    }

    public Exceptional<MethodContext<?, T>> method(final String name, final List<TypeContext<?>> arguments) {
        if (this.methods == null) {
            // Organizing the methods by name and arguments isn't worth the additional overhead for list comparisons,
            // so instead we only link it by name and perform the list comparison on request.
            this.methods = ArrayListMultimap.create();
            for (final MethodContext<?, T> method : this.flatMethods()) {
                this.methods.put(method.name(), method);
            }
        }
        if (this.methods.containsKey(name)) {
            final Collection<MethodContext<?, T>> overloadingMethods = this.methods.get(name);
            for (final MethodContext<?, T> method : overloadingMethods) {
                if (method.parameterTypes().equals(arguments)) return Exceptional.of(method);
            }
        }
        return Exceptional.empty();
    }

    public Exceptional<MethodContext<?, T>> method(final String name, final TypeContext<?>... arguments) {
        return this.method(name, Arrays.asList(arguments));
    }

    public Exceptional<MethodContext<?, T>> method(final String name, final Class<?>... arguments) {
        return this.method(name, Arrays.stream(arguments).map(TypeContext::of).collect(Collectors.toList()));
    }

    private void verifyMetadataAvailable() {
        if (this.isProxy()) throw new ApplicationException("Cannot collect metadata of proxied type").runtime();
    }
}
