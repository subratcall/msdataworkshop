package oracle.db.microservices;

import oracle.AQ.*;
import oracle.jms.*;
import javax.jms.*;
//import java.lang.*;
import java.util.Properties;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;

public class PropagationSetup {
    public static void main (String args [])
            throws java.sql.SQLException, ClassNotFoundException, JMSException
    {
        Console console = System.console();

        console.printf("Enter Jms  User: ");
        String username = console.readLine();
        console.printf("Enter Jms user  password: ");
        char[] passwordChars = console.readPassword();
        String password = new String(passwordChars);


        TopicSession  tsess = null;
        TopicConnectionFactory tcfact=null;
        TopicConnection tconn=null;
        QueueConnectionFactory qcfact = null;
        QueueConnection qconn = null;
        QueueSession qsess = null;

        try
        {
            String myjdbcURL = System.getProperty("JDBC_URL");
            myjdbcURL = "jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS=(PROTOCOL=tcp)(PORT=1521)(HOST=den02tgo))(CONNECT_DATA=(SERVICE_NAME=cdb1_pdb1.regress.rdbms.dev.us.oracle.com)))";
            String myjdbcURL_remote = "jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS=(PROTOCOL=tcp)(PORT=1521)(HOST=den02tgo))(CONNECT_DATA=(SERVICE_NAME=cdb1_pdb2.regress.rdbms.dev.us.oracle.com)))";
            if (myjdbcURL == null )
                System.out.println("The system property JDBC_URL has not been set, Usage:java -DJDBC_URL=xxx filename ");
            else {
                Properties myProperties = new Properties();
                myProperties.put("user", username);
                myProperties.put("password", password);

                tcfact = AQjmsFactory.getTopicConnectionFactory(myjdbcURL, myProperties);
                tconn = tcfact.createTopicConnection( username,password);

                qcfact = AQjmsFactory.getQueueConnectionFactory(myjdbcURL_remote, myProperties);
                qconn = qcfact.createQueueConnection(username,password);

                /* Create a Topic Session */
                tsess = tconn.createTopicSession(true, Session.CLIENT_ACKNOWLEDGE);
                qsess = qconn.createQueueSession(true, Session.CLIENT_ACKNOWLEDGE);

                tconn.start() ;
                qconn.start();
                setupTopicQueue(tsess,qsess,username) ;
                performJmsOperations(tsess,qsess,username);
                tsess.close();
                tconn.close();
                qsess.close();
                qconn.close();
                System.out.println("End of Demo") ;
            }
        }
        catch (Exception ex)
        {
            System.out.println("Exception-1: " + ex);
            ex.printStackTrace();
        }
    }

    public static void setupTopicQueue(TopicSession tsess,QueueSession qsess, String user) throws Exception
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
                qtable=((AQjmsSession)tsess).getQueueTable(user, "INPUT_QUEUE_TABLE" );
                qtable.drop(true);
            } catch (Exception e) {} ;
            try {
                qtable=((AQjmsSession)qsess).getQueueTable(user, "PROP_QUEUE_TABLE" );
                qtable.drop(true);
            } catch (Exception e) { System.out.println("Exception in droppign prop_queue_table " + e); e.printStackTrace();} ;

            qtprop1 = new AQQueueTableProperty ("SYS.AQ$_JMS_TEXT_MESSAGE") ;
            qtprop1.setComment("input queue") ;
            qtprop1.setMultiConsumer(true) ;
            qtprop1.setCompatible("8.1") ;
            qtprop1.setPayloadType("SYS.AQ$_JMS_TEXT_MESSAGE") ;
            table1 = ((AQjmsSession)tsess).createQueueTable(user, "INPUT_QUEUE_TABLE", qtprop1) ;

            System.out.println("Creating Propagation Queue Table...") ;
            qtprop2 = new AQQueueTableProperty ("SYS.AQ$_JMS_TEXT_MESSAGE") ;
            qtprop2.setComment("Popagation queue") ;
            qtprop2.setPayloadType("SYS.AQ$_JMS_TEXT_MESSAGE") ;
            qtprop2.setMultiConsumer(false) ;
            qtprop2.setCompatible("8.1") ;
            table2 = ((AQjmsSession)qsess).createQueueTable(user, "PROP_QUEUE_TABLE", qtprop2) ;

            System.out.println ("Creating Topic input_queue...");
            dprop = new AQjmsDestinationProperty() ;
            dprop.setComment("create topic 1") ;
            topic1=((AQjmsSession)tsess).createTopic(table1,"INPUT_QUEUE",dprop) ;

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


    public static void performJmsOperations(TopicSession tsess, QueueSession qsess, String user)
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
            topic1 = ((AQjmsSession)tsess).getTopic(user, "INPUT_QUEUE") ;

            queue1 = ((AQjmsSession)qsess).getQueue(user, "PROP_QUEUE") ;

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
    } /* end of demo06 */
}

