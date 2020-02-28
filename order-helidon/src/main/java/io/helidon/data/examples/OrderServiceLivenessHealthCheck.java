package io.helidon.data.examples;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@Liveness
@ApplicationScoped
public class OrderServiceLivenessHealthCheck implements HealthCheck {

    @Inject
    public OrderServiceLivenessHealthCheck() {
    }

    @Override
    public HealthCheckResponse call() {
        if (!OrderResource.liveliness) {
            return HealthCheckResponse.named("OrderServerLivenessDown")
                    .down()
                    .withData("databaseconnections", "not live")
                    .build();
        } else return HealthCheckResponse.named("OrderServerLiveness")
                .up()
                .withData("databaseconnections", "live")
                .build();
    }
}

