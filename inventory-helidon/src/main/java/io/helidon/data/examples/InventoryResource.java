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

@Path("/")
@ApplicationScoped
public class InventoryResource {


    @Inject
    @Named("inventorypdb")
    PoolDataSource atpInventoryPDB;

    private Connection conn = null;
    private InventoryServiceInitialization dbandMessagingInitialization= new InventoryServiceInitialization();
    private InventoryServiceOrderEventConsumer oracleAQEventListener;
    // todo get these from env
    static String inventoryuser = "inventoryuser";
    static String inventorypw = "Welcome12345";
    static String inventoryQueueName = "inventoryqueue";

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

}
