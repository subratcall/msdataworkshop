package oracle.db.microservices;

import oracle.AQ.*;
import oracle.jms.*;

import javax.jms.*;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class PropagationSetup {
    // todo get these from env
    String orderuser = "orderuser";
    String orderpw = "Welcome12345";
    String inventoryuser = "inventoryuser";
    String inventorypw = "Welcome12345";
    String orderToInventoryLinkName = "ORDERTOINVENTORYLINK";
    String inventoryToOrderLinkName = "INVENTORYTOORDERLINK";
    String orderQueueName = "orderqueue";
    String orderQueueTableName = "orderqueuetable";
    String inventoryQueueName = "inventoryqueue";
    String inventoryQueueTableName = "inventoryqueuetable";


    public String createUsers(DataSource orderpdbDataSource, DataSource inventorypdbDataSource) throws SQLException {
        String returnValue = "";
        try {
            returnValue += createAQUser(orderpdbDataSource, orderuser, orderpw);
        } catch (SQLException ex) {
            ex.printStackTrace();
            returnValue += ex.toString();
        }
        try {
            returnValue += createAQUser(inventorypdbDataSource, inventoryuser, inventorypw);
        } catch (SQLException ex) {
            ex.printStackTrace();
            returnValue += ex.toString();
        }
        return returnValue;
    }

    Object createAQUser(DataSource ds, String queueOwner, String queueOwnerPW) throws SQLException {
        String outputString = "\nPropagationSetup.createAQUser ds = [" + ds + "], queueOwner = [" + queueOwner + "], queueOwnerPW = [" + queueOwnerPW + "]";
        System.out.println(outputString);
        Connection connection = ds.getConnection();
        System.out.println("PropagationSetup.createAQUser connection:" + connection);
        connection.createStatement().execute("grant pdb_dba to " + queueOwner + " identified by " + queueOwnerPW);
        connection.createStatement().execute("GRANT EXECUTE ON DBMS_CLOUD_ADMIN TO " + queueOwner);
        connection.createStatement().execute("GRANT EXECUTE ON DBMS_CLOUD TO " + queueOwner);
        connection.createStatement().execute("GRANT CREATE DATABASE LINK TO " + queueOwner);
        connection.createStatement().execute("grant unlimited tablespace to " + queueOwner);
        connection.createStatement().execute("grant connect, resource TO " + queueOwner);
        connection.createStatement().execute("grant aq_user_role TO " + queueOwner);
        connection.createStatement().execute("GRANT EXECUTE ON sys.dbms_aqadm TO " + queueOwner);
        connection.createStatement().execute("GRANT EXECUTE ON sys.dbms_aq TO " + queueOwner);
        connection.createStatement().execute("GRANT EXECUTE ON sys.dbms_aq TO " + queueOwner);
        //    sysDBAConnection.createStatement().execute("create table tracking (state number)");
        return outputString + " successful";
    }

    public String createDBLinks(DataSource orderpdbDataSource, DataSource inventorypdbDataSource) throws SQLException {
        String outputString = "\ncreateDBLinks...";
        System.out.println(outputString);
        outputString += createDBLinks(orderpdbDataSource, inventorypdbDataSource,
                orderuser, orderpw, inventoryuser, inventorypw,
                orderToInventoryLinkName, inventoryToOrderLinkName);
        outputString+="\nverifyDBLinks...";
        System.out.println(outputString);
        outputString += verifyDBLinks(orderpdbDataSource, inventorypdbDataSource,
                orderuser, orderpw, inventoryuser, inventorypw,
                orderToInventoryLinkName, inventoryToOrderLinkName);
        System.out.println(outputString);
        return outputString;
    }

    private String verifyDBLinks(DataSource orderdataSource, DataSource inventorydataSource,
                                 String orderuser, String orderpassword, String inventoryuser, String inventorypassword,
                                 String orderToInventoryLinkName, String inventoryToOrderLinkName) throws SQLException {
        String outputString = "\nPropagationSetup.verifyDBLinks " +
                "orderdataSource = [" + orderdataSource + "], " +
                "inventorydataSource = [" + inventorydataSource + "], " +
                "fromuser = [" + orderuser + "], orderpassword = [" + orderpassword + "], " +
                "inventoryuser = [" + inventoryuser + "], inventorypassword = [" + inventorypassword + "], " +
                " orderToInventoryLinkName = [" + orderToInventoryLinkName + "]" +
                ", inventoryToOrderLinkName = [" + inventoryToOrderLinkName + "]";
        // verify order to inventory...
        Connection orderconnection = orderdataSource.getConnection(orderuser, orderpassword);
        Connection iventoryconnection = inventorydataSource.getConnection(inventoryuser, inventorypassword);
        orderconnection.createStatement().execute("create table templinktest (id varchar(32))");
        iventoryconnection.createStatement().execute("create table templinktest (id varchar(32))");
        orderconnection.createStatement().execute("select count(*) from inventoryuser.templinktest@"+orderToInventoryLinkName);
        iventoryconnection.createStatement().execute("select count(*) from orderuser.templinktest@"+inventoryToOrderLinkName);
        orderconnection.createStatement().execute("drop table templinktest");
        iventoryconnection.createStatement().execute("drop table templinktest");
        return outputString;
    }

    private String createDBLinks(DataSource orderdataSource, DataSource inventorydataSource,
                                String orderuser, String orderpassword, String inventoryuser, String inventorypassword,
                                String orderToInventoryLinkName, String inventoryToOrderLinkName) throws SQLException {
        String outputString = "\nPropagationSetup.createDBLink " +
                "orderdataSource = [" + orderdataSource + "], " +
                "inventorydataSource = [" + inventorydataSource + "], " +
                "fromuser = [" + orderuser + "], orderpassword = [" + orderpassword + "], " +
                "inventoryuser = [" + inventoryuser + "], inventorypassword = [" + inventorypassword + "], " +
                " orderToInventoryLinkName = [" + orderToInventoryLinkName + "]" +
                ", inventoryToOrderLinkName = [" + inventoryToOrderLinkName + "]";
        System.out.println(outputString);
        // create link from order to inventory...
        Connection connection = orderdataSource.getConnection(orderuser, orderpassword);
        connection.createStatement().execute("BEGIN " +
                "DBMS_CLOUD.GET_OBJECT(" +
                "object_uri => 'https://objectstorage.us-phoenix-1.oraclecloud.com/p/W09lHtI_JWR8qR7w45P7hRn46y390V4hTGqWlso01Ds/n/stevengreenberginc/b/msdataworkshop/o/cwallet.sso', " +
                "directory_name => 'DATA_PUMP_DIR'); " +
                "END;");
        connection.createStatement().execute("BEGIN " +
                "DBMS_CLOUD.CREATE_CREDENTIAL(" +
                "credential_name => 'INVENTORYPDB_CRED'," +
                "username => 'INVENTORYUSER'," +
                "password => 'Welcome12345'" +
                ");" +
                "END;");
        connection.createStatement().execute("BEGIN " +
                "DBMS_CLOUD_ADMIN.CREATE_DATABASE_LINK(" +
                "db_link_name => '" + orderToInventoryLinkName + "'," +
                "hostname => 'adb.us-phoenix-1.oraclecloud.com'," +
                "port => '1522'," +
                "service_name => 'mnisopbygm56hii_inventorydb_high.atp.oraclecloud.com'," +
                "ssl_server_cert_dn => 'CN=adwc.uscom-east-1.oraclecloud.com,OU=Oracle BMCS US,O=Oracle Corporation,L=Redwood City,ST=California,C=US'," +
                "credential_name => 'INVENTORYPDB_CRED'," +
                "directory_name => 'DATA_PUMP_DIR');" +
                "END;");
        outputString+= "link from order to inventory complete, create link from inventory to order...";
        System.out.println(outputString);
        // create link from inventory to order ...
        connection = inventorydataSource.getConnection(inventoryuser, inventorypassword);
        connection.createStatement().execute("BEGIN " +
                "DBMS_CLOUD.GET_OBJECT(" +
                "object_uri => 'https://objectstorage.us-phoenix-1.oraclecloud.com/p/W09lHtI_JWR8qR7w45P7hRn46y390V4hTGqWlso01Ds/n/stevengreenberginc/b/msdataworkshop/o/cwallet.sso', " +
                "directory_name => 'DATA_PUMP_DIR'); " +
                "END;");
        connection.createStatement().execute("BEGIN " +
                "DBMS_CLOUD.CREATE_CREDENTIAL(" +
                "credential_name => 'ORDERPDB_CRED'," +
                "username => 'ORDERUSER'," +
                "password => 'Welcome12345'" +
                ");" +
                "END;");
        connection.createStatement().execute("BEGIN " +
                "DBMS_CLOUD_ADMIN.CREATE_DATABASE_LINK(" +
                "db_link_name => '" + inventoryToOrderLinkName + "'," +
                "hostname => 'adb.us-phoenix-1.oraclecloud.com'," +
                "port => '1522'," +
                "service_name => 'mnisopbygm56hii_orderdb_high.atp.oraclecloud.com'," +
                "ssl_server_cert_dn => 'CN=adwc.uscom-east-1.oraclecloud.com,OU=Oracle BMCS US,O=Oracle Corporation,L=Redwood City,ST=California,C=US'," +
                "credential_name => 'ORDERPDB_CRED'," +
                "directory_name => 'DATA_PUMP_DIR');" +
                "END;");
        outputString+= "link from inventory to order complete";
        System.out.println(outputString);
        return outputString;
    }


    public String setup(DataSource orderpdbDataSource, DataSource inventorypdbDataSource) {
        String returnString = "in setup...";
        //propagation of order queue from orderpdb to inventorypdb
        returnString += setup(orderpdbDataSource, inventorypdbDataSource, orderuser, orderpw, orderQueueName,
                orderQueueTableName, inventoryuser, inventorypw, orderToInventoryLinkName);
        //propagation of inventory queue from inventorypdb to orderpdb
        returnString += setup(inventorypdbDataSource, orderpdbDataSource, inventoryuser, inventorypw, inventoryQueueName,
                inventoryQueueTableName, orderuser, orderpw, orderToInventoryLinkName);
        return returnString;
    }

    private String setup(
            DataSource orderpdbDataSource, DataSource inventorypdbDataSource, String sourcename, String sourcepw,
            String sourcequeuename, String sourcequeuetable, String destinationuser, String destinationpw,
            String linkName) {
        String returnString = "orderpdbDataSource = [" + orderpdbDataSource + "], " +
                "inventorypdbDataSource = [" + inventorypdbDataSource + "], " +
                "sourcename = [" + sourcename + "], sourcepw = [" + sourcepw + "], " +
                "sourcequeuename = [" + sourcequeuename + "], " +
                "sourcequeuetable = [" + sourcequeuetable + "], destinationuser = [" + destinationuser + "], " +
                "destinationpw = [" + destinationpw + "], linkName = [" + linkName + "]";
        System.out.println(returnString);
        try {
            TopicConnectionFactory tcfact = AQjmsFactory.getTopicConnectionFactory(orderpdbDataSource);
            TopicConnection tconn = tcfact.createTopicConnection(sourcename, sourcepw);
            QueueConnectionFactory qcfact = AQjmsFactory.getQueueConnectionFactory(inventorypdbDataSource);
            QueueConnection qconn = qcfact.createQueueConnection(destinationuser, destinationpw);
            TopicSession tsess = tconn.createTopicSession(true, Session.CLIENT_ACKNOWLEDGE);
            System.out.println("PropagationSetup.setup tsess:" + tsess);
            QueueSession qsess = qconn.createQueueSession(true, Session.CLIENT_ACKNOWLEDGE);
            System.out.println("PropagationSetup.setup qsess:" + qsess);
            tconn.start();
            qconn.start();
//            setupTopicAndQueue(tsess, qsess, sourcename, destinationuser, sourcequeuename, sourcequeuetable);
            performJmsOperations(tsess, qsess, sourcename, destinationuser, sourcequeuename, linkName);
            tsess.close();
            tconn.close();
            qsess.close();
            qconn.close();
            System.out.println("success");
            returnString += "\n ...success";
            return returnString;
        } catch (Exception ex) {
            System.out.println("Exception-1: " + ex);
            ex.printStackTrace();
            return returnString += "\n ..." + ex.toString();
        }
    }

    public static void setupTopicAndQueue(
            TopicSession topicSession, QueueSession queueSession,
            String topicuser, String queueuser,
            String name,
            String tableName) throws Exception {
        try {
            System.out.println("drop source Queue Table...");
            try {
                AQQueueTable qtable = ((AQjmsSession) topicSession).getQueueTable(topicuser, tableName);
                qtable.drop(true);
            } catch (Exception e) {
                System.out.println("Exception in dropping source " + e);
            }
            System.out.println("drop destination Queue Table...");
            try {
                AQQueueTable qtable = ((AQjmsSession) queueSession).getQueueTable(queueuser, tableName);
                qtable.drop(true);
            } catch (Exception e) {
                System.out.println("Exception in dropping destination " + e);
            }
            System.out.println("Creating Input Topic Table...");
            AQQueueTableProperty aqQueueTableProperty = new AQQueueTableProperty("SYS.AQ$_JMS_TEXT_MESSAGE");
            aqQueueTableProperty.setComment("input topic");
            aqQueueTableProperty.setMultiConsumer(true);
            aqQueueTableProperty.setCompatible("8.1");
            aqQueueTableProperty.setPayloadType("SYS.AQ$_JMS_TEXT_MESSAGE");
            AQQueueTable inputTopicTable = ((AQjmsSession) topicSession).createQueueTable(topicuser, tableName, aqQueueTableProperty);

            System.out.println("Creating Propagation Queue Table...");
            AQQueueTableProperty qtprop2 = new AQQueueTableProperty("SYS.AQ$_JMS_TEXT_MESSAGE");
            qtprop2.setComment("propagation queue");
            qtprop2.setPayloadType("SYS.AQ$_JMS_TEXT_MESSAGE");
            qtprop2.setMultiConsumer(false);
            qtprop2.setCompatible("8.1");
            AQQueueTable propagationQueueTable = ((AQjmsSession) queueSession).createQueueTable(queueuser, tableName, qtprop2);

            System.out.println("Creating Topic input_queue...");
            AQjmsDestinationProperty aqjmsDestinationProperty = new AQjmsDestinationProperty();
            aqjmsDestinationProperty.setComment("create topic ");
            Topic topic1 = ((AQjmsSession) topicSession).createTopic(inputTopicTable, name, aqjmsDestinationProperty);

            System.out.println("Creating queue prop_queue...");
            aqjmsDestinationProperty.setComment("create queue");
            Queue queue1 = ((AQjmsSession) queueSession).createQueue(propagationQueueTable, name, aqjmsDestinationProperty);

            ((AQjmsDestination) topic1).start(topicSession, true, true);
            ((AQjmsDestination) queue1).start(queueSession, true, true);
            System.out.println("Successfully setup topic and queue");
        } catch (Exception ex) {
            System.out.println("Error in setupTopic: " + ex);
            throw ex;
        }
    }


    public static void performJmsOperations(
            TopicSession tsess, QueueSession qsess,
            String sourcetopicuser, String destinationqueueuser,
            String name,
            String linkName)
            throws Exception {
        try {
            System.out.println("Setup topic/source and queue/destination for propagation...");
            Topic topic1 = ((AQjmsSession) tsess).getTopic(sourcetopicuser, name);
            Queue queue1 = ((AQjmsSession) qsess).getQueue(destinationqueueuser, name);
            System.out.println("Creating Topic Subscribers... queue1:" + queue1.getQueueName());
            AQjmsConsumer[] subs = new AQjmsConsumer[1];
            subs[0] = (AQjmsConsumer) qsess.createConsumer(queue1);
            System.out.println("_____________________________________________");
            System.out.println("PropagationSetup.performJmsOperations queue1.getQueueName():" + queue1.getQueueName());
            System.out.println("PropagationSetup.performJmsOperations name (adding inventoryuser. to this):" + name);
            System.out.println("_____________________________________________");
            AQjmsAgent agt = new AQjmsAgent("", "inventoryuser." + name + "@" + linkName);
            ((AQjmsSession) tsess).createRemoteSubscriber( topic1, agt, "JMSPriority = 2");
            ((AQjmsDestination) topic1).schedulePropagation(
                    tsess, linkName, null, null, null, new Double(0));
            sendMessages(tsess, topic1);
            Thread.sleep(50000);
//            receiveMessages(qsess, subs);
//            ((AQjmsDestination) topic1).unschedulePropagation(tsess, linkName);
        } catch (Exception e) {
            System.out.println("Error in performJmsOperations: " + e);
            throw e;
        }
    }


    String unscheduleOrderToInventoryPropagation(DataSource orderpdbDataSource)  {
        System.out.println("PropagationSetup.unscheduleOrderToInventoryPropagation");
        TopicConnection tconn = null;
        try {
            tconn = AQjmsFactory.getTopicConnectionFactory(orderpdbDataSource).createTopicConnection(
                    "orderuser", "Welcome12345");
        TopicSession tsess = tconn.createTopicSession(true, Session.CLIENT_ACKNOWLEDGE);
        Topic topic1 = ((AQjmsSession) tsess).getTopic("orderuser", "");
        ((AQjmsDestination) topic1).unschedulePropagation(tsess, orderToInventoryLinkName);
        } catch (JMSException e) {
            e.printStackTrace();
            return e.toString();
        }
        return "success";
    }

    private static void sendMessages(TopicSession tsess, Topic topic1) throws JMSException {
        System.out.println("Publish messages...");
        TextMessage objmsg = tsess.createTextMessage();
        TopicPublisher publisher = tsess.createPublisher(topic1);
        int i = 1;
        String[] cities={"BELMONT","REDWOOD SHORES", "SUNNYVALE", "BURLINGAME" };
            objmsg.setIntProperty("Id",i) ;
            objmsg.setStringProperty("City",cities[3]) ;
            objmsg.setIntProperty("Priority",(1+ (i%3))) ;
            objmsg.setText(i+":"+"message# "+i+":"+500) ;
            objmsg.setJMSCorrelationID(""+i) ;
            objmsg.setJMSPriority(1+(i%3)) ;
            publisher.publish(topic1,objmsg, DeliveryMode.PERSISTENT,
                    1 +(i%3), AQjmsConstants.EXPIRATION_NEVER);
        publisher.publish(topic1, objmsg, DeliveryMode.PERSISTENT,3, AQjmsConstants.EXPIRATION_NEVER);
        System.out.println("Commit now and sleep...");
        tsess.commit();
    }

    private static void receiveMessages(QueueSession qsess, AQjmsConsumer[] subs) throws JMSException {
        System.out.println("Receive Messages...");
        for (int i = 0; i < subs.length; i++) {
            System.out.println("\n\nMessages for subscriber : " + i);
            if (subs[i].getMessageSelector() != null) {
                System.out.println("  with selector: " +   subs[i].getMessageSelector());
            }
            boolean done = false;
            while (!done) {
                try {
                    TextMessage robjmsg = (TextMessage) (subs[i].receiveNoWait());
                    if (robjmsg != null) {
                        String rTextMsg = robjmsg.getText();
                        System.out.println("rTextMsg " + rTextMsg);
                        System.out.print(" Pri: " + robjmsg.getJMSPriority());
                        System.out.print(" Message: " + robjmsg.getIntProperty("Id"));
                        System.out.print(" " + robjmsg.getStringProperty("City"));
                        System.out.println(" " + robjmsg.getIntProperty("Priority"));
                    } else {
                        System.out.println("No more messages.");
                        done = true;
                    }
                    qsess.commit();
                } catch (Exception e) {
                    System.out.println("Error in performJmsOperations: " + e);
                    done = true;
                }
            }
        }
    }


    // UNUSED HERE TO END.... todo delete and...
    // currently there is a manual step for...
    //  1. the user to upload wallets to objectstore and provide the url of the wallet for GET_OBJECT
 //     2. provide info from tnsnames (hostname, port, service_name, and ssl_server_cert_dn) to create dblink
//       * user also needs to provide username and pw for CREATE_CREDENTIAL but it may be safest/best to not automate that


    private String dropUser() {
        return "<h2>Drop User...</h2>" +
                "<form action=\"execute\" method=\"get\">" +
                "    user <input type=\"text\" name=\"user\" size=\"20\" value=\"\"><br>" +
                "    password <input type=\"text\" name=\"password\" size=\"20\" value=\"\"><br>" +
                "<textarea id=\"w3mission\" rows=\"50\" cols=\"20\">" +
                "execute dbms_aqadm.unschedule_propagation(queue_name => 'paul.orders', " +
                "destination => 'inventorydb_link', destination_queue => 'paul.orders_propqueue');" +
                " execute dbms_lock.sleep(10);" +
                " DROP USER paul CASCADE" +
                " /" +
                "" +
                " GRANT DBA TO paul IDENTIFIED BY paul" +
                " /" +
                "</textarea>" +
                "</form>";
    }


    String putWallet(DataSource dataSource) throws SQLException {
        Connection connection = dataSource.getConnection();
        connection.createStatement().execute("BEGIN " +
                "DBMS_CLOUD.GET_OBJECT(" +
                "object_uri => 'file:///Users/pparkins/Downloads/Wallet_DB202002011726/cwallet.sso', " +
                "directory_name => 'DATA_PUMP_DIR'); " +
                "END;");
        return "success";
    }

    String putWallet0(DataSource dataSource) throws SQLException {


        Connection connection = dataSource.getConnection();
        connection.createStatement().execute("BEGIN " +
                "DBMS_CLOUD.CREATE_CREDENTIAL ( " +
                "'objectstore_cred', " +
                "'paul.parkinson', " +
                "'Q:4qWo:7PYDph9KZomZQ'); " +
                "END;");
        connection.createStatement().execute("BEGIN " +
                "        DBMS_CLOUD.put_object(" +
                "                credential_name => 'objectstore_cred', " +
                "                object_uri      => '/Users/pparkins/Downloads/Wallet_DB202002011726/cwallet.sso', " +
                "                directory_name  => 'DATA_PUMP_DIR', " +
                "                file_name       => 'cwallet.sso');" +
                "        END;");
// BEGIN
// DBMS_CLOUD.CREATE_CREDENTIAL (
// 'objectstore_cred',
// 'paul.parkinson',
// 'Q:4qWo:7PYDph9KZomZQ');
// END;
// /
//        pre-authenticated request to inventorypdb bucket wallet.sso
//        https://objectstorage.us-phoenix-1.oraclecloud.com/p/xW-cwallet.sso
//        pre-authenticated request to orderpdb bucket wallet.sso
//        https://objectstorage.us-phoenix-1.oraclecloud.com/p/U3KJ_dZsNeF8Ycwallet.sso
//        https://oracle-base.com/articles/vm/oracle-cloud-autonomous-data-warehouse-adw-import-data-from-object-store
//        BEGIN
//        DBMS_CLOUD.put_object(
//                credential_name => 'obj_store_cred',
//                object_uri      => 'https://s3-eu-west-1.amazonaws.com/my-sh-data-bucket/import.log',
//                directory_name  => 'DATA_PUMP_DIR',
//                file_name       => 'import.log');
//        END;
///
        return "success";
    }
}

