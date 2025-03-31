package me.pixlent.signal;

@FunctionalInterface
public interface SignalListener<T> {
    void onSignal(T event);
}