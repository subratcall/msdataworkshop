package io.helidon.data.examples;

public class Inventory {

    String orderid;
    String itemid;
    String inventorylocation;
    String suggestiveSale;

    public Inventory(String orderid, String itemid, String inventorylocation, String suggestiveSale) {
        this.orderid = orderid;
        this.itemid = itemid;
        this.inventorylocation = inventorylocation;
        this.suggestiveSale = suggestiveSale;
    }
}
