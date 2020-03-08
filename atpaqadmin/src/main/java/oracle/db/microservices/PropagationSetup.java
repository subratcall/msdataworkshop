package oracle.db.microservices;

import oracle.AQ.*;
import oracle.jms.*;

import javax.jms.*;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class PropagationSetup {
    // todo get these from env
    String topicuser = "orderuser";
    String topicpw = "Welcome12345";
    String queueuser = "inventoryuser";
    String queuepw = "Welcome12345";
    String orderToInventoryLinkName = "ORDERTOINVENTORYLINK";
    String inventoryToOrderLinkName = "INVENTORYTOORDERLINK";
    String orderQueueName = "orderqueue";
    String orderQueueTableName = "orderqueuetable";
    String inventoryQueueName = "inventoryqueue";
    String inventoryQueueTableName = "inventoryqueuetable";


    public String createUsers(DataSource orderpdbDataSource, DataSource inventorypdbDataSource) throws SQLException {
        String returnValue = "";
        returnValue += createAQUser(orderpdbDataSource, topicuser, topicpw);
        returnValue += createAQUser(inventorypdbDataSource, queueuser, queuepw);
        return returnValue;
    }

    Object createAQUser(DataSource ds, String queueOwner, String queueOwnerPW) throws SQLException {
        String outputString = "\nPropagationSetup.createAQUser ds = [" + ds + "], queueOwner = [" + queueOwner + "], queueOwnerPW = [" + queueOwnerPW + "]";
        System.out.println(outputString);
        Connection sysDBAConnection = ds.getConnection();
        sysDBAConnection.createStatement().execute("grant pdb_dba to " + queueOwner + " identified by " + queueOwnerPW);
        sysDBAConnection.createStatement().execute("GRANT EXECUTE ON DBMS_CLOUD_ADMIN TO " + queueOwner);
        sysDBAConnection.createStatement().execute("GRANT EXECUTE ON DBMS_CLOUD TO " + queueOwner);
        sysDBAConnection.createStatement().execute("GRANT CREATE DATABASE LINK TO " + queueOwner);
        sysDBAConnection.createStatement().execute("grant unlimited tablespace to " + queueOwner);
        sysDBAConnection.createStatement().execute("grant connect, resource TO " + queueOwner);
        sysDBAConnection.createStatement().execute("grant aq_user_role TO " + queueOwner);
        sysDBAConnection.createStatement().execute("GRANT EXECUTE ON sys.dbms_aqadm TO " + queueOwner);
        sysDBAConnection.createStatement().execute("GRANT EXECUTE ON sys.dbms_aq TO " + queueOwner);
        sysDBAConnection.createStatement().execute("GRANT EXECUTE ON sys.dbms_aq TO " + queueOwner);
        //    sysDBAConnection.createStatement().execute("create table tracking (state number)");
        return outputString + " successful";
    }

    public String createDBLinks(DataSource orderpdbDataSource, DataSource inventorypdbDataSource) throws SQLException {
        String outputString = "\ncreateDBLinks...";
        System.out.println(outputString);
        outputString += createDBLink(orderpdbDataSource, inventorypdbDataSource,
                topicuser, topicpw, queueuser, queuepw,
                orderToInventoryLinkName, inventoryToOrderLinkName);
        outputString+="\nverifyDBLinks...";
        System.out.println(outputString);
        outputString += verifyDBLinks(orderpdbDataSource, inventorypdbDataSource,
                topicuser, topicpw, queueuser, queuepw,
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

    private String createDBLink(DataSource orderdataSource, DataSource inventorydataSource,
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
                "object_uri => 'https://objectstorage.us-phoenix-1.oraclecloud.com/p/xW-H_hj4mIiFliIxccoFWcZcJZL0uFcZZ8mW8dZyVug/n/stevengreenberginc/b/inventorypdb/o/cwallet.sso', " +
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
        // create link from inventory to order ...
        connection = inventorydataSource.getConnection(inventoryuser, inventorypassword);
        connection.createStatement().execute("BEGIN " +
                "DBMS_CLOUD.GET_OBJECT(" +
                "object_uri => 'https://objectstorage.us-phoenix-1.oraclecloud.com/p/U3KJ_dZsNeF8Yb_muUvxJUyMudpfvKZdbC7DsugzJsQ/n/stevengreenberginc/b/orderpdb/o/cwallet.sso', " +
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
                "service_name => 'mnisopbygm56hii_db202002011726_high.atp.oraclecloud.com'," +
                "ssl_server_cert_dn => 'CN=adwc.uscom-east-1.oraclecloud.com,OU=Oracle BMCS US,O=Oracle Corporation,L=Redwood City,ST=California,C=US'," +
                "credential_name => 'ORDERPDB_CRED'," +
                "directory_name => 'DATA_PUMP_DIR');" +
                "END;");
        return outputString;
    }


    public String setup(DataSource orderpdbDataSource, DataSource inventorypdbDataSource) {

        TopicSession tsess = null;
        TopicConnectionFactory tcfact = null;
        TopicConnection tconn = null;
        QueueConnectionFactory qcfact = null;
        QueueConnection qconn = null;
        QueueSession qsess = null;

        try {
//                tcfact = AQjmsFactory.getTopicConnectionFactory(myjdbcURL, myProperties);
            tcfact = AQjmsFactory.getTopicConnectionFactory(orderpdbDataSource);
            tconn = tcfact.createTopicConnection(topicuser, topicpw);
            qcfact = AQjmsFactory.getQueueConnectionFactory(inventorypdbDataSource);
            qconn = qcfact.createQueueConnection(queueuser, queuepw);
            tsess = tconn.createTopicSession(true, Session.CLIENT_ACKNOWLEDGE);
            System.out.println("PropagationSetup.setup tsess:" + tsess);
            qsess = qconn.createQueueSession(true, Session.CLIENT_ACKNOWLEDGE);
            System.out.println("PropagationSetup.setup qsess:" + qsess);

            tconn.start();
            qconn.start();
            //todo do the same in the opposite direction...
            setupTopicAndQueue(tsess, qsess, topicuser, queueuser, orderQueueName, inventoryQueueName, orderQueueTableName, inventoryQueueTableName);
            performJmsOperations(tsess, qsess, topicuser, queueuser, orderQueueName, inventoryQueueName, orderToInventoryLinkName);
            tsess.close();
            tconn.close();
            qsess.close();
            qconn.close();
            System.out.println("success");
            return "success";
        } catch (Exception ex) {
            System.out.println("Exception-1: " + ex);
            ex.printStackTrace();
            return ex.toString();
        }
    }

    public static void setupTopicAndQueue(
            TopicSession tsess, QueueSession qsess,
            String topicuser, String queueuser,
            String sourcetopicname, String destinationqueuename,
            String sourcetopictablename, String destinationqueuetablename) throws Exception {
        try {
            System.out.println("drop source Queue Table...");
            try {
                AQQueueTable qtable = ((AQjmsSession) tsess).getQueueTable(topicuser, sourcetopictablename);
                qtable.drop(true);
            } catch (Exception e) {
                System.out.println("Exception in dropping source " + e);
            }
            System.out.println("drop destination Queue Table...");
            try {
                AQQueueTable qtable = ((AQjmsSession) qsess).getQueueTable(queueuser, destinationqueuetablename);
                qtable.drop(true);
            } catch (Exception e) {
                System.out.println("Exception in dropping destination " + e);
            }
            System.out.println("Creating Input Queue Table...");
            AQQueueTableProperty qtprop1 = new AQQueueTableProperty("SYS.AQ$_JMS_TEXT_MESSAGE");
            qtprop1.setComment("input queue");
            qtprop1.setMultiConsumer(true);
            qtprop1.setCompatible("8.1");
            qtprop1.setPayloadType("SYS.AQ$_JMS_TEXT_MESSAGE");
            AQQueueTable table1 = ((AQjmsSession) tsess).createQueueTable(topicuser, sourcetopictablename, qtprop1);
            System.out.println("Creating Propagation Queue Table...");
            AQQueueTableProperty qtprop2 = new AQQueueTableProperty("SYS.AQ$_JMS_TEXT_MESSAGE");
            qtprop2.setComment("Propagation queue");
            qtprop2.setPayloadType("SYS.AQ$_JMS_TEXT_MESSAGE");
            qtprop2.setMultiConsumer(false);
            qtprop2.setCompatible("8.1");
            AQQueueTable table2 = ((AQjmsSession) qsess).createQueueTable(queueuser, destinationqueuetablename, qtprop2);
            System.out.println("Creating Topic input_queue...");
            AQjmsDestinationProperty dprop = new AQjmsDestinationProperty();
            dprop.setComment("create topic 1");
            Topic topic1 = ((AQjmsSession) tsess).createTopic(table1, sourcetopicname, dprop);
            System.out.println("Creating queue prop_queue...");
            dprop.setComment("create Queue 1");
            Queue queue1 = ((AQjmsSession) qsess).createQueue(table2, destinationqueuename, dprop);
            ((AQjmsDestination) topic1).start(tsess, true, true);
            ((AQjmsDestination) queue1).start(qsess, true, true);
            System.out.println("Successfully setup topic and queue");
        } catch (Exception ex) {
            System.out.println("Error in setupTopic: " + ex);
            throw ex;
        }

    }


    public static void performJmsOperations(
            TopicSession tsess, QueueSession qsess,
            String sourcetopicuser, String destinationqueueuser,
            String sourcetopicname, String destinationqueuename,
            String linkName)
            throws Exception {
        AQjmsConsumer[] subs;
        try {
            System.out.println("Setup topic/source and queue/destination for propagation...");
            Topic topic1 = ((AQjmsSession) tsess).getTopic(sourcetopicuser, sourcetopicname);
            Queue queue1 = ((AQjmsSession) qsess).getQueue(destinationqueueuser, destinationqueuename);
            System.out.println("Creating Topic Subscribers...");
            subs = new AQjmsConsumer[3];
            subs[0] = (AQjmsConsumer) (tsess).createDurableSubscriber(
                    topic1, "PROG1", null, false);
            subs[1] = (AQjmsConsumer) (tsess).createDurableSubscriber(
                    topic1, "PROG2", "JMSPriority > 2", false);
            subs[2] = (AQjmsConsumer) qsess.createConsumer(queue1);
            AQjmsAgent agt = new AQjmsAgent("", destinationqueuename + "@" + linkName);
            ((AQjmsSession) tsess).createRemoteSubscriber( topic1, agt, "JMSPriority = 2");
            ((AQjmsDestination) topic1).schedulePropagation(
                    tsess, linkName, null, null, null, new Double(0));


            sendMessages(tsess, topic1);
            Thread.sleep(50000);
            receiveMessages(tsess, qsess, subs);
            ((AQjmsDestination) topic1).unschedulePropagation(tsess, linkName);
        } catch (Exception e) {
            System.out.println("Error in performJmsOperations: " + e);
            throw e;
        }
    }

    private static void sendMessages(TopicSession tsess, Topic topic1) throws JMSException {
        System.out.println("Publish messages...");
        TextMessage objmsg = tsess.createTextMessage();
        TopicPublisher publisher = tsess.createPublisher(topic1);
        objmsg.setIntProperty("Id", 101);
        objmsg.setStringProperty("City", "Philadelphia");
        objmsg.setIntProperty("Priority", 3);
        objmsg.setText("test message text");
        objmsg.setJMSCorrelationID("correlationid101");
        objmsg.setJMSPriority(3);
        publisher.publish(topic1, objmsg, DeliveryMode.PERSISTENT,3, AQjmsConstants.EXPIRATION_NEVER);
        System.out.println("Commit now and sleep...");
        tsess.commit();
    }

    private static void receiveMessages(TopicSession tsess, QueueSession qsess, AQjmsConsumer[] subs) throws JMSException {
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
                    tsess.commit();
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
//        https://objectstorage.us-phoenix-1.oraclecloud.com/p/xW-H_hj4mIiFliIxccoFWcZcJZL0uFcZZ8mW8dZyVug/n/stevengreenberginc/b/inventorypdb/o/cwallet.sso
//        pre-authenticated request to orderpdb bucket wallet.sso
//        https://objectstorage.us-phoenix-1.oraclecloud.com/p/U3KJ_dZsNeF8Yb_muUvxJUyMudpfvKZdbC7DsugzJsQ/n/stevengreenberginc/b/orderpdb/o/cwallet.sso
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

