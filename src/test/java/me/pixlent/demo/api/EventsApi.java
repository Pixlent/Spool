package me.pixlent.demo.api;

import net.minestom.server.MinecraftServer;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.GlobalEventHandler;
import org.graalvm.polyglot.Value;

public class EventsApi {
    private static final EventsApi INSTANCE = new EventsApi();
    private final GlobalEventHandler eventHandler;
    private EventNode<Event> eventNode;

    private EventsApi() {
        this.eventHandler = MinecraftServer.getGlobalEventHandler();
        eventNode = EventNode.all("spool-events");
        eventHandler.addChild(eventNode);
    }

    public static EventsApi getInstance() {
        return INSTANCE;
    }

    public void subscribe(Class<? extends Event> event, Value callback) {
        eventNode.addListener(event, callback::execute);
        System.out.println("Subscribed to event " + event.getName());
    }

    public void clear() {
        eventNode = EventNode.all("spool-events");
        eventHandler.replaceChildren("spool-events", eventNode);
    }
}