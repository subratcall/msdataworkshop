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

    static boolean isDirectSupplierQuickTest = Boolean.valueOf(System.getProperty("isDirectSupplierQuickTest", "false"));


 /* -------------------------------------------------------
     * JET UI Entry point 
     * -------------------------------------------------------*/

    @Path("/")
    @GET
    @Produces(MediaType.TEXT_HTML)
    public String jethome() {
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
      @Path("/placeorderJet")
      public String placeorderJet(Order newOrder) {
          try {
              URL url = new URL("http://order.msdataworkshop:8080/placeOrder?orderid=" + newOrder.orderId +
                      "&itemid=" + newOrder.orderItem + "&deliverylocation=" + URLEncoder.encode(newOrder.deliverTo, "UTF-8"));
              String json = makeRequest(url);
              System.out.println("FrontEndResource.placeorder json:" + json);
              System.out.println("FrontEndResource.placeorder complete, now show order...");
              url = new URL("http://order.msdataworkshop:8080/showorder?orderid=" + newOrder.orderId );
              json = makeRequest(url);
              return json;
          } catch (IOException e) {
              e.printStackTrace();
              return "\"error\":\"" + e.getMessage() +"\"";
          }
  
      }



    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/placeorder")
    public String placeorder(@QueryParam("orderid") String orderid,
                             @QueryParam("itemid") String itemid,
                             @QueryParam("deliverylocation") String deliverylocation) {
        System.out.println("-----> FrontEnd placeorder orderid:" + orderid + " itemid:" + itemid+ " deliverylocation:" + deliverylocation);
        try {
            URL url = new URL("http://localhost:8080/placeOrder?orderid=" + orderid +
                    "&itemid=" + itemid + "&deliverylocation=" + URLEncoder.encode(deliverylocation, "UTF-8"));
            String json = makeRequest(url);
            System.out.println("FrontEndResource.placeorder json:" + json);
            System.out.println("FrontEndResource.placeorder complete, now show order...");
            url = new URL("http://localhost:8080/showorder?orderid=" + orderid );
            json = makeRequest(url);
            return getFullPage(json);
        } catch (IOException e) {
            e.printStackTrace();
            return home();
        }
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/showorderservicecall")
    public String showorderservicecall(@QueryParam("orderid") String orderid, @QueryParam("test") String test) {
        System.out.println("-----> FrontEnd " + test + " orderid:" + orderid);
        try {
            URL url = new URL("http://localhost:8080/" + test + "?orderid=" + orderid );
            return getFullPage(makeRequest(url));
        } catch (IOException e) {
            e.printStackTrace();
            return home();
        }
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/orderservicecall")
    public String orderservicecall( @QueryParam("test") String test) {
        System.out.println("-----> FrontEnd orderservicecall test:" + test);
        try {
            URL url = new URL("http://localhost:8080/" +test);
            String requestString = makeRequest(url);
            System.out.println("-----> FrontEnd orderservicecall requestString:" + requestString);
            return getFullPage(requestString);
        } catch (IOException e) {
            e.printStackTrace();
            return home();
        }
    }


    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/streamingservicetest")
    public String sendTestStreamOrders(@QueryParam("numberofitemstostream") int numberofitemstostream) {
        try {
            URL url = new URL("http://localhost:8080/sendTestStreamOrders?numberofitemstostream=" + numberofitemstostream);
            return getFullPage(makeRequest(url));
        } catch (IOException e) {
            e.printStackTrace();
            return home();
        }
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/executeonorderpdb")
    public String orderadmin(@QueryParam("sql") String sql, @QueryParam("orderuser") String user, @QueryParam("orderpassword") String password) {
        try {
            System.out.println("-----> FrontEnd orderadmin sql = [" + sql + "], user = [" + user + "], password = [" + password + "]");
            sql = URLEncoder.encode(sql, "UTF-8");
            String urlString = "http://atpaqadmin.msdataworkshop:8080/execute?sql=" + sql + "&user=" + user + "&password=" + password;
            System.out.println("FrontEndResource.orderadmin urlString:" + urlString);
            URL url = new URL( urlString);
            return getFullPage(makeRequest(url));
        } catch (IOException e) {
            e.printStackTrace();
            return home();
        }
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/executeoninventorypdb")
    public String helidonatpinventory(@QueryParam("sql") String sql, @QueryParam("inventoryuser") String user, @QueryParam("inventorypassword") String password) {
        try {
            System.out.println("-----> FrontEnd helidonatpinventory: [" + sql + "], user = [" + user + "], password = [" + password + "]");
            sql = URLEncoder.encode(sql, "UTF-8");
            String urlString = "http://atpaqadmin.msdataworkshop:8080/execute?sql=" + sql + "&user=" + user + "&password=" + password;
            System.out.println("FrontEndResource.inventoryadmin urlString:" + urlString);
            URL url = new URL( urlString);
            return getFullPage(makeRequest(url));
        } catch (IOException e) {
            e.printStackTrace();
            return home();
        }
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/inventoryservicetest")
    public String inventoryservicetest(@QueryParam("test") String test) {
        try {
            URL url = new URL("http://inventory.msdataworkshop:8080/" + test);
            return getFullPage(makeRequest(url));
        } catch (IOException e) {
            e.printStackTrace();
            return home();
        }
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/inventoryservicetestwithitem")
    public String inventoryservicetestwithitem(@QueryParam("test") String test, @QueryParam("itemid") String itemid) {
        try {
            URL url = new URL("http://inventory.msdataworkshop:8080/" + test + "?itemid=" + itemid);
            return getFullPage(makeRequest(url));
        } catch (IOException e) {
            e.printStackTrace();
            return home();
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
            return getFullPage(makeRequest(url));
        } catch (IOException e) {
            e.printStackTrace();
            return home();
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
            return getFullPage(makeRequest(url));
        } catch (IOException e) {
            e.printStackTrace();
            return home();
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

}
