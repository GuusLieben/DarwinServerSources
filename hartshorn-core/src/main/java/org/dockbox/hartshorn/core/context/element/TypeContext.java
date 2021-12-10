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

package org.dockbox.hartshorn.core.context.element;

import org.dockbox.hartshorn.core.ArrayListMultiMap;
import org.dockbox.hartshorn.core.GenericType;
import org.dockbox.hartshorn.core.HartshornUtils;
import org.dockbox.hartshorn.core.MultiMap;
import org.dockbox.hartshorn.core.annotations.inject.Bound;
import org.dockbox.hartshorn.core.boot.ExceptionHandler;
import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.domain.Exceptional;
import org.dockbox.hartshorn.core.domain.tuple.Tristate;
import org.dockbox.hartshorn.core.domain.tuple.Tuple;
import org.dockbox.hartshorn.core.exceptions.ApplicationException;
import org.dockbox.hartshorn.core.exceptions.NotPrimitiveException;
import org.dockbox.hartshorn.core.exceptions.TypeConversionException;
import org.dockbox.hartshorn.core.proxy.javassist.JavassistProxyUtil;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;

import lombok.Getter;

// skipcq: JAVA-W0100
public class TypeContext<T> extends AnnotatedElementContext<Class<T>> {

    private static final Map<Class<?>, TypeContext<?>> CACHE = new ConcurrentHashMap<>();

    /**
     * Fields which should be ignored when detected. This can be for varying reasons, which should be
     * documented on the entry in this array directly.
     */
    private static final Set<String> EXCLUDED_FIELDS = Set.of(
            /*
             * This field is a synthetic field which is added by IntelliJ IDEA when running tests with
             * coverage.
            */
            "__$lineHits$__"
    );
    private static final Map<Class<?>, Class<?>> PRIMITIVE_WRAPPERS = HartshornUtils.ofEntries(
            Tuple.of(boolean.class, Boolean.class),
            Tuple.of(byte.class, Byte.class),
            Tuple.of(char.class, Character.class),
            Tuple.of(double.class, Double.class),
            Tuple.of(float.class, Float.class),
            Tuple.of(int.class, Integer.class),
            Tuple.of(long.class, Long.class),
            Tuple.of(short.class, Short.class)
    );
    private static final Map<?, Function<String, ?>> PRIMITIVE_FROM_STRING = HartshornUtils.ofEntries(
            Tuple.of(boolean.class, Boolean::valueOf),
            Tuple.of(byte.class, Byte::valueOf),
            Tuple.of(char.class, s -> s.charAt(0)),
            Tuple.of(double.class, Double::valueOf),
            Tuple.of(float.class, Float::valueOf),
            Tuple.of(int.class, Integer::valueOf),
            Tuple.of(long.class, Long::valueOf),
            Tuple.of(short.class, Short::valueOf)
    );
    private static final List<Class<?>> NATIVE_SUPPORTED = HartshornUtils.asList(
            boolean.class, byte.class, short.class,
            int.class, long.class, float.class, double.class,
            byte[].class, int[].class, long[].class,
            String.class, List.class, Map.class
    );
    private static final Map<Class<?>, Object> PRIMITIVE_DEFAULTS = HartshornUtils.ofEntries(
            Tuple.of(boolean.class, false),
            Tuple.of(byte.class, 0),
            Tuple.of(char.class, '\u0000'),
            Tuple.of(double.class, 0.0d),
            Tuple.of(float.class, 0.0f),
            Tuple.of(int.class, 0),
            Tuple.of(long.class, 0L),
            Tuple.of(short.class, 0)
    );
    private static final Map<Class<?>, Class<?>> WRAPPERS_TO_PRIMITIVE = HartshornUtils.ofEntries(
            Tuple.of(Boolean.class, boolean.class),
            Tuple.of(Byte.class, byte.class),
            Tuple.of(Character.class, char.class),
            Tuple.of(Double.class, double.class),
            Tuple.of(Float.class, float.class),
            Tuple.of(Integer.class, int.class),
            Tuple.of(Long.class, long.class),
            Tuple.of(Short.class, short.class)
    );

    public static final TypeContext<Void> VOID = TypeContext.of(Void.class);

    @Getter private final Class<T> type;
    @Getter private final boolean isVoid;
    @Getter private final boolean isAnonymous;
    @Getter private final boolean isPrimitive;
    @Getter private final boolean isEnum;
    @Getter private final boolean isAnnotation;
    @Getter private final boolean isArray;
    @Getter private final boolean isTypeContext;

    private final Map<String, FieldContext<?>> fields = new ConcurrentHashMap<>();

    @Nullable
    private Boolean isNative;
    private List<T> enumConstants;
    private TypeContext<?> parent;
    private List<TypeContext<?>> interfaces;
    private List<MethodContext<?, T>> declaredAndInheritedMethods;
    private List<MethodContext<?, T>> declaredMethods;
    private List<TypeContext<?>> typeParameters;
    private MultiMap<TypeContext<?>, TypeContext<?>> interfaceTypeParameters;
    private List<ConstructorContext<T>> constructors;
    private Map<Class<?>, Annotation> annotations;
    private MultiMap<String, MethodContext<?, T>> methods;
    private Exceptional<ConstructorContext<T>> defaultConstructor;
    private Exceptional<TypeContext<?>> elementType;
    private Tristate isProxy = Tristate.UNDEFINED;

    protected TypeContext(final Class<T> type) {
        this.type = type;
        this.isVoid = Void.TYPE.equals(type) || Void.class.equals(type);
        this.isAnonymous = type.isAnonymousClass();
        this.isPrimitive = type.isPrimitive();
        this.isEnum = type.isEnum();
        this.isAnnotation = type.isAnnotation();
        this.isArray = type.isArray();
        this.isTypeContext = TypeContext.class.isAssignableFrom(type);
    }

    public static <T> TypeContext<T> unproxy(final ApplicationContext context, final T instance) {
        if (instance == null) {
            return (TypeContext<T>) VOID;
        }
        if (context.environment().manager().isProxy(instance)) {
            return context.environment().manager().real(instance)
                    .orThrowUnchecked(() -> new ApplicationException("Could not derive real type of instance " + instance));
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

    protected static <T> TypeContext<T> of(final ParameterizedType type) {
        final TypeContext<T> context = of((Class<T>) type.getRawType());
        context.typeParameters = context.contextsFromParameterizedType(type);
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

    public List<MethodContext<?, T>> methods() {
        if (this.declaredAndInheritedMethods == null) {
            this.verifyMetadataAvailable();
            final Set<Method> allMethods = new HashSet<>();
            final Method[] declaredMethods = this.type().getDeclaredMethods();
            final Method[] methods = this.type().getMethods();
            if (!this.parent().isVoid()) {
                final List<Method> superClassMethods = this.parent().methods().stream()
                        .filter(m -> m.isPublic() || m.isProtected())
                        .map(MethodContext::method)
                        .toList();
                allMethods.addAll(superClassMethods);
            }
            allMethods.addAll(Arrays.asList(declaredMethods));
            allMethods.addAll(Arrays.asList(methods));

            this.declaredAndInheritedMethods = allMethods.stream()
                    .map(MethodContext::of)
                    .map(method -> (MethodContext<?, T>) method)
                    .collect(Collectors.toUnmodifiableList());
        }
        return this.declaredAndInheritedMethods;
    }

    public List<MethodContext<?, T>> declaredMethods() {
        if (this.declaredMethods == null) {
            this.verifyMetadataAvailable();
            this.declaredMethods = Arrays.stream(this.type().getDeclaredMethods())
                    .map(MethodContext::of)
                    .map(method -> (MethodContext<?, T>) method)
                    .collect(Collectors.toUnmodifiableList());
        }
        return this.declaredMethods;
    }

    public List<MethodContext<?, T>> methods(final Class<? extends Annotation> annotation) {
        return this.methods().stream()
                .filter(method -> method.annotation(annotation).present())
                .toList();
    }

    public List<TypeContext<?>> typeParameters(final Class<?> superInterface) {
        return this.typeParameters(TypeContext.of(superInterface));
    }

    public List<TypeContext<?>> typeParameters(final TypeContext<?> superInterface) {
        if (!superInterface.isInterface()) throw new IllegalArgumentException("Provided type " + superInterface.name() + " is not a interface");
        if (!this.childOf(superInterface)) throw new IllegalArgumentException("Provided interface " + superInterface.name() + " is not a super type of " + this.name());

        if (this.interfaceTypeParameters == null) {
            this.interfaceTypeParameters = new ArrayListMultiMap<>();
            for (final Type genericSuper : this.type().getGenericInterfaces()) {
                if (genericSuper instanceof ParameterizedType parameterized) {
                    final Type raw = parameterized.getRawType();
                    if (raw instanceof Class<?> clazz && superInterface.is(clazz)) {
                        this.interfaceTypeParameters.putAll(superInterface, this.contextsFromParameterizedType(parameterized));
                    }
                }
            }
        }
        return List.copyOf(this.interfaceTypeParameters.get(superInterface));
    }

    public List<TypeContext<?>> typeParameters() {
        if (this.typeParameters == null) {
            this.verifyMetadataAvailable();
            final Type genericSuper = this.type().getGenericSuperclass();
            if (genericSuper instanceof ParameterizedType parameterized) {
                this.typeParameters = this.contextsFromParameterizedType(parameterized);
            } else {
                this.typeParameters = List.of();
            }
        }
        return this.typeParameters;
    }

    private List<TypeContext<?>> contextsFromParameterizedType(final ParameterizedType parameterizedType) {
        final Type[] arguments = parameterizedType.getActualTypeArguments();

        return Arrays.stream(arguments)
                .filter(type -> type instanceof Class || type instanceof WildcardType)
                .map(type -> {
                    if (type instanceof Class clazz) return TypeContext.of(clazz);
                    else if (type instanceof WildcardType wildcard) return WildcardTypeContext.create();
                    else return TypeContext.VOID;
                })
                .map(type -> (TypeContext<?>) type)
                .collect(Collectors.toList());
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
        return List.copyOf(this.fields.values());
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
        if (real.absent()) return List.of();
        else return this.fieldsOf(real.get());
    }

    private void collectFields() {
        if (this.fields.isEmpty()) {
            this.verifyMetadataAvailable();
            for (final Field declared : this.type().getDeclaredFields()) {
                if (TypeContext.EXCLUDED_FIELDS.contains(declared.getName()))
                    continue;

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
            throw new IllegalArgumentException("Second argument has to be primitive type");
        }
        return PRIMITIVE_WRAPPERS.get(primitive) == targetClass;
    }

    public boolean isAbstract() {
        return this.isInterface() || Modifier.isAbstract(this.type().getModifiers());
    }

    public boolean isInterface() {
        return this.type().isInterface();
    }

    public boolean isProxy() {
        if (Tristate.UNDEFINED == this.isProxy) {
            this.isProxy = JavassistProxyUtil.isProxy(this.type()) ? Tristate.TRUE : Tristate.FALSE;
        }
        return this.isProxy.booleanValue();
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

    public Exceptional<TypeContext<?>> elementType() {
        if (this.elementType == null) {
            this.verifyMetadataAvailable();
            this.elementType = this.isArray()
                    ? Exceptional.of(of(this.type().getComponentType()))
                    : Exceptional.of(new IllegalArgumentException("The reflected type must be an array to use this command"));
        }
        return this.elementType;
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
            if (!this.isEnum) this.enumConstants = List.of();
            else {
                this.enumConstants = List.of(this.type().getEnumConstants());
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
        return this.method(name, List.of());
    }

    public Exceptional<MethodContext<?, T>> method(final String name, final List<TypeContext<?>> arguments) {
        if (this.methods == null) {
            // Organizing the methods by name and arguments isn't worth the additional overhead for list comparisons,
            // so instead we only link it by name and perform the list comparison on request.
            this.methods = new ArrayListMultiMap<>();
            for (final MethodContext<?, T> method : this.methods()) {
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

    @Override
    protected Map<Class<?>, Annotation> validate() {
        if (this.parent().isVoid()) return super.validate();
        else if (this.annotations == null) {
            final Map<Class<?>, Annotation> annotations = HartshornUtils.emptyMap();
            Class<?> type = this.type();
            while (type != null) {
                for (final Annotation annotation : type.getDeclaredAnnotations()) {
                    annotations.put(annotation.annotationType(), annotation);
                }
                type = type.getSuperclass();
            }
            this.annotations = annotations;
        }
        return this.annotations;
    }

    public Exceptional<MethodContext<?, T>> method(final String name, final TypeContext<?>... arguments) {
        return this.method(name, Arrays.asList(arguments));
    }

    public Exceptional<MethodContext<?, T>> method(final String name, final Class<?>... arguments) {
        return this.method(name, Arrays.stream(arguments).map(TypeContext::of).collect(Collectors.toList()));
    }

    private void verifyMetadataAvailable() {
        if (this.isProxy()) ExceptionHandler.unchecked(new ApplicationException("Cannot collect metadata of proxied type '%s'".formatted(this.qualifiedName())));
    }

    public T defaultOrNull() {
        if (this.isPrimitive()) {
            return (T) PRIMITIVE_DEFAULTS.getOrDefault(this.type(), null);
        } else {
            final Class<?> primitive = WRAPPERS_TO_PRIMITIVE.get(this.type());
            if (primitive == null) return null;
            else return (T) TypeContext.of(primitive).defaultOrNull();
        }
    }

    public boolean isFinal() {
        return Modifier.isFinal(this.type().getModifiers());
    }
}
