package com.revolut.interview.notification;

import com.google.inject.AbstractModule;

public class NotificationsModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(NotificationService.class).to(LoggerNotification.class);
    }
}
