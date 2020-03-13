package io.helidon.data.examples;

import oracle.jms.AQjmsConsumer;
import oracle.jms.AQjmsFactory;
import oracle.jms.AQjmsSession;

import javax.jms.*;
import javax.sql.DataSource;

public class OrderServiceEventConsumer implements Runnable {

    DataSource dataSource;
    String orderidToListenFor;

    @Override
    public void run() {
        dolistenForMessages();
    }

    public Object dolistenForMessages() {
        QueueSession session;
        try {
            System.out.println("listenForMessages... " + "dataSource:" + dataSource + " queueOwner:" + OrderResource.orderQueueOwner +
                    " queueName:" + OrderResource.inventoryQueueName);
            QueueConnectionFactory q_cf = AQjmsFactory.getQueueConnectionFactory(dataSource);
            QueueConnection queueConnectionconnection = q_cf.createQueueConnection();
            session = queueConnectionconnection.createQueueSession(true, Session.CLIENT_ACKNOWLEDGE);
            queueConnectionconnection.start();
            Queue queue = ((AQjmsSession) session).getQueue(OrderResource.orderQueueOwner, OrderResource.inventoryQueueName);
            receiveMessages(session,  queue);
        } catch (Exception e) {
            System.out.println("Error in performJmsOperations: " + e);
            return "exception:"+e.toString();
        }
        return "success";
    }


    private void receiveMessages(QueueSession qsess, Queue queue) throws JMSException {
        AQjmsConsumer sub = (AQjmsConsumer) qsess.createConsumer(queue);
        boolean done = false;
        while (!done) {
            try {
                TextMessage textMessage = (TextMessage) (sub.receiveNoWait());
                if (textMessage != null) {
                    String txt = textMessage.getText();
                    System.out.println("txt " + txt);
                    System.out.print(" Pri: " + textMessage.getJMSPriority());
                    System.out.print(" Message: " + textMessage.getIntProperty("Id"));
                    String orderid = textMessage.getStringProperty("orderid");
                    System.out.print(" orderid:" + orderid);
                    String itemid = textMessage.getStringProperty("itemid");
                    System.out.print(" itemid:" + itemid);
                    String inventorylocation = textMessage.getStringProperty("inventorylocation");
                    System.out.print(" inventorylocation:" + inventorylocation);
                    System.out.println(" " + textMessage.getIntProperty("Priority"));
                    System.out.println("((AQjmsSession) qsess).getDBConnection(): " + ((AQjmsSession) qsess).getDBConnection());
                } else {
                    //  done = true;
                }
                qsess.commit();
                Thread.sleep(500);
            } catch (Exception e) {
                System.out.println("Error in performJmsOperations: " + e);
                done = true;
            }
        }
    }


    public Object dolistenForMessages0(DataSource dataSource, String orderidToListenFor) {
        QueueConnection connection = null;
        javax.jms.Queue queue;
        QueueSession session = null;
        String queueOwner = OrderResource.orderQueueOwner;
        String queueName = OrderResource.inventoryQueueName;
        Message msg;
        try {
            System.out.println("listenForMessages... " + "dataSource:" + dataSource + " queueOwner:" + queueOwner +
                    " queueName:" + queueName);
            QueueConnectionFactory q_cf = AQjmsFactory.getQueueConnectionFactory(dataSource);
            connection = q_cf.createQueueConnection();
            session = connection.createQueueSession(true, Session.CLIENT_ACKNOWLEDGE);
            queue = ((AQjmsSession) session).getQueue(queueOwner, queueName);
            java.sql.Connection dbConnection = ((AQjmsSession) session).getDBConnection();
            System.out.println("listenForMessages dbConnection:" + dbConnection);
//            Reason: Since the message of type AQ$_JMS* is produced/enqueued by a
//            Non-JMS client (PL/SQL client in this case), session.createReceiver(queue) is
//            the appropriate call to create a consumer.
//            session.createConsumer(queue) is used when message is produced by JMS client.
//            MessageConsumer consumer = session.createConsumer(queue);
            MessageConsumer consumer = session.createReceiver(queue);
            //todo add selector for orderidToListenFor and action
            connection.start();
            System.out.println("listenForMessages before receive queue:" + queue);
            msg = consumer.receive();
            System.out.println("listenForMessages message:" + msg);
            TextMessage message = (TextMessage) msg;
            String messageTxt = message.getText();
            System.out.println("listenForMessages message (null may be expected):" + messageTxt);
            String action = message.getStringProperty("action");
            System.out.println("listenForMessages message action:" + action);
            int orderid = message.getIntProperty("orderid");
            System.out.println("listenForMessages message orderid:" + orderid);

            session.commit();
            session.close();
            connection.close();
            return action;
        } catch (Exception e) {
            e.printStackTrace();
            try {
                if (session != null) session.rollback();
            } catch (JMSException e1) {
                e1.printStackTrace();
            }
        } finally {
            try {
                if (session != null) {
                    session.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}
