package me.pixlent.signal;

import java.util.concurrent.CopyOnWriteArrayList;

public abstract class AbstractSignal<T> implements Signal<T> {
    private final CopyOnWriteArrayList<SignalListener<T>> listeners = new CopyOnWriteArrayList<>();

    @Override
    public void emit(T event) {
        for (SignalListener<T> listener : listeners) {
            listener.onSignal(event);
        }
    }

    @Override
    public void subscribe(SignalListener<T> listener) {
        listeners.addIfAbsent(listener);
    }

    @Override
    public void unsubscribe(SignalListener<T> listener) {
        listeners.remove(listener);
    }
}