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
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import oracle.ucp.jdbc.PoolDataSource;

@Path("/")
@ApplicationScoped
public class InventoryResource {

    @Inject
    @Named("inventorypdb")
    PoolDataSource atpInventoryPDB;

    private Connection conn = null;
    private InventoryServiceOrderEventConsumer oracleAQEventListener;
    // todo get these from env
    static String inventoryuser = "INVENTORYUSER";
    static String inventorypw = System.getenv("oracle.ucp.jdbc.PoolDataSource.inventorypdb.password");
    static String inventoryQueueName = "inventoryqueue";
    static String orderQueueName = "orderqueue";
    static boolean isDirectSupplierQuickTest = Boolean.valueOf(System.getProperty("isDirectSupplierQuickTest", "false"));

    static {
        System.setProperty("oracle.jdbc.fanEnabled", "false");
    }

    public void init(@Observes @Initialized(ApplicationScoped.class) Object init) {
        System.out.println("InventoryResource.init " + init);
        listenForMessages();
    }

    @Path("/listenForMessages")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response listenForMessages()  {
        new Thread(new InventoryServiceOrderEventConsumer(this)).start();
        final Response returnValue = Response.ok()
                .entity("now listening for messages...")
                .build();
        return returnValue;
    }

    public static void main(String args[]) {
        new Thread(new InventoryServiceOrderEventConsumer(new InventoryResource())).start();
        System.out.println("InventoryResource.main now listening for messages...");
    }

    // for quick test, bypassing supplier...
    static int inventorycount;

    @Path("/addInventory")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response addInventory(@QueryParam("itemid") String itemid) throws Exception {
        System.out.println("--->addInventory for itemid:" + itemid);
        String returnString = "--->addInventory for itemid:" + itemid + " inventoryCount now " + ++inventorycount;
        final Response returnValue = Response.ok()
                .entity(returnString)
                .build();
        return returnValue;
    }

    @Path("/removeInventory")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response removeInventory(@QueryParam("itemid") String itemid) throws Exception {
        System.out.println("--->removeInventory for item");
        String returnString = "--->removeInventory for itemid:" + itemid + " inventoryCount now " + --inventorycount;
        final Response returnValue = Response.ok()
                .entity(returnString)
                .build();
        return returnValue;
    }

    @Path("/getInventory")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response getInventory(@QueryParam("itemid") String itemid) throws Exception {
        System.out.println("--->getInventory for item");
        String returnString = "--->getInventory for itemid:" + itemid + " inventoryCount  " + inventorycount;
        final Response returnValue = Response.ok()
                .entity(returnString)
                .build();
        return returnValue;
    }

}
