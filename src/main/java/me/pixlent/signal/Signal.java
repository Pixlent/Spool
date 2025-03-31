package me.pixlent.signal;

public interface Signal<T> {
    void emit(T event);
    void subscribe(SignalListener<T> listener);
    void unsubscribe(SignalListener<T> listener);
}