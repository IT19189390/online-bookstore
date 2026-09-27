package com.bookstore.inventory.model;

import java.time.LocalDate;

public class PrintedBookStock extends InventoryItem {

    public static final String TYPE = "PRINTED";
    private static final int CARTON_SIZE = 10;

    private String warehouse;
    private String shelfCode;

    public PrintedBookStock(String itemId, String isbn, String bookTitle, int quantity, int reorderLevel,
                            double unitCost, LocalDate lastUpdated, String warehouse, String shelfCode) {
        super(itemId, isbn, bookTitle, quantity, reorderLevel, unitCost, lastUpdated);
        this.warehouse = warehouse;
        this.shelfCode = shelfCode;
    }

    @Override
    public String getItemType() {
        return TYPE;
    }

    @Override
    public String getTypeLabel() {
        return "Printed book";
    }

    @Override
    public double calculateStockValue() {
        return getQuantity() * getUnitCost();
    }

    @Override
    public int getSuggestedReorderQuantity() {
        if (!needsReorder()) {
            return 0;
        }
        int shortfall = Math.max(getReorderLevel() * 2, 1) - getQuantity();
        if (shortfall <= 0) {
            return 0;
        }
        return ((shortfall + CARTON_SIZE - 1) / CARTON_SIZE) * CARTON_SIZE;
    }

    @Override
    public String getStorageDetails() {
        return warehouse + ", shelf " + shelfCode;
    }

    @Override
    protected String typeSpecificFileFields() {
        return clean(warehouse) + DELIMITER + clean(shelfCode);
    }

    public String getWarehouse() { return warehouse; }
    public void setWarehouse(String warehouse) { this.warehouse = warehouse; }

    public String getShelfCode() { return shelfCode; }
    public void setShelfCode(String shelfCode) { this.shelfCode = shelfCode; }
}
