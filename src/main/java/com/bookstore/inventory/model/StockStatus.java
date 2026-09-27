package com.bookstore.inventory.model;

public enum StockStatus {

    IN_STOCK("In stock", "ok"),
    LOW_STOCK("Low stock", "low"),
    OUT_OF_STOCK("Out of stock", "out"),
    LICENSE_EXPIRED("License expired", "expired");

    private final String label;
    private final String cssClass;

    StockStatus(String label, String cssClass) {
        this.label = label;
        this.cssClass = cssClass;
    }

    public String getLabel() { return label; }
    public String getCssClass() { return cssClass; }
}
