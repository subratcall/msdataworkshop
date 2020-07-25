package io.helidon.data.examples;

import javax.json.bind.annotation.JsonbProperty;

public class Order {
    private String orderid;
    private String itemid;
    private String deliverylocation;
    @JsonbProperty(nillable = true)
    private String status;
    @JsonbProperty(nillable = true)
    private String inventoryLocation;
    @JsonbProperty(nillable = true)
    private String suggestiveSale;

    public Order() {
    }

    // orderdetail is the cache object and order is the JSON message and DB object so we have this mapping at least for now...
    public Order(OrderDetail orderDetail) {
        this(orderDetail.getOrderId(), orderDetail.getItemId(), orderDetail.getDeliveryLocation(),
                orderDetail.getOrderStatus(), orderDetail.getInventoryLocation(), orderDetail.getSuggestiveSale());
    }

    public Order(String orderId, String itemId, String deliverylocation,
                 String status, String inventoryLocation, String suggestiveSale) {
        this.orderid = orderId;
        this.itemid = itemId;
        this.deliverylocation = deliverylocation;
        this.status = status;
        this.inventoryLocation = inventoryLocation;
        this.suggestiveSale = suggestiveSale;
    }

    public String getOrderid() {
        return orderid;
    }

    public String getItemid() {
        return itemid;
    }

    public String getDeliverylocation() {
        return deliverylocation;
    }

    public String getStatus() {
        return status;
    }

    public String getInventoryLocation() {
        return inventoryLocation;
    }

    public String getSuggestiveSale() {
        return suggestiveSale;
    }

    public String toString() {
        String returnString = "";
        returnString+="<br> orderId = " + orderid;
        returnString+="<br> itemid = " + itemid;
        returnString+="<br>  suggestiveSale = " + suggestiveSale;
        returnString+="<br>  inventoryLocation = " + inventoryLocation;
        returnString+="<br>  orderStatus = " + status;
        returnString+="<br>  deliveryLocation = " + deliverylocation;
        return returnString;
    }
}
