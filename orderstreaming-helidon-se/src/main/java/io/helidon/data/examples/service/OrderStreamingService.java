package io.helidon.data.examples.service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import io.helidon.config.Config;
import io.helidon.webserver.Routing.Rules;
import io.helidon.webserver.ServerRequest;
import io.helidon.webserver.ServerResponse;
import io.helidon.webserver.Service;
import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;

public class OrderStreamingService implements Service {

    private PoolDataSource pool = null;

    public OrderStreamingService(Config config) throws SQLException {

        String url = config.get("url").asString().get();
        String user = config.get("user").asString().get();
        String password = config.get("password").asString().get();
        System.out.printf("Using url: %s%n", url);
        pool = PoolDataSourceFactory.getPoolDataSource();
        pool.setURL(url);
        pool.setUser(user);
        pool.setPassword(password);
        pool.setConnectionFactoryClassName("oracle.jdbc.pool.OracleDataSource");
    }

    @Override
    public void update(Rules rules) {
        rules
                .get("/streamorders", this::streamOrders);
    }


    void streamOrders(ServerRequest serverRequest, ServerResponse serverResponse) {
        String numberofitemstostream = serverRequest.queryParams().first("numberoforderstostream").get();
        System.out.println("OrderStreamingService.addInventory numberofitemstostream:" + numberofitemstostream);
            new OrderServiceTestStreamingOrders(numberofitemstostream).run();
        serverResponse.send(numberofitemstostream + " test stream orders sent via OSS...");
//            final Response returnValue = Response.ok()
//                    .entity(numberofitemstostream + " test stream orders sent via OSS...")
//                    .build();
//        serverResponse.send(returnValue);
    }



}