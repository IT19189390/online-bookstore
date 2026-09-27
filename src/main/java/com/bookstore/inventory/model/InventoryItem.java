package com.bookstore.inventory.model;

import com.bookstore.common.storage.FileStorable;

import java.time.LocalDate;
import java.util.Locale;


public abstract class InventoryItem implements FileStorable {

    private String itemId;
    private String isbn;
    private String bookTitle;
    private int quantity;
    private int reorderLevel;
    private double unitCost;
    private LocalDate lastUpdated;

    protected InventoryItem(String itemId, String isbn, String bookTitle, int quantity,
                            int reorderLevel, double unitCost, LocalDate lastUpdated) {
        setItemId(itemId);
        setIsbn(isbn);
        setBookTitle(bookTitle);
        setQuantity(quantity);
        setReorderLevel(reorderLevel);
        setUnitCost(unitCost);
        setLastUpdated(lastUpdated == null ? LocalDate.now() : lastUpdated);
    }


    public abstract String getItemType();

    public abstract String getTypeLabel();

    public abstract double calculateStockValue();

    public abstract int getSuggestedReorderQuantity();

    public abstract String getStorageDetails();

    protected abstract String typeSpecificFileFields();


    public StockStatus getStockStatus() {
        if (quantity == 0) {
            return StockStatus.OUT_OF_STOCK;
        }
        if (quantity <= reorderLevel) {
            return StockStatus.LOW_STOCK;
        }
        return StockStatus.IN_STOCK;
    }

    public boolean needsReorder() {
        return getStockStatus() != StockStatus.IN_STOCK;
    }

    public void restock(int amount) {
        requirePositive(amount);
        this.quantity += amount;
        touch();
    }

    public void deduct(int amount) {
        requirePositive(amount);
        if (amount > quantity) {
            throw new IllegalStateException("Only " + quantity + " unit(s) of \"" + bookTitle
                    + "\" are available, so " + amount + " can't be removed.");
        }
        this.quantity -= amount;
        touch();
    }


    @Override
    public final String toFileString() {
        return String.join(DELIMITER,
                getItemType(),
                itemId,
                clean(isbn),
                clean(bookTitle),
                String.valueOf(quantity),
                String.valueOf(reorderLevel),
                String.format(Locale.ROOT, "%.2f", unitCost),
                lastUpdated.toString(),
                typeSpecificFileFields());
    }

    protected void touch() {
        this.lastUpdated = LocalDate.now();
    }

    protected static String clean(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("|", "/").replace("\r", " ").replace("\n", " ").trim();
    }

    private static void requirePositive(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Enter a quantity greater than zero.");
        }
    }


    public String getItemId() { return itemId; }

    public void setItemId(String itemId) {
        if (itemId == null || itemId.isBlank()) {
            throw new IllegalArgumentException("Item ID is required.");
        }
        this.itemId = itemId.trim();
    }

    public String getIsbn() { return isbn; }

    public void setIsbn(String isbn) {
        if (isbn == null || isbn.isBlank()) {
            throw new IllegalArgumentException("ISBN is required.");
        }
        this.isbn = isbn.trim();
    }

    public String getBookTitle() { return bookTitle; }

    public void setBookTitle(String bookTitle) {
        if (bookTitle == null || bookTitle.isBlank()) {
            throw new IllegalArgumentException("Book title is required.");
        }
        this.bookTitle = bookTitle.trim();
    }

    public int getQuantity() { return quantity; }

    public void setQuantity(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity can't be negative.");
        }
        this.quantity = quantity;
    }

    public int getReorderLevel() { return reorderLevel; }

    public void setReorderLevel(int reorderLevel) {
        if (reorderLevel < 0) {
            throw new IllegalArgumentException("Reorder level can't be negative.");
        }
        this.reorderLevel = reorderLevel;
    }

    public double getUnitCost() { return unitCost; }

    public void setUnitCost(double unitCost) {
        if (unitCost < 0) {
            throw new IllegalArgumentException("Unit cost can't be negative.");
        }
        this.unitCost = unitCost;
    }

    public LocalDate getLastUpdated() { return lastUpdated; }

    public void setLastUpdated(LocalDate lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    @Override
    public String toString() {
        return getTypeLabel() + " [" + itemId + "] " + bookTitle + " x" + quantity;
    }
}
