/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.helidon.data.examples;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import oracle.ucp.jdbc.PoolDataSource;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.opentracing.Traced;

@Path("/")
@ApplicationScoped
@Traced
public class OrderResource {

    @Inject
    @Named("orderpdb")
    PoolDataSource atpOrderPdb;

    private boolean isOrderEventConsumerStarted = false;
    private OrderServiceEventProducer orderServiceEventProducer = new OrderServiceEventProducer();
    static final String orderQueueOwner = "ORDERUSER";
    static final String orderQueueName = "orderqueue";
    static final String inventoryQueueName = "inventoryqueue";
    static boolean liveliness = true;
    private static String lastContainerStartTime;
    private OrderServiceCPUStress orderServiceCPUStress = new OrderServiceCPUStress();
    Map<String, OrderDetail> orders = new HashMap<>();

    //Task 11 (Helidon/OKE health liveness/readiness)
    static {
        lastContainerStartTime = new java.util.Date().toString();
        System.out.println("____________________________________________________");
        System.out.println("----------->OrderResource (container) starting at: " + lastContainerStartTime);
        System.out.println("____________________________________________________");
        System.setProperty("oracle.jdbc.fanEnabled", "false");
    }

    @Path("/lastContainerStartTime")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response lastContainerStartTime() {
        System.out.println("--->lastContainerStartTime...");
        return Response.ok()
                .entity("lastContainerStartTime = " + lastContainerStartTime)
                .build();
    }
    //END Task 11 (Helidon/OKE health liveness/readiness)

    //Task 9 (Demonstrate Converged database, Event-driven Order/Inventory Saga, Event Sourcing, CQRS, etc. via Order/Inventory store application)
    @Path("/placeOrder")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Traced(operationName = "OrderResource.placeOrder")
    @Counted(name = "placeOrder_counted") //amount of invocations
    @Timed(name = "placeOrder_timed") //length of time of an object
//    @Metered(name = "placeOrder_metered") //invocation frequency
    public Response placeOrder(@QueryParam("orderid") String orderid, @QueryParam("itemid") String itemid,
                               @QueryParam("deliverylocation") String deliverylocation) {
        System.out.println("--->placeOrder... orderid:" + orderid + " itemid:" + itemid);
        startEventConsumerIfNotStarted();
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrderId(orderid);
        orderDetail.setOrderStatus("pending");
        orderDetail.setDeliveryLocation(deliverylocation);
        orders.put(orderid, orderDetail);
        try {
            System.out.println("--->insertOrderAndSendEvent..." +
                    orderServiceEventProducer.updateDataAndSendEvent(atpOrderPdb, orderid, itemid, deliverylocation));
        } catch (Exception e) {
            e.printStackTrace();
            return Response.ok()
                    .entity("orderid = " + orderid + " failed with exception:" + e.toString())
                    .build();
        }
        return Response.ok()
                .entity("orderid = " + orderid + " orderstatus = " + orderDetail.getOrderStatus() + " order placed")
                .build();
    }

    private void startEventConsumerIfNotStarted() {
        System.out.println("OrderResource.startEventConsumerIfNotStarted isOrderEventConsumerStarted:" + isOrderEventConsumerStarted);
        if (!isOrderEventConsumerStarted) {
            OrderServiceEventConsumer orderServiceEventConsumer = new OrderServiceEventConsumer(this);
            new Thread(orderServiceEventConsumer).start();
            isOrderEventConsumerStarted = true;
        }
    }

    @Path("/showorder")
    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response showorder(@QueryParam("orderid") String orderId) {
        System.out.println("--->showorder for orderId:" + orderId);
        OrderDetail orderDetail = orders.get(orderId); //we can also lookup orderId if is null and we do order population lazily
        String returnString = orderDetail == null ? "orderId not found:" + orderId :
                "orderId = " + orderId + "<br>orderDetail... " + orderDetail;
        return Response.ok()
                .entity(returnString)
                .build();
    }

    @Path("/showallorders")
    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response showallorders() {
        System.out.println("showallorders...");
        StringBuilder returnString = new StringBuilder("orders in cache...");
        for (String order : orders.keySet()) {
            returnString.append("<br>");
            returnString.append(orders.get(order));
        }
        return Response.ok()
                .entity(returnString.toString())
                .build();
    }


    @Path("/deleteorder")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response deleteorder(@QueryParam("orderid") String orderId) {
        System.out.println("--->deleteorder for orderId:" + orderId);
        String returnString = "orderId = " + orderId + "<br>";
        try {
            returnString += orderServiceEventProducer.deleteOrderViaSODA(atpOrderPdb, orderId);
            return Response.ok()
                    .entity(returnString)
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.ok()
                    .entity("orderid = " + orderId + " failed with exception:" + e.toString())
                    .build();
        }
    }

    //END Task 9 (Demonstrate Converged database, Event-driven Order/Inventory Saga, Event Sourcing, CQRS, etc. via Order/Inventory store application)


    //Task 10 (OSS streaming service)
    @Path("/consumeStreamOrders")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response consumeStreamOrders() {
        new Thread(new OrderServiceOSSStreamProcessor(this)).start();
        return Response.ok()
                .entity("now consuming orders streamed from OSS...")
                .build();
    }
    //END Task 10 (OSS streaming service)

    //Task 11 (Helidon/OKE health liveness/readiness)
    @Path("/ordersetlivenesstofalse")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response ordersetlivenesstofalse() {
        liveliness = false;
        return Response.ok()
                .entity("order liveness set to false - OKE should restart the pod due to liveness probe")
                .build();
    }
    //END Task 11 (Helidon/OKE health liveness/readiness)

    //Task 12 (Demonstrate OKE horizontal pod scaling)
    @Path("/startCPUStress")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response startCPUStress() {
        System.out.println("--->startCPUStress...");
        orderServiceCPUStress.start();
        return Response.ok()
                .entity("CPU stress started")
                .build();
    }

    @Path("/stopCPUStress")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response stopCPUStress() {
        System.out.println("--->stopCPUStress...");
        orderServiceCPUStress.stop();
        return Response.ok()
                .entity("CPU stress stopped")
                .build();
    }
    //END Task 12 (Demonstrate OKE horizontal pod scaling)

}