package com.helidon.se.persistence.queues;

public interface QueueProvider {

    String sendMessage(Object message, String owner, String queueName) throws Exception;

    String getMessage(String owner, String queueName) throws Exception;
}
