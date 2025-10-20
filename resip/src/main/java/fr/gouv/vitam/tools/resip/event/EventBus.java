package fr.gouv.vitam.tools.resip.event;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class EventBus {

    private static final Map<Class<? extends Event>, List<EventListener<?>>> listenersMap = new ConcurrentHashMap<>();

    public static <T extends Event> void subscribe(Class<T> eventType, EventListener<T> listener) {
        listenersMap
            .computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>())
            .add(listener);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Event> void publish(T event) {
        List<EventListener<?>> listeners = listenersMap.getOrDefault(event.getClass(), Collections.emptyList());
        for (EventListener<?> listener : listeners) {
            ((EventListener<T>) listener).onEvent(event);
        }
    }

    public static <T extends Event> void unsubscribe(Class<T> eventType, EventListener<T> listener) {
        List<EventListener<?>> listeners = listenersMap.get(eventType);
        if (listeners != null) {
            listeners.remove(listener);
        }
    }
}
