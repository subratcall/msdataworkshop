package io.helidon.data.examples;

import oracle.jms.AQjmsConsumer;
import oracle.jms.AQjmsFactory;
import oracle.jms.AQjmsSession;

import javax.jms.*;
import javax.sql.DataSource;

public class OrderServiceEventConsumer implements Runnable {

    OrderResource orderResource;

    public OrderServiceEventConsumer(OrderResource orderResource) {
        this.orderResource = orderResource;
    }

    @Override
    public void run() {
        dolistenForMessages();
    }

    public Object dolistenForMessages() {
        System.out.println("OrderServiceEventConsumer.dolistenForMessages");
        QueueSession session;
        try {
            DataSource dataSource = orderResource.atpOrderPdb;
            QueueConnectionFactory q_cf = AQjmsFactory.getQueueConnectionFactory(dataSource);
            QueueConnection queueConnectionconnection = q_cf.createQueueConnection();
            session = queueConnectionconnection.createQueueSession(true, Session.CLIENT_ACKNOWLEDGE);
            queueConnectionconnection.start();
            Queue queue = ((AQjmsSession) session).getQueue(OrderResource.orderQueueOwner, OrderResource.inventoryQueueName);
            System.out.println("listenForMessages... " + "dataSource:" + dataSource + " queueOwner:" + OrderResource.orderQueueOwner +
                    " queueName:" + OrderResource.inventoryQueueName + " queue:" + queue);
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
                    String messageText = textMessage.getText();
                    System.out.println("messageText " + messageText);
                    System.out.println("Priority: " + textMessage.getIntProperty("Priority"));
                    System.out.print(" Pri: " + textMessage.getJMSPriority());
                    System.out.print(" Message: " + textMessage.getIntProperty("Id"));
                    Inventory inventory = JsonUtils.read(messageText, Inventory.class);
                    String orderid = inventory.getOrderid();
                    String itemid = inventory.getItemid();
                    String inventorylocation = inventory.getInventorylocation();
                    OrderDetail orderDetail = orderResource.cachedOrders.get(orderid);
                    System.out.println("Lookup orderid:" + orderid + " orderDetail:" + orderDetail + " itemid:" + itemid + " inventorylocation:" + inventorylocation);
                    if(orderDetail == null){
                        throw new JMSException("Rollingback message as no orderDetail found for orderid:" + orderid +
                                ". It may have been started by another server (eg if horizontally scaling) or " +
                                " this server started the order but crashed. "); //todo add "hydration"/caching of orders during init of service to avoid this
                    }
                    else{
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
                        orderResource.orderServiceEventProducer.updateOrderViaSODA(updatedOrder, ((AQjmsSession) qsess).getDBConnection());
                    }
                    System.out.println("((AQjmsSession) qsess).getDBConnection(): " + ((AQjmsSession) qsess).getDBConnection());
                } else {
                    Thread.sleep(500);
                }
                qsess.commit();
            } catch (Exception e) {
                System.out.println("Error in performJmsOperations: " + e);
                done = true;
                qsess.rollback();
            }
        }
    }


}
