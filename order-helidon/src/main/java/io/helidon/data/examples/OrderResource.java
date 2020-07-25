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
import io.opentracing.Tracer;
import io.opentracing.Span;

@Path("/")
@ApplicationScoped
@Traced
public class OrderResource {

    @Inject
    @Named("orderpdb")
    PoolDataSource atpOrderPdb;


    @Inject
    private Tracer tracer;

    private boolean isOrderEventConsumerStarted = false;
    OrderServiceEventProducer orderServiceEventProducer = new OrderServiceEventProducer();
    static final String orderQueueOwner = "ORDERUSER";
    static final String orderQueueName = "orderqueue";
    static final String inventoryQueueName = "inventoryqueue";
    static boolean liveliness = true;
    private static final String lastContainerStartTime;
    private OrderServiceCPUStress orderServiceCPUStress = new OrderServiceCPUStress();
    Map<String, OrderDetail> cachedOrders = new HashMap<>();

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

    @Path("/placeOrder")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Traced(operationName = "OrderResource.placeOrder")
    @Timed(name = "placeOrder_timed") //length of time of an object
    @Counted(name = "placeOrder_counted") //amount of invocations
    public Response placeOrder(@QueryParam("orderid") String orderid, @QueryParam("itemid") String itemid,
                               @QueryParam("deliverylocation") String deliverylocation) {
        System.out.println("--->placeOrder... orderid:" + orderid + " itemid:" + itemid);
        startEventConsumerIfNotStarted();
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrderId(orderid);
        orderDetail.setOrderStatus("pending");
        orderDetail.setDeliveryLocation(deliverylocation);
        cachedOrders.put(orderid, orderDetail);

        Span activeSpan = tracer.buildSpan("orderDetail").asChildOf(tracer.activeSpan()).start();
        activeSpan.log("begin placing order"); // logs are for a specific moment or event within the span (in contrast to tags which should apply to the span regardless of time).
        activeSpan.setTag("orderid", orderid); //tags are annotations of spans in order to query, filter, and comprehend trace data
        activeSpan.setTag("itemid", itemid);
        activeSpan.setTag("db.user", atpOrderPdb.getUser()); // https://github.com/opentracing/specification/blob/master/semantic_conventions.md
        activeSpan.setBaggageItem("sagaid", "testsagaid" + orderid); //baggage is part of SpanContext and carries data across process boundaries for access throughout the trace
        activeSpan.setBaggageItem("orderid", orderid);

        try {
            System.out.println("--->insertOrderAndSendEvent..." +
                    orderServiceEventProducer.updateDataAndSendEvent(atpOrderPdb, orderid, itemid, deliverylocation));
        } catch (Exception e) {
            e.printStackTrace();
            return Response.ok()
                    .entity("orderid = " + orderid + " failed with exception:" + e.toString())
                    .build();
        }

        activeSpan.log("end placing order");

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
    @Produces(MediaType.APPLICATION_JSON)
    public Response showorder(@QueryParam("orderid") String orderId) {
        System.out.println("--->showorder for orderId:" + orderId);
        OrderDetail orderDetail = cachedOrders.get(orderId);
        if (orderDetail == null || orderDetail.getOrderStatus().equals("pending")) {
            System.out.println("--->showorder not in cache for orderId:" + orderId + " orderDetail:" + orderDetail + " querying DB...");
            return showordernocache(orderId);
        }
        String returnJSON = JsonUtils.writeValueAsString(new Order(orderDetail));
        System.out.println("OrderResource.showorder returnJSON:" + returnJSON);
        return Response.ok()
                .entity(returnJSON)
                .build();
    }

    @Path("/showordernocache")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response showordernocache(@QueryParam("orderid") String orderId) {
        System.out.println("--->showorder (via JSON/SODA query) for orderId:" + orderId);
        try {
            Order order = orderServiceEventProducer.getOrderViaSODA(atpOrderPdb, orderId);
            String returnJSON = JsonUtils.writeValueAsString(order);
            System.out.println("OrderResource.showordernocache returnJSON:" + returnJSON);
            return Response.ok()
                    .entity(returnJSON)
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.ok()
                    .entity("showorder orderid = " + orderId + " failed with exception:" + e.toString())
                    .build();
        }

    }

    @Path("/showallorders")
    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response showallorders() {
        System.out.println("showallorders...");
        StringBuilder returnString = new StringBuilder("orders in cache...");
        for (String order : cachedOrders.keySet()) {
            returnString.append("<br>");
            returnString.append(cachedOrders.get(order));
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

    @Path("/deleteallorders")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response deleteallorders() {
        System.out.println("--->deleteallorders");
        try {
            String returnString = "deleteallorders " + orderServiceEventProducer.dropOrderViaSODA(atpOrderPdb);
            return Response.ok()
                    .entity(returnString)
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.ok()
                    .entity("deleteallorders failed with exception:" + e.toString())
                    .build();
        }
    }

    @Path("/ordersetlivenesstofalse")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response ordersetlivenesstofalse() {
        liveliness = false;
        return Response.ok()
                .entity("order liveness set to false - OKE should restart the pod due to liveness probe")
                .build();
    }

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

}