package com.helidon.se.persistence.queues;

import com.helidon.se.persistence.context.DBContext;

public interface MessageConsumer {

    void accept(DBContext context, String message) throws Exception;
}
