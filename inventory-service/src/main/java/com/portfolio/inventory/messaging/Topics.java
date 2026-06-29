package com.portfolio.inventory.messaging;

public final class Topics {

    public static final String ORDER_CREATED = "order.created";
    public static final String INVENTORY_RESERVED = "inventory.reserved";
    public static final String INVENTORY_REJECTED = "inventory.rejected";

    private Topics() {
    }
}
