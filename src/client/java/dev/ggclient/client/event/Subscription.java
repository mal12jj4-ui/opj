package dev.ggclient.client.event;

@FunctionalInterface
public interface Subscription {
    void cancel();
}
