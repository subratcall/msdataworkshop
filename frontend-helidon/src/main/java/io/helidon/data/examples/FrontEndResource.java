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

    private String JAEGER_QUERY_ADDRESS = System.getenv("JAEGER_QUERY_ADDRESS");

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

    @Path("/sushi")
    @GET
    @Produces("image/svg+xml")
    public Response sushi() {
        try {
            return Response.ok(Resource.create("web/images/sushi.svg").stream()).build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @Path("/pizza")
    @GET
    @Produces("image/svg+xml")
    public Response pizza() {
        try {
            return Response.ok(Resource.create("web/images/pizza.svg").stream()).build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @Path("/burger")
    @GET
    @Produces("image/svg+xml")
    public Response burger() {
        try {
            return Response.ok(Resource.create("web/images/burger.svg").stream()).build();
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
              if (json.indexOf("fail") > -1) { // we return 200 regardless and check for "fail"
                  if (json.indexOf("SQLIntegrityConstraintViolationException" ) > -1)
                      return asJSONMessage("SQLIntegrityConstraintViolationException. Delete All Orders or use a different order id to avoid dupes.");
                  else return asJSONMessage( json);
              }
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
    @Traced
    @Path("/command")
    public String command(Command command) {
        boolean isOrderBasedCommand = command.serviceName.equals("order") && command.orderId != -1;
        boolean isSupplierCommand = command.serviceName.equals("supplier");
        boolean isHealthCommand = command.commandName.indexOf("health") > -1;
        String urlString = "http://" + command.serviceName + ".msdataworkshop:8080/" + command.commandName +
                (isOrderBasedCommand ? "?orderid=" + command.orderId : "") +
                (isSupplierCommand ? "?itemid="+ command.orderItem : "");
        System.out.println("FrontEndResource.command url:" + urlString );
        try {
            String response = makeRequest(new URL(urlString));
            String returnString =  isOrderBasedCommand || isHealthCommand ? response: asJSONMessage(response);
            System.out.println("FrontEndResource.command url:" + urlString + "  returnString:" + returnString);
            return returnString;
        } catch (Exception e) {
            e.printStackTrace();
            return asJSONMessage(e);
        }
    }


    private String asJSONMessage(Object e) {
        FrontEndResponse frontEndResponse = new FrontEndResponse();
        frontEndResponse.message = e.toString();
        return JsonUtils.writeValueAsString(frontEndResponse);
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
