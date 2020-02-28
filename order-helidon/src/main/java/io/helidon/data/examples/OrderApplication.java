package io.helidon.data.examples;


import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

@ApplicationScoped
@ApplicationPath("/")
public class OrderApplication extends Application {

    public OrderApplication() {
        System.out.println("OrderApplication (includes io.narayana.lra.filter.FilterRegistration)");
    }

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> s = new HashSet<Class<?>>();
        s.add(OrderResource.class);
        return s;
    }

}
