package io.helidon.data.examples;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@Readiness
@ApplicationScoped
public class OrderServiceReadinessHealthCheck implements HealthCheck {

    @Inject
    public OrderServiceReadinessHealthCheck() {
    }

    @Override
    public HealthCheckResponse call() {
        String message = "ready";
        return HealthCheckResponse.named("OrderServerReadiness")
                .up()
                .withData("data-initialized", message) //data initialized via eventsourcing, view query, etc.
                .withData("connections-created", message)
                .build();
    }
}
