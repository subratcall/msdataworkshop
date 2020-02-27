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


@Path("/")
@ApplicationScoped
@Traced
public class FrontEndResource {

    @Path("/")
    @GET
    @Produces(MediaType.TEXT_HTML)
    public String home() {
        return getFullPage("");
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/placeorder")
    public String placeorder(@QueryParam("orderid") String orderid,
                             @QueryParam("itemid") String itemid) {
        System.out.println("-----> FrontEnd placeorder orderid:" + orderid + " itemid:" + itemid);
        try {
            URL url = new URL("http://order.datademo:8080/placeOrder?orderid=" + orderid +
                    "&itemid=" + itemid);
            String json = makeRequest(url);
            System.out.println("FrontEndResource.placeorder json:" + json);
            return getFullPage(json);
        } catch (IOException e) {
            e.printStackTrace();
            return home();
        }
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/showorder")
    public String showorder(@QueryParam("orderid") String orderid) {
        System.out.println("-----> FrontEnd showorder orderid:" + orderid);
        try {
            URL url = new URL("http://order.datademo:8080/showorder");
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
            URL url = new URL("http://order.datademo:8080/" +test);
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
            URL url = new URL("http://order.datademo:8080/sendTestStreamOrders?numberofitemstostream=" + numberofitemstostream);
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
            String urlString = "http://orderadmin.datademo:8080/execute?sql=" + sql + "&user=" + user + "&password=" + password;
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
            String urlString = "http://inventoryadmin.datademo:8080/execute?sql=" + sql + "&user=" + user + "&password=" + password;
            System.out.println("FrontEndResource.inventoryadmin urlString:" + urlString);
            URL url = new URL( urlString);
            return getFullPage(makeRequest(url));
        } catch (IOException e) {
            e.printStackTrace();
            return home();
        }
    }

    //Inventory service calls...

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/createInventoryQueue")
    public String createInventoryQueue() {
        try {
            URL url = new URL("http://inventory.datademo:8080/createQueue");
            return getFullPage(makeRequest(url));
        } catch (IOException e) {
            e.printStackTrace();
            return home();
        }
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/createInventoryTable")
    public String createInventoryTable() {
        try {
            URL url = new URL("http://inventory.datademo:8080/createInventoryTable");
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
            URL url = new URL("http://inventory.datademo:8080/" + test);
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
        try {
            URL url = new URL("http://supplier.datademo:8080/supplier/" + test + "?itemid=" + itemid);
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


    private String getFullPage(String output) {
        return getPageHeader() +
                "<table width=\"100%\" align=\"left\">" +
                "  <tr>" +
                "    <th width=\"50%\" align=\"left\" valign=\"top\">" +
               "<h4>JSON data, AQ transactional event-driven communication, and choreography saga (order and inventory service)</h4>" +
                "<form action=\"placeorder\">" +
                "itemid : <input type=\"text\" name=\"itemid\" size=\"5\" value=\"11\">  " +
                "orderid : <input type=\"text\" name=\"orderid\"  size=\"10\" value=\"66\"> " +
                "<input type=\"submit\" value=\"place order\"></p>" +
                "</form>" +
                "<h4>Relational data (supplier service)</h4>" +
                "<form action=\"supplierservicecall\">" +
                "itemid : <input type=\"text\" name=\"itemid\" size=\"5\" value=\"11\">  " +
                "<input type=\"submit\" name =\"test\" value=\"addInventory\">" +
                "<input type=\"submit\" name =\"test\" value=\"removeInventory\">" +
//                "<input type=\"submit\" name =\"test\" value=\"getinventory\">" +
                "</form>" +
               "<h4>Event sourcing and CQRS (order service)</h4>" +
                "<form action=\"showorder\">" +
                "orderid : <input type=\"text\" name=\"orderid\"  size=\10\" value=\"66\"> " +
                "    <input type=\"submit\" value=\"show order\">" +
                "</form>" +
                "<h4>OCI Streaming Service via Kafka API (orderstreaming service and order service)</h4>" +
                "<form action=\"orderservicecall\">" +
                "# of orders to stream : <input type=\"text\" name=\"numberoforderstostream\"  size=\5\" value=\"5\"> " +
                "<input type=\"submit\" name =\"test\" value=\"produceStreamOrders\">" +
                "<input type=\"submit\" name =\"test\" value=\"consumeStreamOrders\"></form>" +
                "<h4>Spatial data (map service)</h4>" +
                "<form action=\"deliveryservicetest\"><input type=\"submit\" name =\"test\" value=\"deliveryDetail\"></form>" +
                "<h4>Helidon Health Checks and OKE Health Probes (order service)</h4>" +
                "<form action=\"orderservicecall\"><input type=\"submit\" name =\"test\" value=\"health\">" +
                "   <input type=\"submit\" name =\"test\" value=\"health/live\">" +
                "   <input type=\"submit\" name =\"test\" value=\"health/ready\"></form>" +
                "<form action=\"orderservicecall\"><input type=\"submit\" name =\"test\" value=\"ordersetlivenesstofalse\">" +
                "   <input type=\"submit\" name =\"test\" value=\"ordersetdelayforreadiness\"></form>" +
                "<form action=\"orderservicecall\"><input type=\"submit\" name =\"test\" value=\"lastContainerStartTime\"></form>" +
                "<h4>Helidon Metrics and OKE horizontal-autoscaling (order service)</h4>" +
                "<form action=\"orderservicecall\"><input type=\"submit\" name =\"test\" value=\"metrics\"></form>" +
                "<form action=\"orderservicecall\"><input type=\"submit\" name =\"test\" value=\"startCPUStress\">" +
                "   <input type=\"submit\" name =\"test\" value=\"stopCPUStress\"></form>" +
                "<br> </th>" +
                "    <th width=\"50%\" align=\"left\" valign=\"top\">" +
                "<h4>Setup...</h4>" +
                "<form action=\"adminservicetest\"><input type=\"submit\" name =\"test\" value=\"createDBUsers\"></form>" +
                "<form action=\"orderservicecall\"><input type=\"submit\" name =\"test\" value=\"setupOrderServiceMessaging\"></form>" +
                "<form action=\"inventoryservicetest\"><input type=\"submit\" name =\"test\" value=\"setupInventoryServiceMessaging\"></form>" +
                "<form action=\"inventoryservicetest\"><input type=\"submit\" name =\"test\" value=\"listenForMessages\"></form>" +
                "<h4>Cleanup (drain queues and streams, delete tables and JSON/docs, etc.)...</h4>" +
                "<form action=\"executeonorderpdb\" id=\"executeonorderpdb\">" +
                "<textarea form=\"executeonorderpdb\" rows=\"4\" cols=\"50\" name =\"sql\"\">GRANT PDB_DBA TO orderuser identified by orderuserPW</textarea>" +
                "<br>    user : <input type=\"text\" name=\"orderuser\"  size=\"20\" value=\"\"> " +
                "    password : <input type=\"password\" name=\"orderpassword\"  size=\"20\" value=\"\"> " +
                "   <input type=\"submit\" value=\"executeonorderpdb\"></form>" +

                "<form action=\"executeoninventorypdb\" id=\"executeoninventorypdb\">" +
                "<textarea form=\"executeoninventorypdb\"  rows=\"4\" cols=\"50\" name =\"sql\"\">GRANT PDB_DBA TO inventoryuser identified by inventoryuserPW</textarea>" +
                "<br>    user : <input type=\"text\" name=\"inventoryuser\"  size=\"20\" value=\"\"> " +
                "    password : <input type=\"password\" name=\"inventorypassword\"  size=\"20\" value=\"\"> " +
                "   <input type=\"submit\" value=\"executeoninventorypdb\"></form>" +
                "<br>__________________________________________________________________" +
                "<br>Results......." +
                "<br>" + output + "</th>" +
                "  </tr>" +
                "</table>" +
                "</body></html>";
    }


    private String getPageHeader() {
        return "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">" +
                "    <title>Home</title>" +
                "  <head><style>\n" +
                "body {background-color: powderblue;}" +
                "h4   {color: blue;}" +
                "</style></head>" +
                "  <body><h1 color=\"#BDB76B;\" align=\"left\">" +
                "<a href=\"\" />Intelligent Event-driven Stateful Microservices with Helidon and Autonomous Database on OCI</a></h1>" +
                "</table>";
    }

}
