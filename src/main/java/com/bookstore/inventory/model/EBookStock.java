package com.bookstore.inventory.model;

import java.time.LocalDate;


public class EBookStock extends InventoryItem {

    public static final String TYPE = "EBOOK";
    private static final int EXPIRY_WARNING_DAYS = 30;

    private String platform;
    private LocalDate licenseExpiry;

    public EBookStock(String itemId, String isbn, String bookTitle, int quantity, int reorderLevel,
                      double unitCost, LocalDate lastUpdated, String platform, LocalDate licenseExpiry) {
        super(itemId, isbn, bookTitle, quantity, reorderLevel, unitCost, lastUpdated);
        this.platform = platform;
        setLicenseExpiry(licenseExpiry);
    }

    public boolean isLicenseExpired() {
        return licenseExpiry.isBefore(LocalDate.now());
    }

    public boolean isLicenseExpiringSoon() {
        return !isLicenseExpired() && !licenseExpiry.isAfter(LocalDate.now().plusDays(EXPIRY_WARNING_DAYS));
    }

    @Override
    public String getItemType() {
        return TYPE;
    }

    @Override
    public String getTypeLabel() {
        return "E-book";
    }

    @Override
    public StockStatus getStockStatus() {
        if (isLicenseExpired()) {
            return StockStatus.LICENSE_EXPIRED;
        }
        return super.getStockStatus();
    }

    /** Expired licenses can't be sold, so they're worth nothing. */
    @Override
    public double calculateStockValue() {
        return isLicenseExpired() ? 0 : getQuantity() * getUnitCost();
    }

    /** Exact number of licenses; if expired, renew the full target amount. */
    @Override
    public int getSuggestedReorderQuantity() {
        if (!needsReorder()) {
            return 0;
        }
        int target = Math.max(getReorderLevel() * 2, 1);
        int usable = isLicenseExpired() ? 0 : getQuantity();
        return Math.max(target - usable, 0);
    }

    @Override
    public void deduct(int amount) {
        if (isLicenseExpired()) {
            throw new IllegalStateException("The license for \"" + getBookTitle()
                    + "\" expired on " + licenseExpiry + ". Renew it before removing licenses.");
        }
        super.deduct(amount);
    }

    @Override
    public String getStorageDetails() {
        return platform + ", license valid until " + licenseExpiry;
    }

    @Override
    protected String typeSpecificFileFields() {
        return clean(platform) + DELIMITER + licenseExpiry;
    }

    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }

    public LocalDate getLicenseExpiry() { return licenseExpiry; }

    public void setLicenseExpiry(LocalDate licenseExpiry) {
        if (licenseExpiry == null) {
            throw new IllegalArgumentException("License expiry date is required for e-books.");
        }
        this.licenseExpiry = licenseExpiry;
    }
}
