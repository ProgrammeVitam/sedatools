package fr.gouv.vitam.tools.resip.event;

@FunctionalInterface
public interface EventListener<T extends Event> {
    void onEvent(T event);
}
