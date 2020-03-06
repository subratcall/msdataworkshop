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
    String orderdb_tnsname = "db202002011726_high";
    String inventorydb_tnsname = "inventorydb_high";


    public String createUsers(DataSource orderpdbDataSource, DataSource inventorypdbDataSource) throws SQLException  {
        String returnValue = "";
        returnValue += createAQUser(orderpdbDataSource, topicuser, topicpw);
        returnValue += createAQUser(inventorypdbDataSource, queueuser, queuepw);
        return returnValue;
    }


    Object createAQUser(DataSource ds, String queueOwner, String queueOwnerPW) throws SQLException {
        String outputString = "\nPropagationSetup.createAQUser ds = [" + ds + "], queueOwner = [" + queueOwner + "], queueOwnerPW = [" + queueOwnerPW + "]";
        System.out.println(outputString);
        Connection sysDBAConnection = ds.getConnection();
//        sysDBAConnection.createStatement().execute("grant pdb_dba to " + queueOwner + " identified by " + queueOwnerPW);
        sysDBAConnection.createStatement().execute("GRANT EXECUTE ON DBMS_CLOUD_ADMIN TO " + queueOwner );
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

    public String createDBLinks(DataSource orderpdbDataSource, DataSource inventorypdbDataSource) throws SQLException  {
        String outputString = "\ncreateDBLinks...";
        outputString += createDBLink(orderpdbDataSource, topicuser, topicpw, queueuser, queuepw, inventorydb_tnsname, orderToInventoryLinkName);
        outputString += createDBLink(inventorypdbDataSource, queueuser, queuepw, topicuser, topicpw, orderdb_tnsname, inventoryToOrderLinkName);
        return outputString;
    }

    private String createDBLink(DataSource dataSource,
                              String fromuser, String frompw,
                              String touser, String topw, String tnsnamesName, String linkName) throws SQLException  {
        String outputString = "\nPropagationSetup.createDBLink dataSource = [" + dataSource + "], " +
                "fromuser = [" + fromuser + "], frompw = [" + frompw + "], " +
                "touser = [" + touser + "], topw = [" + topw + "], " +
                "tnsnamesName = [" + tnsnamesName + "], linkName = [" + linkName + "]";
        System.out.println(outputString);
        Connection connection = dataSource.getConnection(fromuser, frompw);
        connection.createStatement().execute("BEGIN" +
                "DBMS_CLOUD_ADMIN.CREATE PUBLIC DATABASE LINK " + linkName + " " +
                "CONNECT TO " + touser + " IDENTIFIED BY " + topw + " " +
                "USING '" + tnsnamesName + "'; " +
                "END;");
        return outputString;
    }

    private void createDBLinkLonghand(DataSource dataSource, String user, String pw, String linkName) throws SQLException  {
        String outputString = "\nPropagationSetup.createDBLink dataSource = [" + dataSource + "], " +
                "user = [" + user + "], pw = [" + pw + "], linkName = [" + linkName + "]";
        System.out.println(outputString);
        Connection connection = dataSource.getConnection(user, pw);
        connection.createStatement().execute("BEGIN" +
                "DBMS_CLOUD_ADMIN.CREATE_DATABASE_LINK(" +
                "db_link_name => '" + linkName +"'," +
                "hostname => 'adb.us-phoenix-1.oraclecloud.com'," +
                "port => '1522'," +
                "service_name => 'mnisopbygm56hii_inventorydb_high.atp.oraclecloud.com'," +
                "ssl_server_cert_dn => 'CN=adwc.uscom-east-1.oraclecloud.com,OU=Oracle BMCS US,O=Oracle Corporation,L=Redwood City,ST=California,C=US'," +
                "credential_name => 'DB_LINK_CRED_TEST1'," +
                "directory_name => 'DATA_PUMP_DIR');" +
                "END;");
    }


//    CREATE PUBLIC DATABASE LINK cdb2_remote
//    CONNECT TO aquser IDENTIFIED BY aquser
//    USING 'cdb1_pdb2'; // as orderuser...

// BEGIN
// DBMS_CLOUD.CREATE_CREDENTIAL (
// 'objectstore_cred',
// 'paul.parkinson',
// 'Q:4qWo:7PYDph9KZomZQ');
// END;
// /
//
// # had to make bucket public as paul.parkinson/authtoken wasnt working thus no `credential_name => 'objectstore_cred',` arg
// # this can likely be done by any user as it is shared, I did it with admin
// BEGIN
// DBMS_CLOUD.GET_OBJECT(
// object_uri => 'https://objectstorage.us-phoenix-1.oraclecloud.com/n/stevengreenberginc/b/msdataworkshop_bucket/o/cwallet.sso',
// directory_name => 'DATA_PUMP_DIR');
// END;
// /
//
// BEGIN
// DBMS_CLOUD.CREATE_CREDENTIAL(
// credential_name => 'DB_LINK_CRED_TEST1',
// username => 'INVENTORYUSER',
// password => 'Welcome12345'
// );
// END;
// /
//
// BEGIN
// DBMS_CLOUD_ADMIN.CREATE_DATABASE_LINK(
// db_link_name => 'TESTLINK1',
// hostname => 'adb.us-phoenix-1.oraclecloud.com',
// port => '1522',
// service_name => 'mnisopbygm56hii_inventorydb_high.atp.oraclecloud.com',
// ssl_server_cert_dn => 'CN=adwc.uscom-east-1.oraclecloud.com,OU=Oracle BMCS US,O=Oracle Corporation,L=Redwood City,ST=California,C=US',
// credential_name => 'DB_LINK_CRED_TEST1',
// directory_name => 'DATA_PUMP_DIR');
// END;
// /
//
// SELECT count(*) FROM inventory@TESTLINK1;

    public void setup(DataSource orderpdbDataSource, DataSource inventorypdbDataSource) {

        TopicSession  tsess = null;
        TopicConnectionFactory tcfact=null;
        TopicConnection tconn=null;
        QueueConnectionFactory qcfact = null;
        QueueConnection qconn = null;
        QueueSession qsess = null;

        try
        {
//                tcfact = AQjmsFactory.getTopicConnectionFactory(myjdbcURL, myProperties);
                tcfact = AQjmsFactory.getTopicConnectionFactory(orderpdbDataSource);
                tconn = tcfact.createTopicConnection( topicuser, topicpw);

                qcfact = AQjmsFactory.getQueueConnectionFactory(inventorypdbDataSource);
                qconn = qcfact.createQueueConnection(queueuser, queuepw);

                /* Create a Topic Session */
                tsess = tconn.createTopicSession(true, Session.CLIENT_ACKNOWLEDGE);
            System.out.println("PropagationSetup.setup tsess:" + tsess);
                qsess = qconn.createQueueSession(true, Session.CLIENT_ACKNOWLEDGE);
            System.out.println("PropagationSetup.setup qsess:" + qsess);

                tconn.start() ;
                qconn.start();
                setupTopicQueue(tsess,qsess,topicuser, queueuser) ;
                performJmsOperations(tsess,qsess,topicuser, queueuser);
                tsess.close();
                tconn.close();
                qsess.close();
                qconn.close();
                System.out.println("End of Demo") ;
        }
        catch (Exception ex)
        {
            System.out.println("Exception-1: " + ex);
            ex.printStackTrace();
        }
    }

    public static void setupTopicQueue(TopicSession tsess,QueueSession qsess, String topicuser, String queueuser) throws Exception
    {
        AQQueueTableProperty qtprop1,qtprop2 ;
        AQQueueTable qtable,table1, table2;
        AQjmsDestinationProperty dprop;
        Topic topic1;
        Queue queue1;
        try {
            /* Create Queue Tables */
            System.out.println("Creating Input Queue Table...") ;

            /* Drop the queue if already exists */
            try {
                qtable=((AQjmsSession)tsess).getQueueTable(topicuser, "INPUT_QUEUE_TABLE" );
                qtable.drop(true);
            } catch (Exception e) {} ;
            try {
                qtable=((AQjmsSession)qsess).getQueueTable(queueuser, "PROP_QUEUE_TABLE" );
                qtable.drop(true);
            } catch (Exception e) { System.out.println("Exception in droppign prop_queue_table " + e); e.printStackTrace();} ;

            qtprop1 = new AQQueueTableProperty ("SYS.AQ$_JMS_TEXT_MESSAGE") ;
            qtprop1.setComment("input queue") ;
            qtprop1.setMultiConsumer(true) ;
            qtprop1.setCompatible("8.1") ;
            qtprop1.setPayloadType("SYS.AQ$_JMS_TEXT_MESSAGE") ;
            table1 = ((AQjmsSession)tsess).createQueueTable(topicuser, "INPUT_QUEUE_TABLE", qtprop1) ;

            System.out.println("Creating Propagation Queue Table...") ;
            qtprop2 = new AQQueueTableProperty ("SYS.AQ$_JMS_TEXT_MESSAGE") ;
            qtprop2.setComment("Propagation queue") ;
            qtprop2.setPayloadType("SYS.AQ$_JMS_TEXT_MESSAGE") ;
            qtprop2.setMultiConsumer(false) ;
            qtprop2.setCompatible("8.1") ;
            table2 = ((AQjmsSession)qsess).createQueueTable(queueuser, "PROP_QUEUE_TABLE", qtprop2) ;

            System.out.println ("Creating Topic input_queue...");
            dprop = new AQjmsDestinationProperty() ;
            dprop.setComment("create topic 1") ;
            topic1=((AQjmsSession)tsess).createTopic(table1,"INPUT_QUEUE",dprop) ;

            System.out.println ("Creating queue prop_queue...");
            dprop.setComment("create Queue 1") ;
            queue1 =((AQjmsSession)qsess).createQueue( table2,"PROP_QUEUE",dprop) ;

            /* Start the topic */
            ((AQjmsDestination)topic1).start(tsess, true, true);
            ((AQjmsDestination)queue1).start(qsess, true, true);
            System.out.println("Successfully setup Topics");

        } catch (Exception ex) {
            System.out.println("Error in setupTopic: " + ex);
            throw ex;
        }

    }


    public static void performJmsOperations(TopicSession tsess, QueueSession qsess, String topicuser, String queueuser)
            throws Exception
    {
        Topic topic1;
        Queue queue1;
        AQjmsConsumer[] subs;
        AQjmsAgent agt;
        TextMessage objmsg  = null, robjmsg=null;
        TopicPublisher publisher ;
        Message  sobj , rmsg;
        String[] cities={"BELMONT","REDWOOD SHORES", "SUNNYVALE", "BURLINGAME" };
        try
        {

            System.out.println("Get Topics...") ;
            topic1 = ((AQjmsSession)tsess).getTopic(topicuser, "INPUT_QUEUE") ;

            queue1 = ((AQjmsSession)qsess).getQueue(queueuser, "PROP_QUEUE") ;

            System.out.println("Creating Topic Subscribers...") ;

            subs = new AQjmsConsumer[3] ;
            subs[0] = (AQjmsConsumer)((AQjmsSession)tsess).createDurableSubscriber(
                    topic1, "PROG1", null, false);
            subs[1] = (AQjmsConsumer)((AQjmsSession)tsess).createDurableSubscriber(
                    topic1, "PROG2", "JMSPriority > 2", false);
   /*   subs[2] = tsess.createDurableSubscriber(
                                  topic2, "PROG3", null , false) ;*/

            subs[2] =(AQjmsConsumer) qsess.createConsumer(queue1);


            agt = new AQjmsAgent("", "PROP_QUEUE@cdb2_remote" ) ;

            System.out.println("Creating Remote Subscriber...") ;
            ((AQjmsSession)tsess).createRemoteSubscriber(
                    topic1, agt,"JMSPriority = 2");

            /* Schedule Propagation with latency 0 */
            System.out.println("Schedule Propagation...") ;
            ((AQjmsDestination)topic1).schedulePropagation(
                    tsess, "cdb2_remote", null, null, null, new Double(0)) ;

            System.out.println("Publish messages...") ;
            objmsg = ((AQjmsSession)tsess).createTextMessage() ;

            publisher = tsess.createPublisher(topic1);

            /*publish 100 messages*/

            for ( int i = 1 ; i <= 100 ; i++)
            {
                objmsg.setIntProperty("Id",i) ;
                if ( ( i % 3 ) == 0 )  {
                    objmsg.setStringProperty("City",cities[0]) ;
                }
                else if ((i % 4 ) == 0) {
                    objmsg.setStringProperty("City",cities[1]) ;
                }
                else if (( i % 2) == 0) {
                    objmsg.setStringProperty("City",cities[2]) ;
                }
                else {
                    objmsg.setStringProperty("City",cities[3]) ;
                }

                objmsg.setIntProperty("Priority",(1+ (i%3))) ;

//                sobj = new Message() ;
//                sobj.setId(i) ;
//                sobj.setName("message# "+i) ;
//                sobj.setData(500);
//                objmsg.setText(sobj.getId()+":"+sobj.getName()+":"+sobj.getData()) ;
                objmsg.setText("test message text") ;
                objmsg.setJMSCorrelationID(""+i) ;
                objmsg.setJMSPriority(1+(i%3)) ;
                publisher.publish(topic1,objmsg, DeliveryMode.PERSISTENT,
                        1 +(i%3), AQjmsConstants.EXPIRATION_NEVER);
            }
            System.out.println("Commit now...") ;
            tsess.commit() ;

            Thread.sleep(50000);

            /* Receive messages for each subscriber */
            System.out.println("Receive Messages...") ;
            for (int i=0; i< subs.length ; i++)
            {
                System.out.println ("Messages for subscriber : " +i) ;
                if (subs[i].getMessageSelector() != null)
                {
                    System.out.println("  with selector: " +
                            subs[i].getMessageSelector());
                }

                boolean done = false;
                while(!done) {
                    try {
                        robjmsg = (TextMessage)( subs[i].receiveNoWait() );
                        if (robjmsg != null)
                        {
                            String rTextMsg  = robjmsg.getText();
                            System.out.println("rTextMsg " + rTextMsg);
                            System.out.print(" Pri: " +robjmsg.getJMSPriority()) ;
                            System.out.print(" Message: " +robjmsg.getIntProperty("Id")) ;
                            System.out.print(" " +robjmsg.getStringProperty("City")) ;
                            System.out.println(" " +robjmsg.getIntProperty("Priority")) ;
                        }
                        else {
                            System.out.println ("No more messages.") ;
                            done=true;
                        }
                        tsess.commit();
                        qsess.commit();
                    } catch (Exception e) {
                        System.out.println("Error in performJmsOperations: " + e) ;
                        done=true;
                    }
                } /* while loop*/
            }
            ((AQjmsDestination)topic1).unschedulePropagation(tsess,"cdb2_remote");
        } catch (Exception e) {
            System.out.println("Error in performJmsOperations: " + e) ;
            throw e;
        }
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
                " DROP USER paul CASCADE" +
                " /" +
                "" +
                " GRANT DBA TO paul IDENTIFIED BY paul" +
                " /" +
                "</textarea>" +
                "</form>";
    }

    private String createDBLinkHTML() {
        return "<h2>Create DB Link...</h2>" +
                "<form action=\"execute\" method=\"get\">" +
                "    user <input type=\"text\" name=\"user\" size=\"20\" value=\"\"><br>" +
                "    password <input type=\"text\" name=\"password\" size=\"20\" value=\"\"><br>" +
                "<textarea id=\"w3mission\" rows=\"50\" cols=\"20\">" +
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

}

