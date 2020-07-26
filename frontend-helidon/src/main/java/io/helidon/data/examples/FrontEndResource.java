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

import org.eclipse.microprofile.opentracing.Traced;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Scanner;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.helidon.common.configurable.Resource;



@Path("/")
@ApplicationScoped
@Traced
public class FrontEndResource {

 /* -------------------------------------------------------
     * JET UI Entry point 
     * -------------------------------------------------------*/

    @Path("/")
    @GET
    @Produces(MediaType.TEXT_HTML)
    public String home() {
        String indexFile = Resource.create("web/index.html").string();
        return indexFile;
    }   


    /* -------------------------------------------------------
     * JET UI supporting endpoints to return the various static 
     * resources used by the UI
     * -------------------------------------------------------*/

    @Path("/styles")
    @GET
    @Produces("text/css")
    public String uiStyles() {
        return Resource.create("web/styles.css").string();
    }  
    @Path("/img")
    @GET
    @Produces("image/png")
    public Response uiImage(@QueryParam("name") String imageName) {
        try {
            return Response.ok(Resource.create("web/images/" + imageName +".png").stream()).build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }    

    @Path("/logo")
    @GET
    @Produces("image/svg+xml")
    public Response logoImage() {
        try {
            return Response.ok(Resource.create("web/images/oracle-logo-dark.svg").stream()).build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }     

         /* -------------------------------------------------------------------------
      * JET UI supporting wrapper endpoints - we could make these calls
      * Directly from the JS code, however, this way, the UI is abstracted from 
      * having to know ultimately where the backend services are living 
      * -------------------------------------------------------------------------*/
      @POST
      @Consumes(MediaType.APPLICATION_JSON)
      @Produces(MediaType.APPLICATION_JSON)
      @Path("/placeorder")
      public String placeorder(Command command) {
          try {
              System.out.println("FrontEndResource.serviceName " + command.serviceName);
              System.out.println("FrontEndResource.commandName " + command.commandName);
              URL url = new URL("http://order.msdataworkshop:8080/placeOrder?orderid=" + command.orderId +
                      "&itemid=" + command.orderItem + "&deliverylocation=" + URLEncoder.encode(command.deliverTo, "UTF-8"));
              String json = makeRequest(url);
              System.out.println("FrontEndResource.placeorder json:" + json);
              System.out.println("FrontEndResource.placeorder complete, now show order...");
              url = new URL("http://order.msdataworkshop:8080/showorder?orderid=" + command.orderId );
              json = makeRequest(url);
              System.out.println("FrontEndResource.placeorder showorder json:" + json);
              return json;
          } catch (IOException e) {
              e.printStackTrace();
              return "\"error\":\"" + e.getMessage() +"\"";
          }
  
      }


    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/deleteallorders")
    public String deleteallorders(Command command) {
        System.out.println("FrontEndResource.deleteallorders " + command.serviceName);
        System.out.println("FrontEndResource.deleteallorders " + command.commandName);
        System.out.println("-----> FrontEnd orderservicecall deleteallorders");
        try {
            URL url = new URL("http://order.msdataworkshop:8080/deleteallorders");
            String responseString = makeRequest(url);
            System.out.println("-----> FrontEnd deleteallorders responseString:" + responseString);
            return responseString;
        } catch (IOException e) {
            e.printStackTrace();
            return exceptionMessage(e);
        }
    }

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/command")
    public String command(Command command) {
        System.out.println("FrontEndResource.command " + command.serviceName);
        System.out.println("FrontEndResource.command " + command.commandName);
        try {
            URL url = new URL("http://" + command.serviceName + ".msdataworkshop:8080/" + command.commandName + "?orderid=" + command.orderId );
            return makeRequest(url);
        } catch (IOException e) {
            e.printStackTrace();
            return exceptionMessage(e);
        }
    }



    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/inventoryservicetest")
    public String inventoryservicetest(@QueryParam("test") String test) {
        try {
            URL url = new URL("http://inventory.msdataworkshop:8080/" + test);
            return makeRequest(url);
        } catch (IOException e) {
            e.printStackTrace();
            return exceptionMessage(e);
        }
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/inventoryservicetestwithitem")
    public String inventoryservicetestwithitem(@QueryParam("test") String test, @QueryParam("itemid") String itemid) {
        try {
            URL url = new URL("http://inventory.msdataworkshop:8080/" + test + "?itemid=" + itemid);
            return makeRequest(url);
        } catch (IOException e) {
            e.printStackTrace();
            return exceptionMessage(e);
        }
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/adminservicetest")
    public String adminservicetest(@QueryParam("test") String test) {
        try {
            String urlString = "http://atpaqadmin.msdataworkshop:8080/" + test;
            System.out.println("FrontEndResource.adminservicetest calling");
            URL url = new URL(urlString);
            return makeRequest(url);
        } catch (IOException e) {
            e.printStackTrace();
            return exceptionMessage(e);
        }
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/supplierservicecall")
    public String supplierservicecall(@QueryParam("test") String test, @QueryParam("itemid") String itemid) {
        try { //http://localhost:8080/supplier/getInventory?itemid=cucumbers
            String urlString = "http://supplier.msdataworkshop:8080/supplier/" + test + "?itemid=" + itemid;
            System.out.println("FrontEndResource.supplierservicecall urlString:" + urlString);
            URL url = new URL(urlString);
            return makeRequest(url);
        } catch (IOException e) {
            e.printStackTrace();
            return exceptionMessage(e);
        }
    }

    // todo convert to rest call/annotation
    private String makeRequest(URL url) throws IOException {
        URLConnection connection = url.openConnection();
        InputStream response = connection.getInputStream();
        try (Scanner scanner = new Scanner(response)) {
            return scanner.hasNext() ? scanner.useDelimiter("\\A").next() : "" + response;
        } catch (java.util.NoSuchElementException ex) {
            ex.printStackTrace();
            return ex.getMessage();
        }
    }

    private String exceptionMessage( Exception e) {
          return "{ message: " + "\"" + e + "\"}";
    }

}
