package me.pixlent.signal;

import org.graalvm.polyglot.Value;

public class SignalValueListener<T> implements SignalListener<T> {
    private final Value listener;

    SignalValueListener(Value listener) {
        this.listener = listener;
    }

    @Override
    public void onSignal(T event) {
        if (listener.canExecute()) {
            listener.execute(event);
        }
    }
}
