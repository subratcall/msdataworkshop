package io.helidon.data.examples.service;


import io.helidon.config.Config;
import io.helidon.webserver.Routing.Rules;
import io.helidon.webserver.ServerRequest;
import io.helidon.webserver.ServerResponse;
import io.helidon.webserver.Service;

public class OrderStreamingService implements Service {


    public OrderStreamingService(Config config)  {

        String url = config.get("url").asString().get();
        System.out.printf("Using url: %s%n", url);
    }

    @Override
    public void update(Rules rules) {
        rules.get("/streamorders", this::streamOrders);
    }


    void streamOrders(ServerRequest serverRequest, ServerResponse serverResponse) {
        String numberofitemstostream = serverRequest.queryParams().first("numberoforderstostream").get();
        System.out.println("OrderStreamingService.addInventory numberofitemstostream:" + numberofitemstostream);
            new OrderServiceTestStreamingOrders(Integer.valueOf(numberofitemstostream)).run();
        serverResponse.send(numberofitemstostream + " test stream orders sent via OSS...");
    }



}