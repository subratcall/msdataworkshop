package io.helidon.data.examples;

import oracle.jms.AQjmsConsumer;
import oracle.jms.AQjmsFactory;
import oracle.jms.AQjmsSession;

import javax.jms.*;
import java.sql.Connection;

public class OrderServiceEventConsumer implements Runnable {

    OrderResource orderResource;
    Connection dbConnection;

    public OrderServiceEventConsumer(OrderResource orderResource) {
        this.orderResource = orderResource;
    }

    @Override
    public void run() {
        System.out.println("Receive messages...");
        try {
            dolistenForMessages();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public void dolistenForMessages() throws JMSException {
        QueueConnectionFactory q_cf = AQjmsFactory.getQueueConnectionFactory(orderResource.atpOrderPdb);
        QueueSession qsess = null;
        QueueConnection qconn = null;
        AQjmsConsumer consumer = null;
        boolean done = false;
        while (!done) {
            try {
                if (qconn == null || qsess == null || dbConnection == null || dbConnection.isClosed()) {
                    qconn = q_cf.createQueueConnection();
                    qsess = qconn.createQueueSession(true, Session.CLIENT_ACKNOWLEDGE);
                    qconn.start();
                    Queue queue = ((AQjmsSession) qsess).getQueue(OrderResource.orderQueueOwner, OrderResource.inventoryQueueName);
                    consumer = (AQjmsConsumer) qsess.createConsumer(queue);
                }
                TextMessage textMessage = (TextMessage) consumer.receive(-1);
                String messageText = textMessage.getText();
                System.out.println("messageText " + messageText);
//                    System.out.println("Priority: " + textMessage.getIntProperty("Priority"));
                System.out.print(" Pri: " + textMessage.getJMSPriority());
//                    System.out.print(" Message: " + textMessage.getIntProperty("Id"));
                Inventory inventory = JsonUtils.read(messageText, Inventory.class);
                String orderid = inventory.getOrderid();
                String itemid = inventory.getItemid();
                String inventorylocation = inventory.getInventorylocation();
                OrderDetail orderDetail = orderResource.cachedOrders.get(orderid);
                System.out.println("Lookup orderid:" + orderid + " orderDetail:" + orderDetail + " itemid:" + itemid + " inventorylocation:" + inventorylocation);
                if (orderDetail == null) {
                    System.out.println("Order not in cache, querying DB orderid:" + orderid);
                    Order order = orderResource.orderServiceEventProducer.getOrderViaSODA(orderResource.atpOrderPdb, orderid);
                    if (order == null)
                        throw new JMSException("Rollingback message as no orderDetail found for orderid:" + orderid +
                                ". It may have been started by another server (eg if horizontally scaling) or " +
                                " this server started the order but crashed. ");
                }
                boolean isSuccessfulInventoryCheck = !(inventorylocation == null || inventorylocation.equals("")
                        || inventorylocation.equals("inventorydoesnotexist")
                        || inventorylocation.equals("none"));
                if (isSuccessfulInventoryCheck) {
                    orderDetail.setOrderStatus("success inventory exists");
                    orderDetail.setInventoryLocation(inventorylocation);
                    orderDetail.setSuggestiveSale(inventory.getSuggestiveSale());
                } else {
                    orderDetail.setOrderStatus("failed inventory does not exist");
                }
                Order updatedOrder = new Order(orderDetail);
                dbConnection = ((AQjmsSession) qsess).getDBConnection();
                orderResource.orderServiceEventProducer.updateOrderViaSODA(updatedOrder, dbConnection);
                System.out.println("((AQjmsSession) qsess).getDBConnection(): " + ((AQjmsSession) qsess).getDBConnection());
                qsess.commit();
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Exception in receiveMessages: " + e);
                qsess.rollback();
            }
        }
    }
}
