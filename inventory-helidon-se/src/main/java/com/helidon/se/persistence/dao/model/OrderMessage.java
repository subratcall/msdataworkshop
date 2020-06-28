package com.helidon.se.persistence.dao.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class OrderMessage {

    private String orderid;
    private String itemid;
    private String deliverylocation;

    public OrderMessage(String orderid, String itemid, String deliverylocation) {
        this.orderid = orderid;
        this.itemid = itemid;
        this.deliverylocation = deliverylocation;
    }
}
