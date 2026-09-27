package com.bookstore.inventory.model;

import java.util.List;

public class InventorySummary {

    private final int totalItems;
    private final int totalUnits;
    private final double totalValue;
    private final long needReorderCount;
    private final long outOfStockCount;
    private final long expiredLicenseCount;

    public InventorySummary(List<InventoryItem> items) {
        this.totalItems = items.size();
        this.totalUnits = items.stream().mapToInt(InventoryItem::getQuantity).sum();
        // Polymorphic call: each item calculates its own value
        this.totalValue = items.stream().mapToDouble(InventoryItem::calculateStockValue).sum();
        this.needReorderCount = items.stream().filter(InventoryItem::needsReorder).count();
        this.outOfStockCount = items.stream()
                .filter(i -> i.getStockStatus() == StockStatus.OUT_OF_STOCK).count();
        this.expiredLicenseCount = items.stream()
                .filter(i -> i.getStockStatus() == StockStatus.LICENSE_EXPIRED).count();
    }

    public int getTotalItems() { return totalItems; }
    public int getTotalUnits() { return totalUnits; }
    public double getTotalValue() { return totalValue; }
    public long getNeedReorderCount() { return needReorderCount; }
    public long getOutOfStockCount() { return outOfStockCount; }
    public long getExpiredLicenseCount() { return expiredLicenseCount; }
}
