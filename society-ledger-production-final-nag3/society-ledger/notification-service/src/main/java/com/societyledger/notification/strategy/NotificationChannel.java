package com.societyledger.notification.strategy;

/**
 * Strategy interface for notification delivery channels.
 *
 * Current: Email (stub)
 * Planned: WhatsApp, Push Notifications
 *
 * Adding a new channel = implement this interface + register in NotificationDispatchService.
 */
public interface NotificationChannel {

    String getChannelName();

    void send(NotificationPayload payload);

    boolean isEnabled();
}
