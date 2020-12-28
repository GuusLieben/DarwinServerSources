/*
 *  Copyright (C) 2020 Guus Lieben
 *
 *  This framework is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as
 *  published by the Free Software Foundation, either version 2.1 of the
 *  License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 *  the GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this library. If not, see {@literal<http://www.gnu.org/licenses/>}.
 */

package org.dockbox.selene.core.impl.events.processors;

import org.dockbox.selene.core.annotations.event.processing.Getter;
import org.dockbox.selene.core.annotations.event.processing.Provided;
import org.dockbox.selene.core.annotations.event.processing.SkipIf;
import org.dockbox.selene.core.annotations.event.processing.UnwrapOrSkip;
import org.dockbox.selene.core.annotations.event.processing.WrapSafe;
import org.dockbox.selene.core.annotations.extension.Extension;
import org.dockbox.selene.core.events.EventStage;
import org.dockbox.selene.core.events.EventWrapper;
import org.dockbox.selene.core.events.parents.Event;
import org.dockbox.selene.core.events.processing.AbstractEventParamProcessor;
import org.dockbox.selene.core.exceptions.SkipEventException;
import org.dockbox.selene.core.objects.Exceptional;
import org.dockbox.selene.core.server.Selene;
import org.dockbox.selene.core.server.properties.InjectorProperty;
import org.dockbox.selene.core.util.SeleneUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * Parameter annotation processor definitions for internal event listener parameter annotations. This enumeration only
 * contains definitions for internal annotations, processors for annotations applied by extensions should be defined in
 * the responsible extension.
 */
public enum DefaultParamProcessors {
    /**
     * The processor definition for {@link Getter}. Tries to obtain a value through a getter method inside the provided
     * {@link Event} instance. If no method exists {@code null} is returned. This processor is performed in a {@link EventStage#POPULATE}
     * stage, making it the first available option to provide the object value. It is possible there is another annotation
     * processed before this if it is in the same stage, in which case the processor respects the value of
     * {@link Provided#overrideExisting()}.
     */
    GETTER(Getter.class, EventStage.POPULATE, (object, annotation, event, parameter, wrapper) -> {
        if (null != object && !annotation.overrideExisting()) return object;

        AtomicReference<Object> arg = new AtomicReference<>(null);
        SeleneUtils.REFLECTION.getMethodValue(event, annotation.value(), parameter.getType())
                .ifPresent(arg::set);
        return arg.get();
    }),

    /**
     * The processor definition for {@link Provided}. Tries to obtain a value through {@link Selene#getInstance(Class, InjectorProperty[])}.
     * If no instance is found {@code null} is returned. This processor is performed in a {@link EventStage#POPULATE}
     * stage, making it the first available option to provide the object value. It is possible there is another annotation
     * processed before this if it is in the same stage, in which case the processor respects the value of
     * {@link Provided#overrideExisting()}.
     */
    PROVIDED(Provided.class, EventStage.POPULATE, (object, annotation, event, parameter, wrapper) -> {
        if (null != object && !annotation.overrideExisting()) return object;

        Class<?> extensionClass = parameter.getType();
        if (SeleneUtils.REFLECTION.isNotVoid(annotation.value()) && annotation.value().isAnnotationPresent(Extension.class)) {
            extensionClass = annotation.value();
        } else if (wrapper.getListener().getClass().isAnnotationPresent(Extension.class)) {
            extensionClass = wrapper.getListener().getClass();
        }
        return Selene.getInstance(parameter.getType(), extensionClass);
    }),

    /**
     * The processor definition for {@link SkipIf}. Filters the value of the provided object based on a given
     * {@link SkipIf.Type}, which either:
     * <ul>
     *  <li>Performs a {@code null} check.</li>
     *  <li>
     *      Checks if the object is empty (applies to {@link java.util.Collection}s, {@link String}s, and any type with a
     *      method {@code isEmpty} which returns a {@link Boolean}.
     *  </li>
     *  <li>Checks if the object is a instance of {@link Number} and is equal to {@code 0}.</li>
     * </ul>
     */
    SKIP_IF(SkipIf.class, EventStage.FILTER, (object, annotation, event, parameter, wrapper) -> {
        switch (annotation.value()) {
            case NULL:
                if (null == object) throw new SkipEventException();
                break;
            case EMPTY:
                if (SeleneUtils.ASSERTION.isEmpty(object)) throw new SkipEventException();
                break;
            case ZERO:
                if (object instanceof Number && 0 == ((Number) object).floatValue())
                    throw new SkipEventException();
                break;
        }
        return object;
    }),

    /**
     * The processor definition for {@link WrapSafe}. Wraps the final object in a instance of {@link Exceptional}. If the
     * object is a instance of {@link Exceptional} it is returned 'as is'. If the object is a instance of {@link Optional}
     * it is converted to a {@link Exceptional}.
     */
    WRAP_SAFE(WrapSafe.class, EventStage.FILTER, (object, annotation, event, parameter, wrapper) -> {
        if (SeleneUtils.REFLECTION.isAssignableFrom(parameter.getType(), event.getClass())) {
            Selene.log().warn("Event parameter cannot be wrapped");
            return object;
        }
        if (object instanceof Exceptional<?>) return object;
        if (object instanceof Optional<?>) return Exceptional.of((Optional<?>) object);

        return Exceptional.ofNullable(object);
    }),

    /**
     * The processor definition for {@link UnwrapOrSkip}. Attempts to unwrap the final object if it is a instance of
     * {@link Exceptional} or {@link Optional}. If the type is {@code null} or the value is not present, it respects
     * {@link UnwrapOrSkip#skipIfNull()} to skip the event or return {@code null}.
     * If the value is already unwrapped and not {@code null} it is returned 'as is'
     */
    UNWRAP_OR_SKIP(UnwrapOrSkip.class, EventStage.FILTER, (object, annotation, event, parameter, wrapper) -> {
        if (object instanceof Exceptional<?>) {
            if (((Exceptional<?>) object).isPresent()) return ((Exceptional<?>) object).get();
            else if (annotation.skipIfNull()) throw new SkipEventException();

        } else if (object instanceof Optional<?>) {
            if (((Optional<?>) object).isPresent()) return ((Optional<?>) object).get();
            else if (annotation.skipIfNull()) throw new SkipEventException();

        } else if (null == object && annotation.skipIfNull()) {
            throw new SkipEventException();
        }
        return object; // Already unwrapped
    });

    private final EventStage stage;
    private final Supplier<AbstractEventParamProcessor<?>> processorSupplier;

    <A extends Annotation> DefaultParamProcessors(Class<A> annotationClass, AbstractEnumEventParamProcessor<A> processor) {
        this(annotationClass, EventStage.PROCESS, processor);
    }

    <A extends Annotation> DefaultParamProcessors(Class<A> annotationClass, EventStage stage, AbstractEnumEventParamProcessor<A> processor) {
        this.stage = stage;
        this.processorSupplier = () -> new AbstractEventParamProcessor<A>() {
            @Override
            public @NotNull Class<A> getAnnotationClass() {
                return annotationClass;
            }

            @Override
            public @NotNull EventStage targetStage() {
                return null == stage ? super.targetStage() : stage;
            }

            @Override
            public @Nullable Object process(Object object, A annotation, Event event, Parameter parameter, EventWrapper wrapper) throws SkipEventException {
                return processor.process(object, annotation, event, parameter, wrapper);
            }
        };
    }

    /**
     * Gets the {@link EventStage} at which the processor is to be performed.
     *
     * @return The stage
     */
    public EventStage getStage() {
        return this.stage;
    }

    /**
     * Gets a new instance of a {@link AbstractEventParamProcessor} based on the supplier definition.
     *
     * @return The processor
     */
    public AbstractEventParamProcessor<?> getProcessor() {
        return this.processorSupplier.get();
    }

    @FunctionalInterface
    private interface AbstractEnumEventParamProcessor<A extends Annotation> {
        Object process(Object object, A annotation, Event event, Parameter parameter, EventWrapper wrapper) throws SkipEventException;
    }
}
