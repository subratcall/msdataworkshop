package io.helidon.data.examples;

import io.helidon.security.providers.httpauth.SecureUserStore;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Destroyed;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import java.util.HashMap;

@ApplicationScoped
public class InventoryInit {

    public void init(@Observes @Initialized(ApplicationScoped.class) Object init) {
        System.out.println("InventoryInit.init " + init);
    }

}

