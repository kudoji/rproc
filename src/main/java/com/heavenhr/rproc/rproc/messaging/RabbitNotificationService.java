/**
 * @author kudoji
 */
package com.heavenhr.rproc.rproc.messaging;

import com.heavenhr.rproc.rproc.entities.Application;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RabbitNotificationService implements ApplicationStatusNotificationService{
    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public RabbitNotificationService(RabbitTemplate rabbitTemplate){
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendNotification(Application application){
        Notification notification = new Notification(application);
        log.info("attempting to send a notification due to application status change: {}", notification);

        MessageConverter messageConverter = rabbitTemplate.getMessageConverter();
        MessageProperties messageProperties = new MessageProperties();
        Message message = messageConverter.toMessage(
                notification,
                messageProperties);

        rabbitTemplate.send(message);
    }
}
