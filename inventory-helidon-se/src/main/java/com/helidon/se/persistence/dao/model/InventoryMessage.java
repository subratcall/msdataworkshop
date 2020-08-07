package com.helidon.se.persistence.dao.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class InventoryMessage {

    private String orderid;
    private String itemid;
    private String inventorylocation;
    private String suggestiveSale;

    public InventoryMessage(String orderid, String itemid, String inventorylocation, String suggestiveSale) {
        this.orderid = orderid;
        this.itemid = itemid;
        this.inventorylocation = inventorylocation;
        this.suggestiveSale = suggestiveSale;
    }
}
