package oracle.db.microservices;

import oracle.AQ.*;
import oracle.jms.*;

import javax.jms.*;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class PropagationSetup {
    String orderQueueName = "orderqueue";
    String orderQueueTableName = "orderqueuetable";
    String inventoryQueueName = "inventoryqueue";
    String inventoryQueueTableName = "inventoryqueuetable";


    public String createInventoryTable(DataSource inventorypdbDataSource) throws SQLException {
        System.out.println("PropagationSetup.createInventoryTable");
        String returnValue = "PropagationSetup.createInventoryTable\n";
        try {
            inventorypdbDataSource.getConnection().createStatement().executeUpdate(
                    "create table inventory (inventoryid varchar(16), inventorylocation varchar(32), inventorycount integer)");
            returnValue += "success";
        } catch (SQLException ex) {
            ex.printStackTrace();
            returnValue += ex.toString();
        }
        return returnValue;
    }

    public String createUsers(DataSource orderpdbDataSource, DataSource inventorypdbDataSource) throws SQLException {
        String returnValue = "";
        try {
            returnValue += createAQUser(orderpdbDataSource, ATPAQAdminResource.orderuser, ATPAQAdminResource.orderpw);
        } catch (SQLException ex) {
            ex.printStackTrace();
            returnValue += ex.toString();
        }
        try {
            returnValue += createAQUser(inventorypdbDataSource, ATPAQAdminResource.inventoryuser, ATPAQAdminResource.inventorypw);
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

    String createDBLinks(DataSource orderpdbDataSource, DataSource inventorypdbDataSource) throws SQLException {
        String outputString = "\ncreateDBLinks...";
        System.out.println(outputString);
        outputString += createDBLinks(orderpdbDataSource, inventorypdbDataSource,
                ATPAQAdminResource.orderuser, ATPAQAdminResource.orderpw,
                ATPAQAdminResource.inventoryuser, ATPAQAdminResource.inventorypw,
                ATPAQAdminResource.orderToInventoryLinkName, ATPAQAdminResource.inventoryToOrderLinkName);
        outputString = verifyDBLinks(orderpdbDataSource, inventorypdbDataSource, outputString);
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
        outputString += "link from order to inventory complete, create link from inventory to order...";
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
        outputString += "link from inventory to order complete";
        System.out.println(outputString);
        return outputString;
    }


    String verifyDBLinks(DataSource orderpdbDataSource, DataSource inventorypdbDataSource, String outputString) throws SQLException {
        outputString += "\nverifyDBLinks...";
        System.out.println(outputString);
        outputString += verifyDBLinks(orderpdbDataSource, inventorypdbDataSource,
                ATPAQAdminResource.orderuser, ATPAQAdminResource.orderpw,
                ATPAQAdminResource.inventoryuser, ATPAQAdminResource.inventorypw,
                ATPAQAdminResource.orderToInventoryLinkName, ATPAQAdminResource.inventoryToOrderLinkName);
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
        Connection orderconnection = orderdataSource.getConnection(orderuser, orderpassword);
        Connection inventoryconnection = inventorydataSource.getConnection(inventoryuser, inventorypassword);
        System.out.println("PropagationSetup.verifyDBLinks orderconnection:" + orderconnection +
                " inventoryconnection:" + inventoryconnection);
        orderconnection.createStatement().execute("create table templinktest (id varchar(32))");
        System.out.println("PropagationSetup.verifyDBLinks temp table created on order");
        inventoryconnection.createStatement().execute("create table templinktest (id varchar(32))");
        System.out.println("PropagationSetup.verifyDBLinks temp table created on inventory");
        // verify orderuser select on inventorytable using link...
        orderconnection.createStatement().execute("select count(*) from inventoryuser.templinktest@" + orderToInventoryLinkName);
        System.out.println("PropagationSetup.verifyDBLinks select on inventoryuser.templinktest");
        // verify inventoryuser select to inventory using link  ...
        inventoryconnection.createStatement().execute("select count(*) from orderuser.templinktest@" + inventoryToOrderLinkName);
        System.out.println("PropagationSetup.verifyDBLinks select on orderuser.templinktest");
        orderconnection.createStatement().execute("drop table templinktest");
        inventoryconnection.createStatement().execute("drop table templinktest");
        return outputString;
    }

    public String setup(DataSource orderpdbDataSource, DataSource inventorypdbDataSource,
                        boolean isSetupOrderToInventory, boolean isSetupInventoryToOrder) {
        String returnString = "in setup... " +
                "isSetupOrderToInventory:" + isSetupOrderToInventory + " isSetupInventoryToOrder:" + isSetupInventoryToOrder;
        //propagation of order queue from orderpdb to inventorypdb
        if (isSetupOrderToInventory) returnString += setup(orderpdbDataSource, inventorypdbDataSource,
                ATPAQAdminResource.orderuser, ATPAQAdminResource.orderpw, orderQueueName,
                orderQueueTableName, ATPAQAdminResource.inventoryuser, ATPAQAdminResource.inventorypw,
                ATPAQAdminResource.orderToInventoryLinkName, false);
        //propagation of inventory queue from inventorypdb to orderpdb
        if (isSetupInventoryToOrder) returnString += setup(inventorypdbDataSource, orderpdbDataSource,
                ATPAQAdminResource.inventoryuser, ATPAQAdminResource.inventorypw, inventoryQueueName,
                inventoryQueueTableName, ATPAQAdminResource.orderuser, ATPAQAdminResource.orderpw,
                ATPAQAdminResource.inventoryToOrderLinkName, false);
        return returnString;
    }

    public String testOrderToInventory(DataSource orderpdbDataSource, DataSource inventorypdbDataSource) {
        String returnString = "in testOrderToInventory...";
        //propagation of order queue from orderpdb to inventorypdb
        returnString += setup(orderpdbDataSource, inventorypdbDataSource,
                ATPAQAdminResource.orderuser, ATPAQAdminResource.orderpw, orderQueueName,
                orderQueueTableName, ATPAQAdminResource.inventoryuser, ATPAQAdminResource.inventorypw,
                ATPAQAdminResource.orderToInventoryLinkName, true);
        return returnString;
    }

    public String testInventoryToOrder(DataSource orderpdbDataSource, DataSource inventorypdbDataSource) {
        String returnString = "in testInventoryToOrder...";
        //propagation of inventory queue from inventorypdb to orderpdb
        returnString += setup(inventorypdbDataSource, orderpdbDataSource,
                ATPAQAdminResource.inventoryuser, ATPAQAdminResource.inventorypw, inventoryQueueName,
                inventoryQueueTableName, ATPAQAdminResource.orderuser, ATPAQAdminResource.orderpw,
                ATPAQAdminResource.inventoryToOrderLinkName, true);
        return returnString;
    }

    private String setup(
            DataSource sourcepdbDataSource, DataSource targetpdbDataSource, String sourcename, String sourcepw,
            String sourcequeuename, String sourcequeuetable, String targetuser, String targetpw,
            String linkName, boolean isTest) {
        String returnString = "sourcepdbDataSource = [" + sourcepdbDataSource + "], " +
                "targetpdbDataSource = [" + targetpdbDataSource + "], " +
                "sourcename = [" + sourcename + "], sourcepw = [" + sourcepw + "], " +
                "sourcequeuename = [" + sourcequeuename + "], " +
                "sourcequeuetable = [" + sourcequeuetable + "], targetuser = [" + targetuser + "], " +
                "targetpw = [" + targetpw + "], linkName = [" + linkName + "] isTest = " + isTest;
        System.out.println(returnString);
        try {
            TopicConnectionFactory tcfact = AQjmsFactory.getTopicConnectionFactory(sourcepdbDataSource);
            TopicConnection tconn = tcfact.createTopicConnection(sourcename, sourcepw);
            QueueConnectionFactory qcfact = AQjmsFactory.getQueueConnectionFactory(targetpdbDataSource);
            QueueConnection qconn = qcfact.createQueueConnection(targetuser, targetpw);
            TopicSession tsess = tconn.createTopicSession(true, Session.CLIENT_ACKNOWLEDGE);
            System.out.println("PropagationSetup.setup source topicsession:" + tsess);
            QueueSession qsess = qconn.createQueueSession(true, Session.CLIENT_ACKNOWLEDGE);
            System.out.println("PropagationSetup.setup destination queuesession:" + qsess);
            tconn.start();
            qconn.start();
            if (!isTest) setupTopicAndQueue(tsess, qsess, sourcename, targetuser, sourcequeuename, sourcequeuetable);
            performJmsOperations(tsess, qsess, sourcename, targetuser, sourcequeuename, linkName, isTest);
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
            TopicSession topicSession, QueueSession queueSession,
            String sourcetopicuser, String destinationqueueuser,
            String name,
            String linkName, boolean isTest)
            throws Exception {
        try {
            System.out.println("Setup topic/source and queue/destination for propagation... isTest:" + isTest);
            Topic topic1 = ((AQjmsSession) topicSession).getTopic(sourcetopicuser, name);
            Queue queue = ((AQjmsSession) queueSession).getQueue(destinationqueueuser, name);
            System.out.println("Creating Topic Subscribers... queue:" + queue.getQueueName());
            AQjmsConsumer[] subs = new AQjmsConsumer[1];
            subs[0] = (AQjmsConsumer) queueSession.createConsumer(queue);
            if(!isTest)createRemoteSubAndSchedulePropagation(topicSession, destinationqueueuser, name, linkName, topic1, queue);
            sendMessages(topicSession, topic1);
            Thread.sleep(50000);
            receiveMessages(queueSession, subs);
//            ((AQjmsDestination) topic1).unschedulePropagation(topicSession, linkName);
        } catch (Exception e) {
            System.out.println("Error in performJmsOperations: " + e);
            throw e;
        }
    }

    private static void createRemoteSubAndSchedulePropagation(TopicSession topicSession, String destinationqueueuser, String name, String linkName, Topic topic1, Queue queue) throws JMSException, SQLException {
        System.out.println("_____________________________________________");
        System.out.println("PropagationSetup.performJmsOperations queue.getQueueName():" + queue.getQueueName());
        System.out.println("PropagationSetup.performJmsOperations name (prefixing " + destinationqueueuser + ". to this):" + name);
        System.out.println("_____________________________________________");
        AQjmsAgent agt = new AQjmsAgent("", destinationqueueuser + "." + name + "@" + linkName);
        ((AQjmsSession) topicSession).createRemoteSubscriber(topic1, agt, "JMSPriority = 2");
        ((AQjmsDestination) topic1).schedulePropagation(
                topicSession, linkName, null, null, null, new Double(0));
    }


    String unscheduleOrderToInventoryPropagation(DataSource orderpdbDataSource, String topicUser, String topicPassword, String linkName) {
        System.out.println("PropagationSetup.unscheduleOrderToInventoryPropagation");
        TopicConnection tconn = null;
        try {
            tconn = AQjmsFactory.getTopicConnectionFactory(orderpdbDataSource).createTopicConnection(
                    topicUser, topicPassword);
            TopicSession tsess = tconn.createTopicSession(true, Session.CLIENT_ACKNOWLEDGE);
            Topic topic1 = ((AQjmsSession) tsess).getTopic(topicUser, "");
            ((AQjmsDestination) topic1).unschedulePropagation(tsess, linkName);
        } catch (JMSException e) {
            e.printStackTrace();
            return e.toString();
        }
        return "success";
    }

    String enablePropagation(DataSource orderpdbDataSource, String topicUser, String topicPassword, String linkName) {
        System.out.println("PropagationSetup.schedulePropagation");
        TopicConnection tconn = null;
        try {
            tconn = AQjmsFactory.getTopicConnectionFactory(orderpdbDataSource).createTopicConnection(
                    topicUser, topicPassword);
            TopicSession tsess = tconn.createTopicSession(true, Session.CLIENT_ACKNOWLEDGE);
            Topic topic1 = ((AQjmsSession) tsess).getTopic(topicUser, "");
            ((AQjmsDestination) topic1).enablePropagationSchedule(tsess, linkName);
        } catch (JMSException e) {
            e.printStackTrace();
            return e.toString();
        }
        return "success";
    }

    private static void sendMessages(TopicSession topicSession, Topic topic) throws JMSException {
        System.out.println("Publish messages...");
        TextMessage objmsg = topicSession.createTextMessage();
        TopicPublisher publisher = topicSession.createPublisher(topic);
        objmsg.setIntProperty("Id", 1);
        objmsg.setStringProperty("City", "Philadelphia");
        objmsg.setText(1 + ":" + "message# " + 1 + ":" + 500);
        objmsg.setIntProperty("Priority", 2);
        objmsg.setJMSCorrelationID("" + 12);
        objmsg.setJMSPriority(2);
        publisher.publish(topic, objmsg, DeliveryMode.PERSISTENT, 2, AQjmsConstants.EXPIRATION_NEVER);
        publisher.publish(topic, objmsg, DeliveryMode.PERSISTENT, 3, AQjmsConstants.EXPIRATION_NEVER);
        System.out.println("Commit now and sleep...");
        topicSession.commit();
    }

    private static void receiveMessages(QueueSession qsess, AQjmsConsumer[] subs) throws JMSException {
        System.out.println("Receive Messages...");
        for (int i = 0; i < subs.length; i++) {
            System.out.println("\n\nMessages for subscriber : " + i);
            if (subs[i].getMessageSelector() != null) {
                System.out.println("  with selector: " + subs[i].getMessageSelector());
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

    private void cleanup(DataSource orderpdbDataSource, DataSource inventorypdbDataSource) throws SQLException {
//            ((AQjmsDestination) topic1).unschedulePropagation(topicSession, linkName);
        orderpdbDataSource.getConnection().createStatement().execute(
                "execute dbms_aqadm.unschedule_propagation(queue_name => 'paul.orders', " +
                "destination => 'inventorydb_link', destination_queue => 'paul.orders_propqueue');" +
                " execute dbms_lock.sleep(10);" +
                " DROP USER paul CASCADE" +
                " GRANT DBA TO paul IDENTIFIED BY paul");
    }
}

