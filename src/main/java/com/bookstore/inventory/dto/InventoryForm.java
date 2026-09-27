package com.bookstore.inventory.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;


public class InventoryForm {

    private String itemType;

    @NotBlank(message = "Enter the ISBN.")
    @Pattern(regexp = "^[0-9Xx-]{10,17}$", message = "Use digits and hyphens only, for example 978-0-13-468599-1.")
    private String isbn;

    @NotBlank(message = "Enter the book title.")
    @Size(max = 150, message = "Keep the title under 150 characters.")
    private String bookTitle;

    @NotNull(message = "Enter the quantity.")
    @Min(value = 0, message = "Quantity can't be negative.")
    @Max(value = 100000, message = "Quantity can't be more than 100,000.")
    private Integer quantity;

    @NotNull(message = "Enter the reorder level.")
    @Min(value = 0, message = "Reorder level can't be negative.")
    @Max(value = 10000, message = "Reorder level can't be more than 10,000.")
    private Integer reorderLevel;

    @NotNull(message = "Enter the unit cost.")
    @PositiveOrZero(message = "Unit cost can't be negative.")
    private Double unitCost;

    // Printed book only
    @Size(max = 40, message = "Keep the warehouse name under 40 characters.")
    private String warehouse;

    @Size(max = 20, message = "Keep the shelf code under 20 characters.")
    private String shelfCode;

    // E-book only
    @Size(max = 40, message = "Keep the platform name under 40 characters.")
    private String platform;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate licenseExpiry;

    public String getItemType() { return itemType; }
    public void setItemType(String itemType) { this.itemType = itemType; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Integer getReorderLevel() { return reorderLevel; }
    public void setReorderLevel(Integer reorderLevel) { this.reorderLevel = reorderLevel; }

    public Double getUnitCost() { return unitCost; }
    public void setUnitCost(Double unitCost) { this.unitCost = unitCost; }

    public String getWarehouse() { return warehouse; }
    public void setWarehouse(String warehouse) { this.warehouse = warehouse; }

    public String getShelfCode() { return shelfCode; }
    public void setShelfCode(String shelfCode) { this.shelfCode = shelfCode; }

    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }

    public LocalDate getLicenseExpiry() { return licenseExpiry; }
    public void setLicenseExpiry(LocalDate licenseExpiry) { this.licenseExpiry = licenseExpiry; }
}
