package com.revolut.interview.notification;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Singleton;

@Singleton
class NOOPNotificationService implements NotificationService {

    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public void sendNotification(TransactionNotification notification) {
        if (notification.success) {
            LOGGER.info("Will notify sender {} about success and receiver {} about amount {} received.", notification.senderId, notification.receiverId, notification.amount);
        } else {
            LOGGER.info("Will notify sender {} about failure sending amount {} to {}.", notification.senderId, notification.amount, notification.receiverId);
        }
    }
}
