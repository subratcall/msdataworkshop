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
package oracle.db.microservices;


import java.sql.*;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


@Path("/")
@ApplicationScoped
public class ATPAQAdminResource {
  PropagationSetup propagationSetup = new PropagationSetup();

  @Inject
  @Named("orderpdb")
  private DataSource orderpdbDataSource; // .setFastConnectionFailoverEnabled(false) to get rid of benign SEVERE message

  @Inject
  @Named("inventorypdb")
  private DataSource inventorypdbDataSource;

  @Path("/test")
  @GET
  @Produces(MediaType.TEXT_HTML)
  public String test() {
      System.out.println("test datasources...");
      try {
          System.out.println("ATPAQAdminResource.test inventorypdbDataSource connection:" + inventorypdbDataSource.getConnection());
      } catch (Exception e) {
          e.printStackTrace();
      }
      try {
          System.out.println("ATPAQAdminResource.test orderpdbDataSource connection:" + orderpdbDataSource.getConnection());
      } catch (Exception e) {
          e.printStackTrace();
      }
      return "success";
  }

    @Path("/setupAll")
  @GET
  @Produces(MediaType.TEXT_HTML)
  public String setupAll() {
    String returnValue = "";
    try {
      System.out.println("setupAll ...");
      returnValue += propagationSetup.createUsers(orderpdbDataSource, inventorypdbDataSource);
      returnValue += propagationSetup.createDBLinks(orderpdbDataSource, inventorypdbDataSource);
      returnValue += propagationSetup.setup(orderpdbDataSource, inventorypdbDataSource);
      return " result of setupAll : success... " + returnValue;
    } catch (Exception e) {
      e.printStackTrace();
      returnValue += e;
      return " result of setupAll : " + returnValue;
    }
  }

  @Path("/createUsers")
  @GET
  @Produces(MediaType.TEXT_HTML)
  public String createUsers() {
    String returnValue = "";
    try {
      System.out.println("createUsers ...");
      returnValue += propagationSetup.createUsers(orderpdbDataSource, inventorypdbDataSource);
      return " result of createUsers : success... " + returnValue;
    } catch (Exception e) {
      e.printStackTrace();
      returnValue += e;
      return " result of createUsers : " + returnValue;
    }
  }

  @Path("/createDBLinks")
  @GET
  @Produces(MediaType.TEXT_HTML)
  public String createDBLinks() {
    String returnValue = "";
    try {
      System.out.println("createDBLinks ...");
      returnValue += propagationSetup.createDBLinks(orderpdbDataSource, inventorypdbDataSource);
      return " result of createDBLinks : success... " + returnValue;
    } catch (Exception e) {
      e.printStackTrace();
      returnValue += e;
      return " result of createDBLinks : " + returnValue;
    }
  }

  @Path("/setupTablesQueuesAndPropagation")
  @GET
  @Produces(MediaType.TEXT_HTML)
  public String setupTablesQueuesAndPropagation() {
    String returnValue = "";
    try {
      System.out.println("setupTablesQueuesAndPropagation ...");
      returnValue += propagationSetup.setup(orderpdbDataSource, inventorypdbDataSource);
      return " result of setupTablesQueuesAndPropagation : success... " + returnValue;
    } catch (Exception e) {
      e.printStackTrace();
      returnValue += e;
      return " result of setupTablesQueuesAndPropagation : " + returnValue;
    }
  }

  @Path("/execute")
  @GET
  @Produces(MediaType.TEXT_HTML)
  public String execute(@QueryParam("pdb") String pdb, @QueryParam("sql") String sql, @QueryParam("user") String user, @QueryParam("password") String password) {
    try {
      System.out.println("pdb:"+pdb+" execute sql = [" + sql + "], user = [" + user + "]");
      boolean isUserPWPresent = user != null && password != null && !user.equals("") && !password.equals("");
      System.out.println("pdb:"+pdb+" execute sql = [" + sql + "], user = [" + user + "] isUserPWPresent:" + isUserPWPresent);
      DataSource dataSource = "order".endsWith(pdb) ?orderpdbDataSource:inventorypdbDataSource;
      Connection connection = isUserPWPresent ?  dataSource.getConnection(user, password): dataSource.getConnection();
      System.out.println("connection:" + connection);
      connection.createStatement().execute(sql);
      return " result of sql = [" + sql + "], user = [" + user + "]" + " : " + "success";
    } catch (Exception e) {
      e.printStackTrace();
      return " result of sql = [" + sql + "], user = [" + user + "]" + " : " + e;
    }
  }


  @Path("/unschedulePropagation")
  @GET
  @Produces(MediaType.TEXT_PLAIN)
  public Response unschedulePropagation() throws SQLException {
    final Response returnValue = Response.ok()
            .entity("Connection obtained successfully metadata:" + propagationSetup.unscheduleOrderToInventoryPropagation(orderpdbDataSource))
            .build();
    return returnValue;
  }


  @Path("/getConnectionMetaData")
  @GET
  @Produces(MediaType.TEXT_PLAIN)
  public Response getConnectionMetaData() throws SQLException {
    final Response returnValue = Response.ok()
            .entity("Connection obtained successfully metadata:" + orderpdbDataSource.getConnection().getMetaData())
            .build();
    return returnValue;
  }


}
