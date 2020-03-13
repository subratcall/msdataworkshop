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
import org.eclipse.microprofile.opentracing.Traced;

@Path("/")
@ApplicationScoped
@Traced
public class OrderResource {

    @Inject
    @Named("orderpdb")
    PoolDataSource atpOrderPdb;

    private OrderServiceEventConsumer orderServiceEventConsumer = new OrderServiceEventConsumer();
    private OrderServiceEventProducer orderServiceEventProducer = new OrderServiceEventProducer();
    static final String orderQueueOwner = "orderuser"; // System.getenv("oracle.ucp.jdbc.PoolDataSource.orderpdb.user");
    static final String orderQueueName = "orderqueue"; // System.getenv("orderqueuename");
    static final String inventoryQueueName = "inventoryqueue"; // System.getenv("inventoryqueuename");
    static boolean liveliness = true;
    private static String lastContainerStartTime;
    OrderServiceCPUStress orderServiceCPUStress = new OrderServiceCPUStress();
    Map<String, OrderDetail> orders = new HashMap<>();

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

    @Path("/listenForMessages")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response dequeue() throws Exception {
        orderServiceEventConsumer.dataSource = atpOrderPdb;
        new Thread(orderServiceEventConsumer).start();
        final Response returnValue = Response.ok()
                .entity("listening for messages on inventory queue...")
                .build();
        return returnValue;
    }

    @Path("/showorder")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response showorder(@QueryParam("order") String orderId) throws Exception {
        System.out.println("--->showorder for orderId:" + orderId);
        OrderDetail orderDetail = orders.get(orderId);
        if (orderDetail == null) {
            String inventoryStatus = "nullstatus"; // orderServiceEventConsumer.dolistenForMessages(atpOrderPdb, orderId).toString();
            orderDetail = new OrderDetail();
            orders.put(orderId, orderDetail);
            if (inventoryStatus.equals("inventoryexists")) {
                orderDetail.setOrderStatus("successful");
                orderDetail.setSuggestiveSaleItem("suggestiveSaleItem"); //todo get from dolistenForMessages
                orderDetail.setInventoryLocation("inventoryLocation"); //todo get from dolistenForMessages
            } else if (inventoryStatus.equals("inventorydoesnotexist")) {
                orderDetail.setOrderStatus("failed");
            }
        }
//        System.out.println("--->inventoryStatus..." + inventoryStatus);
        String returnString = orderDetail == null? "orderId not found:" + orderId :
                "orderId = " + orderId + "<br>orderstatus = " + orderDetail.getOrderStatus() +
                        "<br>suggestiveSale (event sourced from catalog) = " + orderDetail.getSuggestiveSale() +
                        "<br>inventoryLocation (event sourced from supplier) = " + orderDetail.getInventoryLocation();
        final Response returnValue = Response.ok()
            .entity(returnString)
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
    public Response placeOrder(@QueryParam("orderid") String orderid, @QueryParam("itemid") String itemid,
                               @QueryParam("deliverylocation") String deliverylocation) throws Exception {
        System.out.println("--->placeOrder... orderid:" + orderid + " itemid:" + itemid);
//        itemid(Integer.valueOf(widget));
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrderStatus("pending");
        orderDetail.setDeliveryLocation(deliverylocation);
        orders.put(orderid, orderDetail);
        System.out.println("--->insertOrderAndSendEvent..." +
                orderServiceEventProducer.updateDataAndSendEvent( atpOrderPdb,  orderid, itemid, deliverylocation));
        final Response returnValue = Response.ok()
            .entity("orderid = " + orderid + " orderstatus = " + orderDetail.getOrderStatus() + " order placed")
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


    class OrderDetail {
        private int orderId = -1;
        private String suggestiveSaleItem = "";
        private String suggestiveSale = "";
        private String inventoryLocationItem = "";
        private String inventoryLocation = "none";
        private String shippingEstimate = "none";
        private String shippingEstimateItem = "";
        private String orderStatus = "none";
        private String deliveryLocation = "none";

        public int getOrderId() {
            return orderId;
        }

        public void setOrderId(int orderId) {
            this.orderId = orderId;
        }

        public String getSuggestiveSaleItem() {
            return suggestiveSaleItem;
        }

        public void setSuggestiveSaleItem(String suggestiveSaleItem) {
            this.suggestiveSaleItem = suggestiveSaleItem;
        }

        public String getSuggestiveSale() {
            return suggestiveSale;
        }

        public void setSuggestiveSale(String suggestiveSale) {
            this.suggestiveSale = suggestiveSale;
        }

        public String getInventoryLocationItem() {
            return inventoryLocationItem;
        }

        public void setInventoryLocationItem(String inventoryLocationItem) {
            this.inventoryLocationItem = inventoryLocationItem;
        }

        public String getInventoryLocation() {
            return inventoryLocation;
        }

        public void setInventoryLocation(String inventoryLocation) {
            this.inventoryLocation = inventoryLocation;
        }

        public String getShippingEstimate() {
            return shippingEstimate;
        }

        public void setShippingEstimate(String shippingEstimate) {
            this.shippingEstimate = shippingEstimate;
        }

        public String getShippingEstimateItem() {
            return shippingEstimateItem;
        }

        public void setShippingEstimateItem(String shippingEstimateItem) {
            this.shippingEstimateItem = shippingEstimateItem;
        }

        public String getOrderStatus() {
            return orderStatus;
        }

        public void setOrderStatus(String orderStatus) {
            this.orderStatus = orderStatus;
        }

        public void setDeliveryLocation(String deliverylocation) {
            this.deliveryLocation = deliverylocation;
        }

        public String getDeliveryLocation() {
            return deliveryLocation;
        }
    }
}
