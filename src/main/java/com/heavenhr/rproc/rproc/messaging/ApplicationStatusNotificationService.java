/**
 * @author kudoji
 */
package com.heavenhr.rproc.rproc.messaging;

import com.heavenhr.rproc.rproc.entities.Application;

public interface ApplicationStatusNotificationService {
    void sendNotification(Application application);
}
