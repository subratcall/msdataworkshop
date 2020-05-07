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

    static boolean isDirectSupplierQuickTest = Boolean.valueOf(System.getProperty("isDirectSupplierQuickTest", "false"));

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
                             @QueryParam("itemid") String itemid,
                             @QueryParam("deliverylocation") String deliverylocation) {
        System.out.println("-----> FrontEnd placeorder orderid:" + orderid + " itemid:" + itemid+ " deliverylocation:" + deliverylocation);
        try {
            URL url = new URL("http://order.msdataworkshop:8080/placeOrder?orderid=" + orderid +
                    "&itemid=" + itemid + "&deliverylocation=" + URLEncoder.encode(deliverylocation, "UTF-8"));
            String json = makeRequest(url);
            System.out.println("FrontEndResource.placeorder json:" + json);
            System.out.println("FrontEndResource.placeorder complete, now show order...");
            url = new URL("http://order.msdataworkshop:8080/showorder?orderid=" + orderid );
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
            URL url = new URL("http://order.msdataworkshop:8080/" + test + "?orderid=" + orderid );
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
            URL url = new URL("http://order.msdataworkshop:8080/" +test);
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
            URL url = new URL("http://order.msdataworkshop:8080/sendTestStreamOrders?numberofitemstostream=" + numberofitemstostream);
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

    private String getFullPage(String output) {
        return getPageHeader() +
                "<table width=\"100%\" align=\"left\">" +
                "  <tr>" +
                "    <th width=\"50%\" align=\"left\" valign=\"top\">" +
                "<h3>Task 7...</h3>" +
                "<form action=\"adminservicetest\">" +
                "   <input type=\"submit\" name =\"test\" value=\"testdatasources\"><br>" +
                "<h3>Task 8...</h3>" +
                "   <input type=\"submit\" name =\"test\" value=\"createUsers\">" +
                "   <input type=\"submit\" name =\"test\" value=\"createInventoryTable\">" +
                "   <input type=\"submit\" name =\"test\" value=\"createDBLinks\">" +
                "   <input type=\"submit\" name =\"test\" value=\"setupTablesQueuesAndPropagation\">" +
                "</form>" +
                /**         "<h4>Cleanup (drain queues and streams, delete tables and JSON/docs, etc.)...</h4>" +
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
                 */
                "<h3>Task 9...</h3>" +
               "<h4>JSON data, AQ transactional event-driven communication, and choreography saga (order and inventory service)</h4>" +
                "<form action=\"inventoryservicetest\"><input type=\"submit\" name =\"test\" value=\"listenForMessages\"></form>" +
                "<form action=\"placeorder\">" +
                "veggie : <input type=\"text\" name=\"itemid\" size=\"12\" value=\"cucumbers\"> <br> " +
                "deliver to : <input type=\"text\" name=\"deliverylocation\" size=\"35\" value=\"780 PANORAMA DR,San Francisco,CA\"><br>  " +
                "orderid : <input type=\"text\" name=\"orderid\"  size=\"6\" value=\"66\"> " +
                "<br><input type=\"submit\" value=\"place order\">" +
                "</form>" +
                "<h4>Relational data (supplier service)</h4>" +
                "<form action=\"" + (isDirectSupplierQuickTest?"inventoryservicetestwithitem":"supplierservicecall") + "\">" +
                "veggie (cucumbers, carrots, tomatoes, onions): <br><input type=\"text\" name=\"itemid\" size=\"12\" value=\"cucumbers\">  <br>" +
                "<input type=\"submit\" name =\"test\" value=\"addInventory\">" +
                "<input type=\"submit\" name =\"test\" value=\"removeInventory\">" +
                "<input type=\"submit\" name =\"test\" value=\"getInventory\">" +
                "</form>" +
               "<h4>Event sourcing and CQRS (order service)</h4>" +
                "<form action=\"showorderservicecall\">" +
                "orderid : <input type=\"text\" name=\"orderid\"  size=\"6\" value=\"66\"> " +
                " <br>   <input type=\"submit\" name =\"test\" value=\"showorder\">" +
                "   <input type=\"submit\" name =\"test\" value=\"showallorders\">" +
                "   <input type=\"submit\" name =\"test\" value=\"deleteorder\">" +
                "   <input type=\"submit\" name =\"test\" value=\"deleteallorders\">" +
                "</form>" +
              "<h4>Spatial data (map service running on WebLogic)</h4>" +
//                " <label>From:</label>" +
                               "        <input type=\"hidden\" size=\"35\" id=\"start_address\" name=\"start_address\" value=\"1469 WEBSTER ST,San Francisco,CA\">" +
//                "        <label>To:</label>" +
                "        <input type=\"hidden\" size=\"35\" id=\"end_address\" name=\"end_address\" value=\"780 PANORAMA DR,San Francisco,CA\">" +
//                "        <button type=\"button\" onclick=\"find_path_address_render()\">deliveryDetail</button>" +
                "<form action=\"https://150.136.177.253:7002/chrest/Visualize.jsp\" method=\"POST\" id=\"renderForm\">" +
                "<input type=\"hidden\" id = \"geojson\" name=\"geojson\" value=\"" +
//              "{&quot;type&quot;:&quot;LineString&quot;,&quot;coordinates&quot;:[[-74.00501,40.70583],[-74.00457,40.70549],[-74.00447,40.70541],[-74.00418,40.70559],[-74.00386,40.70579],[-74.00361,40.70595],[-74.00346,40.70605]," +
//                "[-74.00335,40.70611],[-74.00318,40.70621],[-74.00231,40.7067],[-74.00274,40.70722],[-74.00311,40.70767],[-74.00336,40.708],[-74.00345,40.70808],[-74.00407,40.70745],[-74.00412,40.70757],[-74.00433,40.70783]," +
//                "[-74.00477,40.70841],[-74.00505,40.70876],[-74.00513,40.70885],[-74.00524,40.70893],[-74.00532,40.70899],[-74.00547,40.70909],[-74.00643,40.70956],[-74.00705,40.70987],[-74.00774,40.71022]," +
//                "[-74.00906,40.71089],[-74.01046,40.71153],[-74.01013,40.71209],[-74.00967,40.71274],[-74.00927,40.71326],[-74.00902,40.71359],[-74.00885,40.71381],[-74.0084,40.71437],[-74.00795,40.71494]," +
//                "[-74.00755,40.71544],[-74.00882,40.71602],[-74.0092,40.71619],[-74.00911,40.71692],[-74.00906,40.71726],[-74.009,40.7176],[-74.00894,40.71793],[-74.00888,40.71827],[-74.00882,40.71864],[-74.00875,40.71903]," +
//                "[-74.0087,40.7193],[-74.00858,40.71996],[-74.00847,40.72065],[-74.00842,40.72089],[-74.00837,40.7212],[-74.00834,40.72133],[-74.00823,40.72198],[-74.00812,40.72264],[-74.00801,40.72328],[-74.00795,40.72365]," +
//                "[-74.00793,40.72376],[-74.00786,40.72382],[-74.00777,40.72388],[-74.00773,40.72392],[-74.00771,40.72393],[-74.00745,40.72412],[-74.00736,40.72417],[-74.00728,40.72424],[-74.00723,40.72429],[-74.0071,40.72441]," +
//                "[-74.00703,40.7245]]}" +
//                "\"/>"  +
             "{&quot;type&quot;:&quot;LineString&quot;,&quot;coordinates&quot;:[[-122.43118136363267,37.784149090890885],[-122.43105,37.7835],[-122.43104,37.78344],[-122.43087,37.78257],[-122.43067,37.78162],[-122.43049,37.78069],[-122.4303,37.77976]," +
                "[-122.43026,37.77956],[-122.43011,37.77884],[-122.42991,37.77791],[-122.4298,37.77728],[-122.42975,37.77697],[-122.42969,37.77666],[-122.42961,37.77652],[-122.42954,37.77604],[-122.42944,37.77558],[-122.42933,37.77512],[-122.42925,37.77465]," +
                "[-122.42915,37.77425],[-122.42913,37.77417],[-122.42897,37.77325],[-122.42888,37.77281],[-122.4288,37.77244],[-122.42878,37.77233],[-122.42869,37.77186],[-122.42861,37.77139],[-122.42852,37.77094],[-122.42842,37.77046],[-122.42922,37.77035]," +
                "[-122.42913,37.76958],[-122.42912,37.76946],[-122.42909,37.7691],[-122.42906,37.76881],[-122.42904,37.76867],[-122.429,37.76828],[-122.42895,37.76771],[-122.42894,37.76761],[-122.42944,37.7672],[-122.42971,37.76698],[-122.4301,37.76668]," +
                "[-122.43028,37.76655],[-122.43035,37.76649],[-122.43054,37.76634],[-122.43101,37.76598],[-122.43122,37.76579],[-122.43156,37.76552],[-122.43169,37.76542],[-122.43175,37.76537],[-122.43203,37.76514],[-122.43229,37.76493],[-122.43259,37.76469]," +
                "[-122.43314,37.76424],[-122.43351,37.76398],[-122.43358,37.76392],[-122.43383,37.76374],[-122.43403,37.76358],[-122.43449,37.7632],[-122.4347,37.76303],[-122.43486,37.76291],[-122.43503,37.76279],[-122.43516,37.76264],[-122.43545,37.76262]," +
                "[-122.43554,37.7626],[-122.43589,37.76246],[-122.43596,37.76244],[-122.43605,37.76243],[-122.43735,37.76236],[-122.43797,37.76232],[-122.43842,37.76229],[-122.43949,37.76222],[-122.44017,37.76217],[-122.44175,37.76209],[-122.44232,37.76206]," +
                "[-122.44287,37.76203],[-122.44334,37.762],[-122.44356,37.76198],[-122.44388,37.76196],[-122.4443,37.76193],[-122.4445,37.76192],[-122.44521,37.76188],[-122.4453,37.76187],[-122.44573,37.76185],[-122.44594,37.76183],[-122]]}" +
                "\"/>"  +

                "<input type=\"submit\" value=\"deliveryDetail\"></form>" +
//                "<h3>Task 10...</h3>" +
//                "<h4>OCI Streaming Service via Kafka API (orderstreaming service and order service)</h4>" +
//                "<form action=\"orderservicecall\">" +
//                "# of orders to stream : <input type=\"text\" name=\"numberoforderstostream\"  size=\"5\" value=\"5\"> " +
//                "<input type=\"submit\" name =\"test\" value=\"produceStreamOrders\">" +
                "</th>" +
                "    <th width=\"50%\" align=\"left\" valign=\"top\">" +
                "<h3>Task 10...</h3>" +
                "<h4>Helidon Metrics</h4>" +
                "<form action=\"orderservicecall\"><input type=\"submit\" name =\"test\" value=\"metrics\"></form>" +
                "<h3>Task 11...</h3>" +
                "<h4>Helidon Health Checks and OKE Health Probes (order service)</h4>" +
                "<form action=\"orderservicecall\"><input type=\"submit\" name =\"test\" value=\"health\">" +
                "   <input type=\"submit\" name =\"test\" value=\"health/live\">" +
                "   <input type=\"submit\" name =\"test\" value=\"health/ready\"></form>" +
                "<form action=\"orderservicecall\"><input type=\"submit\" name =\"test\" value=\"ordersetlivenesstofalse\">" +
                "   <input type=\"submit\" name =\"test\" value=\"ordersetdelayforreadiness\"></form>" +
                "<form action=\"orderservicecall\"><input type=\"submit\" name =\"test\" value=\"lastContainerStartTime\"></form>" +
                "<h3>Task 12...</h3>" +
                "<h4>Helidon OpenTracing</h4>" +
                "<a href=\"https://raw.githubusercontent.com/paulparkinson/msdataworkshop/master/images/jaegercollapsed.png\">Jaeger (collapsed view)</a>" +
                "<br><a href=\"https://raw.githubusercontent.com/paulparkinson/msdataworkshop/master/images/jaegerexpanded.png\">Jaeger (expanded view)</a>" +
                "<h3>Task 13...</h3>" +
                "<h4>OKE horizontal-autoscaling</h4>" +
                "<form action=\"orderservicecall\"><input type=\"submit\" name =\"test\" value=\"startCPUStress\">" +
                "   <input type=\"submit\" name =\"test\" value=\"stopCPUStress\"></form> " +
                "   <h3>Cleanup etc....</h3> " +
                "<form action=\"adminservicetest\">" +
                "   <input type=\"submit\" name =\"test\" value=\"unschedulePropagation\">" +
                "   <input type=\"submit\" name =\"test\" value=\"deleteUsers\">" +
                "   <input type=\"submit\" name =\"test\" value=\"enablePropagation\">" +
                "</form>" +
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
                "  <head>" +
                "        <script type=\"text/javascript\" src=\"https://code.jquery.com/jquery-3.3.1.min.js\"></script>" +
                "<style>" +
                "body {background-color: powderblue;}" +
                "h4   {color: gray;}" +
                "</style></head>" +
                "  <body>" +
                "<script>" +
                "" +
                "            const elocationURL = 'https://elocation.oracle.com/routeserver/servlet/RouteServerServlet'" +
                "" +
                "            function createLonLatReqeuest() {" +
                "                var start = document.getElementById(\"start_lonlat\").value;" +
                "                var end = document.getElementById(\"end_lonlat\").value;" +
                "                var start_lonlat = start.split(\",\")" +
                "                var end_lonlat = end.split(\",\")" +
                "" +
                "                var postData = 'xml_request=<route_request route_preference=\"shortest\" road_preference=\"highway\" return_driving_directions=\"true\" return_locations=\"true\" distance_unit=\"mile\" time_unit=\"minute\" return_route_geometry=\"true\" return_subroute_geometry=\"false\" return_segment_geometry=\"false\"><start_location><input_location id=\"1\" longitude=\"' + start_lonlat[0] + '\" latitude=\"' + start_lonlat[1] + '\" /></start_location><end_location><input_location id=\"2\" longitude=\"' + end_lonlat[0] + '\" latitude=\"' + end_lonlat[1] + '\" /></end_location></route_request>';" +
                "" +
                "                return postData" +
                "            }" +
                "" +
                "            function createAddressReqeuest() {" +
                "                var start = document.getElementById(\"start_address\").value;" +
                "                var end = document.getElementById(\"end_address\").value;" +
                "                var start_address = start.split(\",\")" +
                "                var end_address = end.split(\",\")" +
                "" +
                "                var postData = 'xml_request=<route_request route_preference=\"shortest\" road_preference=\"highway\" return_driving_directions=\"true\" language=\"French\" distance_unit=\"km\" time_unit=\"minute\" return_route_geometry=\"true\"><start_location> <input_location id=\"1\"> <input_address> <us_form1 street=\"' + start_address[0] + '\" lastline=\"' + start_address.slice(1,3) + '\" /> </input_address> </input_location></start_location><end_location> <input_location id=\"2\"> <input_address> <us_form1 street=\"' + end_address[0] + '\" lastline=\"' + end_address.slice(1,3) + '\" /> </input_address> </input_location></end_location></route_request>';" +
                "" +
                "                return postData" +
                "            }" +
                "" +
                "            function find_path_address() {" +
                "                var postData = createAddressReqeuest()" +
                "                $.ajax({" +
                "                    url: elocationURL," +
                "                    type: 'POST'," +
                "                    data: postData," +
                "                    dataType: \"xml\"," +
                "                    success: function (data) {" +
                "                        var res = (new XMLSerializer()).serializeToString(data)" +
                "                        alert(res)" +
                "                    }" +
                "                });" +
                "            }" +
                "" +
                "            function find_path_address_render() {" +
                "                var postData = createAddressReqeuest()" +
                "                $.ajax({" +
                "                    url: elocationURL," +
                "                    type: 'POST'," +
                "                    data: postData," +
                "                    success: function (data) {" +
                "                        var res = (new XMLSerializer()).serializeToString(data)" +
                "                        render(res)" +
                "                    }" +
                "                });" +
                "            }" +
                "" +
                "            function find_path_lonlat() {" +
                "                var postData = createLonLatReqeuest()" +
                "                $.ajax({" +
                "                    url: elocationURL," +
                "                    type: 'POST'," +
                "                    data: postData," +
                "                    dataType: \"xml\"," +
                "                    success: function (data) {" +
                "                        var res = (new XMLSerializer()).serializeToString(data)" +
                "                        alert(res)" +
                "                    }" +
                "                });" +
                "            }" +
                "" +
                "            function find_path_lonlat_render() {" +
                "                var postData = createLonLatReqeuest()" +
                "                $.ajax({" +
                "                    url: elocationURL," +
                "                    type: 'POST'," +
                "                    data: postData," +
                "                    dataType: \"xml\"," +
                "                    success: function (data) {" +
                "                        var res = (new XMLSerializer()).serializeToString(data)" +
                "                        render(res)" +
                "                    }" +
                "                });" +
                "            }" +
                "" +
                "            function createRenderRequest(coordinates) {" +
                "                coors = coordinates.trim().split(' ')" +
                "                // console.log(coors)" +
                "                cStr = '['" +
                "                for (i = 0; i < coors.length; i++) {" +
                "                    cStr += '[' + coors[i] + ']'" +
                "                    if (i < coors.length - 1) {" +
                "                        cStr += ','" +
                "                    }" +
                "                }" +
                "                cStr += ']'" +
                "                var postData = '{\"type\":\"LineString\",\"coordinates\":' + cStr + '}'" +
                "                // console.log(cStr)" +
                "                return postData;" +
                "            }" +
                "" +
                "            function render(xmlStr) {" +
                "                var oParser = new DOMParser();" +
                "                var xmlDoc = oParser.parseFromString(xmlStr, \"application/xml\");" +
                "                coordinates = xmlDoc.getElementsByTagName(\"coordinates\")[0].childNodes[0].nodeValue;   " +
                "" +
                "                var postData = createRenderRequest(coordinates);" +
                "                document.getElementById(\"geojson\").value = postData;" +
                "                var renderForm = document.getElementById(\"renderForm\");" +
                "                renderForm.submit();" +
                "            }" +
                "" +
                "        </script>        " +
                
                "<h1 color=\"#BDB76B;\" align=\"left\">" +
                "Event-driven Stateful Microservices with Helidon and Autonomous Database on OCI</h1>" +
                "<h2>\"VeggieDash\"</h2>" +
                "</table>";
    }

}
