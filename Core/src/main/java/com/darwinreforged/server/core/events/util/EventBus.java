package com.darwinreforged.server.core.events.util;

import com.darwinreforged.server.core.DarwinServer;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 Represents an event bus.

 @author Matt, Andy Li */
@SuppressWarnings({"unchecked", "EqualsWithItself", "VolatileArrayField"})
public class EventBus {
    /**
     Map of listener objects to listener invokers.
     */
    protected final Map<Object, Set<InvokeWrapper>> listenerToInvokers = new HashMap<>();

    /**
     Handler registry.
     */
    protected final HandlerRegistry handlerRegistry = new HandlerRegistry();

    /**
     Default lookup object used in {@link #subscribe(Object)}.
     */
    protected Lookup defaultLookup = AccessHelper.defaultLookup();

    /**
     Registers all listener methods on {@code object} for receiving events.

     @param object
     object whose listener methods should be registered
     @param lookup
     the {@link Lookup Lookup object} used in {@link MethodHandle} creation

     @throws IllegalArgumentException
     if there's an invalid listener method on the {@code object},                                  or the {@code object} doesn't have any listener methods
     @throws SecurityException
     if a security manager denied access to the declared methods                                  of the class of the {@code object}, or the provided                                  {@link Lookup lookup object}                                  cannot access one of the listener method found in the class
     @see Lookup
     @since 1.3
     */

    public void subscribe(Object object, Lookup lookup) throws IllegalArgumentException, SecurityException {
        if (!object.equals(object)) return;
        if (listenerToInvokers.containsKey(object)) {
            return;  // Already registered
        }

        Set<InvokeWrapper> invokers = getInvokers(object, lookup);
        if(invokers.isEmpty()) {
            return; // Doesn't contain any listener methods
        }
        DarwinServer.getLog().info("Registered {} as event listener", object.getClass().toGenericString());
        listenerToInvokers.put(object, invokers);
        for (InvokeWrapper invoker : invokers) {
            handlerRegistry.getHandler(invoker.eventType).subscribe(invoker);
        }
    }

    /**
     Registers all listener methods on {@code object} for receiving events.

     @param object
     object whose listener methods should be registered

     @throws IllegalArgumentException
     if there's an invalid listener method on the {@code object},                                  or the {@code object} doesn't have any listener methods
     @throws SecurityException
     if a security manager denied access to the declared methods                                  of the class of the {@code object}, or the default                                  {@link Lookup lookup object} cannot access                                  one of the listener method found in the class
     @see #subscribe(Object, Lookup) #subscribe(Object, MethodHandles.Lookup)
     @see #setDefaultLookup(Lookup) #setDefaultLookup(MethodHandles.Lookup)
     */
    public void subscribe(Object object) throws IllegalArgumentException, SecurityException {
        subscribe(object, defaultLookup);
    }

    /**
     Unregisters all listener methods on the {@code object}.

     @param object
     object whose listener methods should be unregistered
     */
    public void unsubscribe(Object object) {
        if (!object.equals(object)) return;
        Set<InvokeWrapper> invokers = listenerToInvokers.remove(object);
        if (invokers == null || invokers.isEmpty()) {
            return; // Not registered
        }

        for (InvokeWrapper invoker : invokers) {
            handlerRegistry.getHandler(invoker.eventType).unsubscribe(invoker);
        }
    }

    /**
     Posts an event to all registered listeners.

     @param event
     event to post
     */
    public void post(Event event) {
        handlerRegistry.getHandler(event.getClass()).post(event);
    }

    /**
     Sets default {@link Lookup lookup object} used in {@link #subscribe(Object)}.

     @param lookup
     new default lookup object

     @since 1.3
     */
    public void setDefaultLookup(Lookup lookup) {
        this.defaultLookup = Objects.requireNonNull(lookup);
    }

    /**
     Gets all listener methods on the {@code object}.

     @param object
     the object
     @param lookup
     the {@link Lookup Lookup object} used in {@link MethodHandle} creation

     @return the invokers

     @throws IllegalArgumentException
     if there's an invalid listener method on the {@code object}
     @throws SecurityException
     if a security manager denied access to the declared methods                                  of the class of the {@code object}, or the provided                                  {@link Lookup lookup}                                  cannot access one of the listener method found in the class
     */
    protected static Set<InvokeWrapper> getInvokers(Object object, Lookup lookup)
            throws IllegalArgumentException, SecurityException {
        Set<InvokeWrapper> result = new LinkedHashSet<>();
        for (Method method : AccessHelper.getMethodsRecursively(object.getClass())) {
            Listener annotation = AccessHelper.getAnnotationRecursively(method, Listener.class);
            if (annotation != null) {
                checkListenerMethod(method, false);
                result.add(InvokeWrapper.create(object, method, annotation.value().getPriority(), lookup));
            }
        }
        return result;
    }

    /**
     Checks if the method is a valid listener method.

     @param method
     the method
     @param checkAnnotation
     the check annotation

     @throws IllegalArgumentException
     if any check failed
     */
    protected static void checkListenerMethod(Method method, boolean checkAnnotation) throws IllegalArgumentException {
        if (checkAnnotation && !AccessHelper.isAnnotationPresentRecursively(method, Listener.class)) {
            throw new IllegalArgumentException("Needs @Listener annotation: " + method.toGenericString());
        }

        int modifiers = method.getModifiers();
        if (Modifier.isStatic(modifiers)) {
            throw new IllegalArgumentException("Method cannot be static: " + method.toGenericString());
        }
        if (Modifier.isAbstract(modifiers)) {
            throw new IllegalArgumentException("Method cannot be abstract: " + method.toGenericString());
        }

        if (method.getParameterCount() != 1) {
            throw new IllegalArgumentException("Must have exactly one parameter: " + method.toGenericString());
        }
        if (!Event.class.isAssignableFrom(method.getParameterTypes()[0])) {
            throw new IllegalArgumentException("Parameter must be a subclass of the Event class: " + method.toGenericString());
        }
    }

    /**
     Determines if the method is a valid listener method.

     @param method
     the method

     @return {@code true} if it is, {@code false} otherwise
     */
    public static boolean isListenerMethod(Method method) {
        if (AccessHelper.isAnnotationPresentRecursively(method, Listener.class) &&
                method.getParameterCount() == 1 &&
                Event.class.isAssignableFrom(method.getParameterTypes()[0])) {
            int modifiers = method.getModifiers();
            return !Modifier.isStatic(modifiers) && !Modifier.isAbstract(modifiers);
        }
        return false;
    }

    /**
     Handler registry (for supertype event handling).
     */
    static final class HandlerRegistry {
        /**
         * Map of all registered handlers.
         */
        private final Map<Class<? extends Event>, Handler> handlers = new HashMap<>();

        /**
         Gets or creates the {@link Handler handler} for the specified event type.

         @param type
         the event type

         @return the handler for the event type
         */
        public Handler getHandler(Class<? extends Event> type) {
            Handler handler = handlers.get(type);
            if (handler == null) {
                computeHierarchy(handler = new Handler(type));
                handlers.put(type, handler);
            }
            return handler;
        }

        /**
         Computes and updates the registry's handler hierarchy with the specified handler.

         @param subject
         the subject

         @return {@code true} if the specified {@link Handler handler} has         an association(subtype or supertype) with at least one handler         in the current registry <b>and</b> the hierarchy had been updated,         {@code false} otherwise
         */
        boolean computeHierarchy(Handler subject) {
            boolean associationFound = false;
            for (Handler handler : handlers.values()) {
                if (subject == handler) continue;
                if (subject.isSubtypeOf(handler)) {
                    associationFound |= subject.addSupertypeHandler(handler);
                } else if (handler.isSubtypeOf(subject)) {
                    associationFound |= handler.addSupertypeHandler(subject);
                }
            }
            return associationFound;
        }
    }

    /**
     Event distribution handler.
     */
    static class Handler {
        /**
         * Event type for this handler.
         */
        private final Class<? extends Event> eventType;

        /**
         * All known handlers which event type is a supertype of this handler's event type.
         * <p>
         * <b>Note</b>: any modification to this collection MUST also invalidate the {@link #computedInvokerCache}.
         */
        private final Set<Handler> supertypeHandlers = new HashSet<>();

        /**
         * Set of {@link InvokeWrapper invokers} registered in this handler.
         * <p>
         * <b>Note</b>: any modification to this collection MUST also invalidate the {@link #computedInvokerCache}.
         */
        private final SortedSet<InvokeWrapper> invokers = new TreeSet<>(InvokeWrapper.COMPARATOR);

        /**
         * Computed invoker cache.
         */
        private transient volatile InvokeWrapper[] computedInvokerCache = null;

        /**
         Instantiates a new Handler.

         @param eventType
         the event type
         */
        Handler(Class<? extends Event> eventType) { this.eventType = eventType; }

        /**
         Adds an {@link InvokeWrapper invoker} to this handler.

         @param invoker
         the invoker

         @return {@code true} if this handler did not already contain the specified invoker
         */
        public boolean subscribe(InvokeWrapper invoker) {
            return invalidateCache(invokers.add(invoker));
        }

        /**
         Removes the specified {@link InvokeWrapper invoker} from this handler if it's present.

         @param invoker
         the invoker

         @return {@code true} if this handler contained the specified invoker
         */
        public boolean unsubscribe(InvokeWrapper invoker) {
            return invalidateCache(invokers.remove(invoker));
        }

        /**
         Posts an event to all registered listeners in this handler and its supertype handlers.

         @param event
         event to post
         */
        public void post(Event event) {
            InvokeWrapper[] cache = this.computedInvokerCache;
            if (cache == null) {
                synchronized (this) {
                    if ((cache = this.computedInvokerCache) == null) {
                        cache = this.computedInvokerCache = computeInvokerCache();
                    }
                }
            }

            for (InvokeWrapper invoker : cache) {
                invoker.invoke(event);
            }
        }

        /**
         Computes all invokers that need to be invoked when this handler received an event.

         @return the invoke wrapper [ ]
         */
        synchronized InvokeWrapper[] computeInvokerCache() {
            SortedSet<InvokeWrapper> set;
            if (hasSupertypeHandler()) {
                set = new TreeSet<>(this.invokers);
                for (Handler supertypeHandler : this.supertypeHandlers)
                    set.addAll(supertypeHandler.invokers);
            } else {
                set = this.invokers;
            }
            return set.toArray(new InvokeWrapper[0]);
        }

        /**
         Invalidates the {@link #computedInvokerCache} when {@code modified} is {@code true}.

         @param modified
         should we invalidate?

         @return same value as {@code modified}
         */
        boolean invalidateCache(boolean modified) {
            if (modified) this.computedInvokerCache = null;
            return modified;
        }

        /**
         Event type class.

         @return event type for this handler
         */
        public Class<? extends Event> eventType() {
            return eventType;
        }

        /**
         Determines if the event type for this handler
         is a subtype of the specified {@code Class} parameter.

         @param cls
         the {@code Class} object to be checked

         @return {@code true} if the event type for this handler         is a subtype of the type represented by {@code cls},         {@code false} otherwise
         */
        public boolean isSubtypeOf(Class<?> cls) {
            Class<? extends Event> type = eventType();
            return type != cls && cls.isAssignableFrom(type);
        }

        /**
         Determines if the event type for this handler is a subtype of
         the event type for the specified {@code handler} parameter.

         @param handler
         the {@code Handler} object to be checked

         @return {@code true} if the event type for this handler is a subtype of         the event type for the {@code handler} parameter,         {@code false} otherwise
         */
        public boolean isSubtypeOf(Handler handler) {
            return isSubtypeOf(handler.eventType());
        }

        /**
         Returns {@code true} if this hander has at least one supertype handler.

         @return the boolean
         */
        public boolean hasSupertypeHandler() {
            return !supertypeHandlers.isEmpty();
        }

        /**
         Gets an unmodifiable view of this handler's supertype handlers.

         @return an unmodifiable view
         */
        public Set<Handler> getSupertypeHandlers() {
            return Collections.unmodifiableSet(supertypeHandlers);
        }

        /**
         Adds a handler as this handler's supertype handler if it isn't already present.

         @param handler
         the supertype handler to be added

         @return {@code true} if any modification occurred, {@code false} otherwise.
         */
        boolean addSupertypeHandler(Handler handler) {
            if (handler == this) return false;
            return invalidateCache(supertypeHandlers.add(handler));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Handler)) return false;
            return Objects.equals(eventType, ((Handler) o).eventType);
        }

        @Override
        public int hashCode() {
            return eventType.hashCode();
        }

        @Override
        public String toString() {
            return String.format("Handler{%s}", eventType.getName());
        }
    }

    /**
     Listener method invocation wrapper.
     */
    static class InvokeWrapper implements Comparable<InvokeWrapper> {
        /**
         Compares Invokewrappers using their {@code priority} value,
         and only returns 0 when {@code o1.equals(o2)}.
         */
        public static final Comparator<InvokeWrapper> COMPARATOR = (o1, o2) -> {
            if (fastEqual(o1, o2)) return 0;

            // @formatter:off
            int c;
            if ((c = Integer.compare(o1.priority, o2.priority))                         != 0) return c;
            if ((c = o1.method.getName().compareTo(o2.method.getName()))                != 0) return c;
            if ((c = o1.eventType.getName().compareTo(o2.eventType.getName()))          != 0) return c;
            if ((c = Integer.compare(o1.listener.hashCode(), o2.listener.hashCode()))   != 0) return c;
            if ((c = Integer.compare(o1.hashCode(), o2.hashCode()))                     != 0) return c;
            // @formatter:on
            throw new AssertionError();  // ensures the comparator will never return 0 if the two wrapper aren't equal
        };

        /**
         Constructs an InvokeWrapper.

         @param instance
         the instance
         @param method
         the method
         @param lookup
         the lookup

         @return the invoke wrapper

         @throws SecurityException
         if the provided {@link Lookup lookup}                           cannot access the specified method
         */
        public static InvokeWrapper create(Object instance, Method method, Lookup lookup) throws SecurityException {
            int priority = AccessHelper.getAnnotationRecursively(method, Listener.class).value().getPriority();
            return create(instance, method, priority, lookup);
        }

        /**
         Constructs an InvokeWrapper with specified {@code priority} value.

         @param instance
         the instance
         @param method
         the method
         @param priority
         the priority
         @param lookup
         the lookup

         @return the invoke wrapper

         @throws SecurityException
         if the provided {@link Lookup lookup}                           cannot access the specified method
         */
        public static InvokeWrapper create(Object instance, Method method, int priority, Lookup lookup)
                throws SecurityException {
            Class<? extends Event> eventType = (Class<? extends Event>) method.getParameterTypes()[0];
            MethodHandle methodHandle = AccessHelper.unreflectMethodHandle(lookup, method);
            return new InvokeWrapper(instance, eventType, method, priority, methodHandle);
        }

        /**
         * Listener instance. Used in invocation.
         */
        private final Object listener;

        /**
         * Event type which the {@code listener} listens.
         */
        private final Class<? extends Event> eventType;

        /**
         * Listener method.
         */
        private final Method method;

        /**
         * Listener priority. Lower values are called first.
         */
        private final int priority;

        /**
         * {@link MethodHandle} for invocation.
         */
        private final MethodHandle methodHandle;

        /**
         Instantiates a new Invoke wrapper.

         @param listener
         the listener
         @param eventType
         the event type
         @param method
         the method
         @param priority
         the priority
         @param methodHandle
         the method handle
         */
        InvokeWrapper(Object listener, Class<? extends Event> eventType, Method method, int priority, MethodHandle methodHandle) {
            this.listener = listener;
            this.eventType = eventType;
            this.method = method;
            this.priority = priority;
            this.methodHandle = methodHandle;
        }

        /**
         Invokes the listener.

         @param event
         event to post

         @throws RuntimeException
         if the underlying listener method throws an exception
         */
        public void invoke(Event event) throws RuntimeException {
            try {
                methodHandle.invoke(listener, event);
            } catch (Throwable e) {
                DarwinServer.error("Failed to invoke method", e);
            }
        }

        /**
         * Compares this object with another InvokeWrapper for order.
         */
        @Override
        public int compareTo(InvokeWrapper o) {
            return COMPARATOR.compare(this, o);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof InvokeWrapper)) return false;
            return fastEqual(this, (InvokeWrapper) o);
        }

        @Override
        public int hashCode() {
            int n = 1;
            n = 31 * n + listener.hashCode();
            n = 31 * n + eventType.hashCode();
            n = 31 * n + method.hashCode();
            return n;
        }

        private static boolean fastEqual(InvokeWrapper o1, InvokeWrapper o2) {
            return Objects.equals(o1.listener, o2.listener) &&
                    Objects.equals(o1.eventType, o2.eventType) &&
                    Objects.equals(o1.method, o2.method);
        }

        @Override
        public String toString() {
            return String.format("InvokeWrapper{listener=%s, eventType=%s, method=%s(%s), priority=%d}",
                    listener, eventType.getName(), method.getName(), eventType.getSimpleName(), priority);
        }
    }
}
