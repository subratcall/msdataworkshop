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

import java.sql.Connection;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import oracle.ucp.jdbc.PoolDataSource;
import org.eclipse.microprofile.health.Liveness;
import org.eclipse.microprofile.health.Readiness;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.opentracing.Traced;

@Path("/")
@ApplicationScoped
@Traced
public class OrderResource {

    @Inject
    @Named("atp1")
    PoolDataSource atpOrderPdb;

    @Inject
    @Named("atpinventorypdb")
    PoolDataSource atpInventoryPdb;

    private OrderServiceEventConsumer orderServiceEventConsumer = new OrderServiceEventConsumer();
    private OrderServiceEventProducer orderServiceEventProducer = new OrderServiceEventProducer();
    static final String orderQueueOwner = System.getenv("oracle.ucp.jdbc.PoolDataSource.atp1.user");
    static final String orderQueueName = System.getenv("orderqueuename");
    static final String inventoryQueueName = System.getenv("inventoryqueuename");
    private String orderStatus = "none";
    static boolean liveliness = true;
    private int orderId = -1;
    private String suggestiveSaleItem = "";
    private String suggestiveSale = "";
    private String inventoryLocationItem = "";
    private String inventoryLocation = "none";
    private String shippingEstimate = "none";
    private String shippingEstimateItem = "";
    private static String lastContainerStartTime;
    OrderServiceCPUStress orderServiceCPUStress = new OrderServiceCPUStress();

    static {
        lastContainerStartTime = new java.util.Date().toString();
        System.out.println("____________________________________________________");
        System.out.println("----------->OrderResource (container) starting at: " + lastContainerStartTime );
        System.out.println("____________________________________________________");
    }

    @Path("/lastContainerStartTime")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response lastContainerStartTime() throws Exception {
        System.out.println("--->lastContainerStartTime...");
        final Response returnValue = Response.ok()
            .entity("lastContainerStartTime = " + lastContainerStartTime)
            .build();
        return returnValue;
    }

    @Path("/showorder")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response showorder(@QueryParam("order") String message) throws Exception {
        System.out.println("--->showorder...");
        final Response returnValue = Response.ok()
            .entity("orderId = " + orderId + "<br>orderstatus = " + orderStatus +
                    "<br>suggestiveSale (event sourced from catalog) = " + suggestiveSale +
                    "<br>inventoryLocation (event sourced from supplier) = " + inventoryLocation +
                    "<br>shipping estimate (event sourced from supplier) = " + shippingEstimate)
            .build();
        return returnValue;
    }

    @Path("/placeOrder")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Traced(operationName = "OrderResource.placeOrder")
    @Counted(name = "placeOrder_counted") //amount of invocations
//    @Metered(name = "placeOrder_metered") //invocation frequency
//    @Timed(name = "placeOrder_timed") //length of time of an object
    public Response placeOrder(@QueryParam("orderid") String orderid, @QueryParam("itemid") String itemid) throws Exception {
        System.out.println("--->placeOrder... orderid:" + orderid + " itemid:" + itemid);
        orderId = Integer.valueOf(orderid);
//        itemid(Integer.valueOf(widget));
        System.out.println("--->insertOrderAndSendEvent..." +
                orderServiceEventProducer.updateDataAndSendEvent( atpInventoryPdb,  orderid, itemid));
        String inventoryStatus = orderServiceEventConsumer.dolistenForMessages(atpInventoryPdb, orderid).toString();
        if (inventoryStatus.equals("inventoryexists")) {
            orderStatus = "successful";
            suggestiveSale = suggestiveSaleItem;
            inventoryLocation = inventoryLocationItem;
            shippingEstimate = shippingEstimateItem;
        }
        else if (inventoryStatus.equals("inventorydoesnotexist")) {
            orderStatus = "failed";
            suggestiveSale = "";
            inventoryLocation = "";
            shippingEstimate = "";
        }
        System.out.println("--->inventoryStatus..." + inventoryStatus);
        final Response returnValue = Response.ok()
            .entity("orderid = " + orderid + " orderstatus = " + orderStatus + " inventoryStatus = " + inventoryStatus)
            .build();
        return returnValue;
    }

//    @Gauge ...
//    int itemid(int itemid) {
//        return itemid;
//    }


    @Path("/consumeStreamOrders")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response consumeStreamOrders()  {
        new Thread(new OrderServiceOSSStreamProcessor(this)).start();
        final Response returnValue = Response.ok()
                .entity("now consuming orders streamed from OSS...")
                .build();
        return returnValue;
    }


    @Path("/ordersetlivenesstofalse")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response ordersetlivenesstofalse()  {
        liveliness = false;
        final Response returnValue = Response.ok()
                .entity("order liveness set to false - OKE should restart the pod due to liveness probe")
                .build();
        return returnValue;
    }


    @Path("/startCPUStress")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response startCPUStress() throws Exception {
        System.out.println("--->startCPUStress...");
        orderServiceCPUStress.start();
        final Response returnValue = Response.ok()
                .entity("CPU stress started")
                .build();
        return returnValue;
    }

    @Path("/stopCPUStress")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response stopCPUStress() throws Exception {
        System.out.println("--->startCPUStress...");
        orderServiceCPUStress.stop();
        final Response returnValue = Response.ok()
                .entity("CPU stress stopped")
                .build();
        return returnValue;
    }

}
