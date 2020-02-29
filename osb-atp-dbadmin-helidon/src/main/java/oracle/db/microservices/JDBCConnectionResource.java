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

import java.sql.Connection;
import java.sql.SQLException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/")
@ApplicationScoped
public class JDBCConnectionResource {

  static {
    System.out.println("JDBCConnectionResource.static oracle.ucp.jdbc.PoolDataSource.atp1.passwordfromsecret:" +
            System.getenv("oracle.ucp.jdbc.PoolDataSource.atp1.passwordfromsecret"));
  }

  @Inject
  @Named("atp1")
  private DataSource dataSource; // .setFastConnectionFailoverEnabled(false) to get rid of benign SEVERE message

  @Path("/")
  @GET
  @Produces(MediaType.TEXT_HTML)
  public String home() {
    return getHTMLString("", "");
  }

  @Path("/execute")
  @GET
  @Produces(MediaType.TEXT_HTML)
  public String execute(@QueryParam("sql") String sql, @QueryParam("user") String user, @QueryParam("password") String password) {
    try {
      System.out.println("execute sql = [" + sql + "], user = [" + user + "]");
      boolean isUserPWPresent = user != null && password != null && !user.equals("") && !password.equals("");
      System.out.println("execute sql = [" + sql + "], user = [" + user + "] isUserPWPresent:" + isUserPWPresent);
      Connection connection = isUserPWPresent ?  dataSource.getConnection(user, password):dataSource.getConnection();
      System.out.println("connection:" + connection);
      connection.createStatement().execute(sql);
      return " result of sql = [" + sql + "], user = [" + user + "]" + " : " + "success";
    } catch (Exception e) {
      e.printStackTrace();
      return " result of sql:" + sql + " : " + e;
    }
  }

  @Path("/executeSQLFromForm")
  @GET
  @Produces(MediaType.TEXT_HTML)
  public String executeSQLFromForm(@QueryParam("sql") String sql, @QueryParam("user") String user, @QueryParam("password") String password) {
    try {
      System.out.println("execute sql = [" + sql + "], user = [" + user + "], password = [" + password + "]");
      Connection connection = (user != null && password != null && !user.equals("") && !password.equals(""))?
              dataSource.getConnection(user, password):dataSource.getConnection();
      System.out.println("connection:" + connection);
      connection.createStatement().execute(sql);
      return getHTMLString(sql, "success");
    } catch (Exception e) {
      e.printStackTrace();
      return getHTMLString(sql, e.toString());
    }
  }

  public String getHTMLString(@QueryParam("sql") String sql, String sqlExecuteReturnString) {
    return "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">" +
            "    <title>Home</title>" +
            "    <link rel=\"stylesheet\" href=\"./oracledbdemo_files/style.css\">" +
            "  </head>" +
            "  <body>" +
            "    <h1>Helidon ATP Query</h1>" +
            "<h2>Execute SQL...</h2>" +
            "<form action=\"executeSQLFromForm\" method=\"get\">" +
            "    user <input type=\"text\" name=\"user\" size=\"20\" value=\"\"><br>" +
            "    password <input type=\"text\" name=\"password\" size=\"20\" value=\"\"><br>" +
            "    SQL <input type=\"text\" name=\"sql\" size=\"300\" value=\"select count(*) from dual\"><br>" +
            "    <input type=\"submit\" value=\"execute\">" +
            "</form>" +
            "<br>" +
            " result of sql:" + sql + " : " + sqlExecuteReturnString +
            "<br>" +
            "<h2>Create AQ User...</h2>" +
            "<form action=\"createAQUser\" method=\"get\">" +
            "    queueOwner <input type=\"text\" name=\"queueOwner\" size=\"20\" value=\"\"><br>" +
            "    queueOwnerPW <input type=\"text\" name=\"queueOwnerPW\" size=\"20\" value=\"\"><br>" +
            "    <input type=\"submit\" value=\"createAQUser\">" +
            "</form>" +
            createDBLinkHTML() +
            "<p><a href=\"getConnectionMetaData\">getConnectionMetaData</a></p>" +
            "<p><a href=\"createAQUser\">createAQUser</a></p>" +
            "</body></html>";
  }

  private String dropUser() {
    return "<h2>Drop User...</h2>" +
            "<form action=\"execute\" method=\"get\">" +
            "    user <input type=\"text\" name=\"user\" size=\"20\" value=\"\"><br>" +
            "    password <input type=\"text\" name=\"password\" size=\"20\" value=\"\"><br>" +
            "<textarea id=\"w3mission\" rows=\"50\" cols=\"20\">" +
            "execute dbms_aqadm.unschedule_propagation(queue_name => 'paul.orders', " +
            "destination => 'inventorydb_link', destination_queue => 'paul.orders_propqueue');" +
            " execute dbms_lock.sleep(10);" +
            " DROP USER paul CASCADE\n" +
            " /\n" +
            "\n" +
            " GRANT DBA TO paul IDENTIFIED BY paul\n" +
            " /" +
            "</textarea>" +
            "</form>";
  }

  private String createDBLinkHTML() {
    return "<h2>Create DB Link...</h2>" +
            "<form action=\"execute\" method=\"get\">" +
            "    user <input type=\"text\" name=\"user\" size=\"20\" value=\"\"><br>" +
            "    password <input type=\"text\" name=\"password\" size=\"20\" value=\"\"><br>" +
            "<textarea id=\"w3mission\" rows=\"50\" cols=\"20\">\n" +
            "create database link inventorylink" +
            "  connect to inventorydb_high identified by paul " +
            "  using " +
            "  '(DESCRIPTION=" +
            "    (ADDRESS=" +
            "     (PROTOCOL=TCP)" +
            "     (HOST=10.2.10.18)" +
            "     (PORT=1525))" +
            "    (CONNECT_DATA=" +
            "     (SID=test10)))'" +
            "</textarea>" +
            "</form>";
  }

  @Path("/getConnectionMetaData")
  @GET
  @Produces(MediaType.TEXT_PLAIN)
  public Response getConnectionMetaData() throws SQLException {
    final Response returnValue = Response.ok()
            .entity("Connection obtained successfully metadata:" + dataSource.getConnection().getMetaData())
            .build();
    return returnValue;
  }

  @Path("/createAQUser")
  @GET
  @Produces(MediaType.TEXT_PLAIN)
  public Response createAQUser(@QueryParam("queueOwner") String queueOwner,
                               @QueryParam("queueOwnerPW") String queueOwnerPW) throws SQLException {
    System.out.println("createAQUser queueOwner = [" + queueOwner + "], queueOwnerPW = [" + queueOwnerPW + "]");
    Connection sysDBAConnection = dataSource.getConnection();
    sysDBAConnection.createStatement().execute(
            "grant pdb_dba to " + queueOwner + " identified by " + queueOwnerPW);
    sysDBAConnection.createStatement().execute("grant unlimited tablespace to " + queueOwner);
    sysDBAConnection.createStatement().execute("grant connect, resource TO " + queueOwner);
    sysDBAConnection.createStatement().execute("grant aq_user_role TO " + queueOwner);
    sysDBAConnection.createStatement().execute("GRANT EXECUTE ON sys.dbms_aqadm TO " + queueOwner);
    sysDBAConnection.createStatement().execute("GRANT EXECUTE ON sys.dbms_aq TO " + queueOwner);
    sysDBAConnection.createStatement().execute("GRANT EXECUTE ON sys.dbms_aq TO " + queueOwner);
    final Response returnValue = Response.ok()
            .entity("createAQUser successful")
            .build();
    return returnValue;
  }

}

/**

 set echo on

 connect system/manager

 -- execute the following statements to make DROP USER paul CASCADE more likely to succeed
 execute dbms_aqadm.unschedule_propagation(queue_name => 'paul.my_source_queue', destination => 'my_link', destination_queue => 'paul.my_dest_queue');
 execute dbms_lock.sleep(10);

 DROP USER paul CASCADE
 /

 GRANT DBA TO paul IDENTIFIED BY paul
 /

 connect paul/paul

 CREATE DATABASE LINK my_link CONNECT TO paul IDENTIFIED BY paul USING 'inst1'
 /

 -- verify database link works
 SELECT * FROM DUAL@my_link
 /

 CREATE OR REPLACE TYPE payload AS OBJECT(jdata CLOB);
 /
 show errors;

 -- create and start destination queue
 BEGIN
 dbms_aqadm.create_queue_table(
 queue_table        => 'paul.my_dest_queue_table',
 queue_payload_type => 'payload',
 multiple_consumers => TRUE);
 dbms_aqadm.create_queue(queue_name  => 'paul.my_dest_queue',
 queue_table => 'paul.my_dest_queue_table');
 dbms_aqadm.start_queue(queue_name => 'paul.my_dest_queue');
 END;
 /

 -- create and start source queue
 -- Note: propagation requires that the source queue be a multi-consumer queue, so multiple_consumers is set to TRUE
 BEGIN
 dbms_aqadm.create_queue_table(
 queue_table        => 'paul.my_source_queue_table',
 queue_payload_type => 'payload',
 multiple_consumers => true);
 dbms_aqadm.create_queue(queue_name  => 'paul.my_source_queue',
 queue_table => 'paul.my_source_queue_table');
 dbms_aqadm.start_queue(queue_name => 'paul.my_source_queue');
 END;
 /

 -- add subscriber on destination queue
 DECLARE
 sub sys.aq$_agent := sys.aq$_agent(name     => 'dest_subscriber',
 address  => NULL,
 protocol => NULL);
 BEGIN
 dbms_aqadm.add_subscriber(queue_name     => 'paul.my_dest_queue',
 subscriber     => sub);
 END;
 /


 -- add propagation subscriber on source queue
 DECLARE
 sub sys.aq$_agent := sys.aq$_agent(name     => 'dest_subscriber',
 address  => 'paul.my_dest_queue@my_link',
 protocol => NULL);
 BEGIN
 dbms_aqadm.add_subscriber(queue_name     => 'paul.my_source_queue',
 subscriber     => sub,
 queue_to_queue => TRUE);
 END;
 /


 -- schedule propagation on database link
 BEGIN
 dbms_aqadm.schedule_propagation(queue_name  => 'paul.my_source_queue',
 destination => 'my_link',
 latency     => 10,
 destination_queue => 'paul.my_dest_queue');
 COMMIT;
 END;
 /

 set serveroutput on

 -- enqueue a message (on order pdb?)
 DECLARE
 enqueue_options    dbms_aq.enqueue_options_t;
 message_properties dbms_aq.message_properties_t;
 message_handle     RAW(16);
 my_payload         payload := payload('{"id":1,"First Name":"X","Middle Name" : "y", "Last Name" : "Z"}');
 BEGIN
 dbms_aq.enqueue(queue_name => 'paul.my_source_queue',
 enqueue_options => enqueue_options,
 message_properties => message_properties,
 payload => my_payload,
 msgid => message_handle);
 COMMIT;
 dbms_output.put_line('enqueued message with id ' || RAWTOHEX(message_handle));
 END;
 /

 -- dequeue a message (on inventory pdb?)
 DECLARE
 dequeue_options    dbms_aq.dequeue_options_t;
 message_properties dbms_aq.message_properties_t;
 message_handle     RAW(16);
 my_payload         payload;
 BEGIN
 dequeue_options.navigation := dbms_aq.first_message;
 dequeue_options.consumer_name := 'DEST_SUBSCRIBER';
 dbms_aq.dequeue(queue_name => 'paul.my_dest_queue',
 dequeue_options => dequeue_options,
 message_properties => message_properties,
 payload => my_payload,
 msgid => message_handle);
 dbms_output.put_line('dequeued message with id ' || RAWTOHEX(message_handle));
 COMMIT;
 END;
 /

 create database link testlinktoinv
 connect to system identified by inventorydb_high
 using
 '(description= (retry_count=20)(retry_delay=3)(address=(protocol=tcps)(port=1522)(host=adb.us-phoenix-1.oraclecloud.com))(connect_data=(service_name=mnisopbygm56hii_inventorydb_high.atp.oraclecloud.com))(security=(ssl_server_cert_dn="CN=adwc.uscom-east-1.oraclecloud.com,OU=Oracle BMCS US,O=Oracle Corporation,L=Redwood City,ST=California,C=US")))'

 GRANT EXECUTE ON DBMS_CLOUD_ADMIN TO orderuser;

 CREATE DATABASE LINK my_link CONNECT TO inventoryuser IDENTIFIED BY Welcome12345 USING 'inventorydb_high'
 */









/**

 //working commands...

 // as admin....
 GRANT EXECUTE ON DBMS_CLOUD_ADMIN TO orderuser
 GRANT EXECUTE ON DBMS_CLOUD TO orderuser
 GRANT CREATE DATABASE LINK TO orderuser

 // as orderuser...
 BEGIN
 DBMS_CLOUD.CREATE_CREDENTIAL (
 'objectstore_cred',
 'paul.parkinson',
 'Q:4qWo:7PYDph9KZomZQ');
 END;
 /

 # had to make bucket public as paul.parkinson/authtoken wasnt working thus no `credential_name => 'objectstore_cred',` arg
 # this can likely be done by any user as it is shared, I did it with admin
 BEGIN
 DBMS_CLOUD.GET_OBJECT(
 object_uri => 'https://objectstorage.us-phoenix-1.oraclecloud.com/n/stevengreenberginc/b/datademo_bucket/o/cwallet.sso',
 directory_name => 'DATA_PUMP_DIR');
 END;
 /

 BEGIN
 DBMS_CLOUD.CREATE_CREDENTIAL(
 credential_name => 'DB_LINK_CRED_TEST1',
 username => 'INVENTORYUSER',
 password => 'Welcome12345'
 );
 END;
 /

 BEGIN
 DBMS_CLOUD_ADMIN.CREATE_DATABASE_LINK(
 db_link_name => 'TESTLINK1',
 hostname => 'adb.us-phoenix-1.oraclecloud.com',
 port => '1522',
 service_name => 'mnisopbygm56hii_inventorydb_high.atp.oraclecloud.com',
 ssl_server_cert_dn => 'CN=adwc.uscom-east-1.oraclecloud.com,OU=Oracle BMCS US,O=Oracle Corporation,L=Redwood City,ST=California,C=US',
 credential_name => 'DB_LINK_CRED_TEST1',
 directory_name => 'DATA_PUMP_DIR');
 END;
 /

 SELECT count(*) FROM inventory@TESTLINK1;

 CREATE OR REPLACE TYPE payload AS OBJECT(jdata CLOB);
 /
 BEGIN
 dbms_aqadm.create_queue_table(
 queue_table        => 'orderuser.my_dest_queue_table1',
 queue_payload_type => 'payload',
 multiple_consumers => FALSE);
 dbms_aqadm.create_queue(queue_name  => 'orderuser.my_dest_queue1',
 queue_table => 'orderuser.my_dest_queue_table1');
 dbms_aqadm.start_queue(queue_name => 'orderuser.my_dest_queue1');
 END;
 /

 BEGIN
 dbms_aqadm.create_queue_table(
 queue_table        => 'orderuser.my_source_queue_table1',
 queue_payload_type => 'payload',
 multiple_consumers => true);
 dbms_aqadm.create_queue(queue_name  => 'orderuser.my_source_queue1',
 queue_table => 'orderuser.my_source_queue_table1');
 dbms_aqadm.start_queue(queue_name => 'orderuser.my_source_queue1');
 END;
 /

 DECLARE
 sub sys.aq$_agent := sys.aq$_agent(name     => 'dest_subscriber1',
 address  => NULL,
 protocol => NULL);
 BEGIN
 dbms_aqadm.add_subscriber(queue_name     => 'orderuser.my_dest_queue1',
 subscriber     => sub);
 END;
 /

 DECLARE
 sub sys.aq$_agent := sys.aq$_agent(name     => 'dest_subscriber1',
 address  => 'inventoryuser.my_dest_queue1@TESTLINK1',
 protocol => NULL);
 BEGIN
 dbms_aqadm.add_subscriber(queue_name     => 'orderuser.my_source_queue1',
 subscriber     => sub,
 queue_to_queue => TRUE);
 END;
 /

 BEGIN
 dbms_aqadm.schedule_propagation(queue_name  => 'orderuser.my_source_queue1',
 destination => 'TESTLINK1',
 latency     => 10,
 destination_queue => 'orderuser.my_dest_queue1');
 COMMIT;
 END;
 /

 DECLARE
 enqueue_options    dbms_aq.enqueue_options_t;
 message_properties dbms_aq.message_properties_t;
 message_handle     RAW(16);
 my_payload         payload := payload('{"id":1,"First Name":"X","Middle Name" : "y", "Last Name" : "Z"}');
 BEGIN
 dbms_aq.enqueue(queue_name => 'orderuser.my_source_queue1',
 enqueue_options => enqueue_options,
 message_properties => message_properties,
 payload => my_payload,
 msgid => message_handle);
 COMMIT;
 dbms_output.put_line('enqueued message with id ' || RAWTOHEX(message_handle));
 END;
 /




 DONE on inventory DB...

 BEGIN
 dbms_aqadm.create_queue_table(
 queue_table        => 'inventoryuser.my_dest_queue_table1',
 queue_payload_type => 'payload',
 multiple_consumers => TRUE);
 dbms_aqadm.create_queue(queue_name  => 'inventoryuser.my_dest_queue1',
 queue_table => 'inventoryuser.my_dest_queue_table1');
 dbms_aqadm.start_queue(queue_name => 'inventoryuser.my_dest_queue1');
 END;
 /



 DECLARE
 sub sys.aq$_agent := sys.aq$_agent(name     => 'dest_subscriber1',
 address  => NULL,
 protocol => NULL);
 BEGIN
 dbms_aqadm.add_subscriber(queue_name     => 'inventoryuser.my_dest_queue1',
 subscriber     => sub);
 END;
 /

 DONE ON ORDER db...

 BEGIN
 dbms_aqadm.schedule_propagation(queue_name  => 'orderuser.my_source_queue1',
 destination => 'TESTLINK1',
 latency     => 10,
 destination_queue => 'inventoryuser.my_dest_queue1');
 COMMIT;
 END;
 /
 BEGIN
 dbms_aqadm.unschedule_propagation(queue_name  => 'orderuser.my_source_queue1',
 destination => 'TESTLINK1',
 destination_queue => 'inventoryuser.my_dest_queue1');
 COMMIT;
 END;
 /

 DECLARE
 enqueue_options    dbms_aq.enqueue_options_t;
 message_properties dbms_aq.message_properties_t;
 message_handle     RAW(16);
 my_payload         payload := payload('{"id":2,"First Name":"XX","Middle Name" : "yY", "Last Name" : "ZZ"}');
 BEGIN
 dbms_aq.enqueue(queue_name => 'orderuser.my_source_queue1',
 enqueue_options => enqueue_options,
 message_properties => message_properties,
 payload => my_payload,
 msgid => message_handle);
 COMMIT;
 dbms_output.put_line('enqueued message with id ' || RAWTOHEX(message_handle));
 END;
 /

 -- dequeue a message
 DECLARE
 dequeue_options    dbms_aq.dequeue_options_t;
 message_properties dbms_aq.message_properties_t;
 message_handle     RAW(16);
 my_payload         payload;
 BEGIN
 dequeue_options.navigation := dbms_aq.first_message;
 dequeue_options.consumer_name := 'DEST_SUBSCRIBER1';
 dbms_aq.dequeue(queue_name => 'inventoryuser.my_dest_queue1',
 dequeue_options => dequeue_options,
 message_properties => message_properties,
 payload => my_payload,
 msgid => message_handle);
 dbms_output.put_line('dequeued message with id ' || RAWTOHEX(message_handle));
 COMMIT;
 END;
 /

 SELECT count(*) FROM INVENTORYUSER.MY_DEST_QUEUE_TABLE1@TESTLINK1;

 //end of working commands

 BEGIN
 DBMS_AQADM.STOP_QUEUE(queue_name => 'ORDERQUEUE');
 DBMS_AQADM.DROP_QUEUE(queue_name => 'ORDERQUEUE');
 DBMS_AQADM.DROP_QUEUE_TABLE(queue_table => 'ORDERQUEUE');
 END;

 */