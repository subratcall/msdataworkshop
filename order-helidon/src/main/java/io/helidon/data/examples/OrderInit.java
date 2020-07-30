package io.helidon.data.examples;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;

@ApplicationScoped
public class OrderInit {

    public void init(@Observes @Initialized(ApplicationScoped.class) Object init) {
        System.out.println("Order.init " + init);
    }

}

