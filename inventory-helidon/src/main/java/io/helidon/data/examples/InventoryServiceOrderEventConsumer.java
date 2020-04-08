package io.helidon.data.examples;

import oracle.jms.*;

import javax.jms.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class InventoryServiceOrderEventConsumer implements Runnable {

    InventoryResource inventoryResource;

    public InventoryServiceOrderEventConsumer(InventoryResource inventoryResource) {
        this.inventoryResource = inventoryResource;
    }

    @Override
    public void run() {
        System.out.println("Receive Messages...");
            try {
                listenForOrderEvents();
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    public void listenForOrderEvents() throws Exception {
        QueueConnectionFactory qcfact = AQjmsFactory.getQueueConnectionFactory(inventoryResource.atpInventoryPDB);
        QueueConnection qconn = qcfact.createQueueConnection(inventoryResource.inventoryuser, inventoryResource.inventorypw);
        QueueSession qsess = qconn.createQueueSession(true, Session.CLIENT_ACKNOWLEDGE);
        qconn.start();
        receiveMessages(qsess);
    }

    private void receiveMessages(QueueSession qsess) throws JMSException {
        Queue queue = ((AQjmsSession) qsess).getQueue(inventoryResource.inventoryuser, inventoryResource.orderQueueName);
        AQjmsConsumer sub = (AQjmsConsumer) qsess.createConsumer(queue);
        boolean done = false;
        while (!done) {
            try {
                TextMessage robjmsg = (TextMessage) (sub.receiveNoWait());
                if (robjmsg != null) {
                    String txt = robjmsg.getText();
                    System.out.println("txt " + txt);
                    System.out.print(" Pri: " + robjmsg.getJMSPriority());
                    System.out.print(" Message: " + robjmsg.getIntProperty("Id"));
                    String orderid = robjmsg.getStringProperty("orderid");
                    System.out.print(" orderid:" + orderid);
                    String itemid = robjmsg.getStringProperty("itemid");
                    System.out.print(" itemid:" + itemid);
                    System.out.println(" " + robjmsg.getIntProperty("Priority"));
                    System.out.println("((AQjmsSession) qsess).getDBConnection(): " + ((AQjmsSession) qsess).getDBConnection());
                    updateDataAndSendEventOnInventory((AQjmsSession) qsess, orderid, itemid);
                } else {
                  //  done = true;
                }
                Thread.sleep(500);
            } catch (Exception e) {
                System.out.println("Error in performJmsOperations: " + e);
                qsess.rollback();
                done = true;
            }
        }
    }

    private void updateDataAndSendEventOnInventory(AQjmsSession session, String orderid, String itemid) throws Exception {
//        String inventorylocation = evaluateInventory(session, itemid);
        String inventorylocation = "Philadelphia"; //todo get from evaluateInventory
        String jsonString = "{ \"orderid\" : \"" + orderid + "\", \"item\" : " + itemid +
                "\", \"inventoryLocation\" : " + inventorylocation + " }";
        Topic topic =  session.getTopic(InventoryResource.inventoryuser, InventoryResource.inventoryQueueName);
        System.out.println("send inventory status message... jsonString:" + jsonString + " topic:" + topic) ;
        TextMessage objmsg = session.createTextMessage();
        TopicPublisher publisher = session.createPublisher(topic);
        objmsg.setIntProperty("Id", 1);
        objmsg.setStringProperty("orderid", orderid);
        objmsg.setStringProperty("itemid", itemid);
        objmsg.setStringProperty("inventorylocation", inventorylocation);
        objmsg.setStringProperty("suggestiveSale", "lettuce"); //static suggestiveSale
        objmsg.setIntProperty("Priority", 2);
        objmsg.setText(jsonString);
        objmsg.setJMSCorrelationID("" + 2);
        objmsg.setJMSPriority(2);
        publisher.publish(topic, objmsg, DeliveryMode.PERSISTENT,2, AQjmsConstants.EXPIRATION_NEVER);
        session.commit();
        System.out.println("message sent");
    }

    //returns location if exists and "inventorydoesnotexist" otherwise
    private String evaluateInventory(AQjmsSession session, String itemid) throws JMSException, SQLException {
        Connection dbConnection = session.getDBConnection();
        System.out.println("sendMessage dbConnection:" + dbConnection);
        System.out.println("-------------->MessagingService.doIncomingOutgoing connection:" + dbConnection +
                "Session:" + session + " check inventory for inventoryid:" + itemid);
        int inventorycount;
        String inventoryLocation = "";
        // todo this should be increment decrement and handle violation rather than select update...
        ResultSet resultSet = dbConnection.createStatement().executeQuery(
                "select * from inventory  where inventoryid = '" + itemid + "'");
        if (resultSet.next()) {
            inventorycount = resultSet.getInt("inventorycount");
            inventoryLocation = resultSet.getString("inventorylocation");
            System.out.println("MessagingService.doIncomingOutgoing inventorycount:" + inventorycount);
        } else inventorycount = 0;
        String status = inventorycount > 0 ? "inventoryexists" : "inventorydoesnotexist";
        inventoryLocation = inventorycount > 0 ? inventoryLocation : "inventorydoesnotexist";
        System.out.println("InventoryServiceOrderEventConsumer.updateDataAndSendEventOnInventory status:" + status);
        return inventoryLocation;
    }


}
