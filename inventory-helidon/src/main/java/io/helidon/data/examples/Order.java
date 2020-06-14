package io.helidon.data.examples;

public class Order {
    private String orderid;
    private String itemid;
    private String deliverylocation;
    private String suggestivesaleitem;
    private String status;

    public Order(String orderId, String itemId, String deliverylocation) {
        this.orderid = orderId;
        this.itemid = itemId;
        this.deliverylocation = deliverylocation;
    }

    public String getOrderid() {
        return orderid;
    }

    public String getItemid() {
        return itemid;
    }
}
