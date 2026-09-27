package com.bookstore.inventory.service;

import com.bookstore.inventory.dto.InventoryForm;
import com.bookstore.inventory.exception.DuplicateStockException;
import com.bookstore.inventory.exception.InventoryNotFoundException;
import com.bookstore.inventory.model.EBookStock;
import com.bookstore.inventory.model.InventoryItem;
import com.bookstore.inventory.model.InventorySummary;
import com.bookstore.inventory.model.PrintedBookStock;
import com.bookstore.inventory.repository.InventoryRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class InventoryService {

    private final InventoryRepository repository;

    public InventoryService(InventoryRepository repository) {
        this.repository = repository;
    }


    public List<InventoryItem> getAll() {
        return repository.findAll().stream()
                .sorted(Comparator.comparing(InventoryItem::getItemId))
                .toList();
    }

    public InventoryItem getById(String itemId) {
        return repository.findById(itemId)
                .orElseThrow(() -> new InventoryNotFoundException(itemId));
    }

    public List<InventoryItem> search(String keyword, String type, String status) {
        String kw = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        String kwIsbn = normalizeIsbn(kw);

        return getAll().stream()
                .filter(i -> kw.isEmpty()
                        || i.getItemId().toLowerCase(Locale.ROOT).contains(kw)
                        || i.getBookTitle().toLowerCase(Locale.ROOT).contains(kw)
                        || (!kwIsbn.isEmpty() && normalizeIsbn(i.getIsbn()).contains(kwIsbn)))
                .filter(i -> isBlank(type) || i.getItemType().equals(type))
                .filter(i -> isBlank(status) || i.getStockStatus().name().equals(status))
                .toList();
    }

    public InventorySummary getSummary() {
        return new InventorySummary(repository.findAll());
    }

    /** Items that are low, out of stock or expired - most urgent first. */
    public List<InventoryItem> getItemsNeedingAttention(int limit) {
        return getAll().stream()
                .filter(InventoryItem::needsReorder)
                .sorted(Comparator.comparingInt(InventoryItem::getQuantity))
                .limit(limit)
                .toList();
    }


    public InventoryItem create(InventoryForm form) {
        ensureNoDuplicate(form.getIsbn(), form.getItemType(), null);
        InventoryItem item = buildItem(repository.nextId(), form);
        repository.save(item);
        return item;
    }

    // ---------- UPDATE ----------

    public InventoryItem update(String itemId, InventoryForm form) {
        InventoryItem existing = getById(itemId);
        form.setItemType(existing.getItemType()); // type can't change after creation
        ensureNoDuplicate(form.getIsbn(), form.getItemType(), existing.getItemId());
        InventoryItem updated = buildItem(existing.getItemId(), form);
        repository.save(updated);
        return updated;
    }

    public InventoryItem restock(String itemId, int amount) {
        InventoryItem item = getById(itemId);
        item.restock(amount);
        repository.save(item);
        return item;
    }

    public InventoryItem deduct(String itemId, int amount) {
        InventoryItem item = getById(itemId);
        item.deduct(amount); // EBookStock blocks this if its license expired
        repository.save(item);
        return item;
    }


    public InventoryItem delete(String itemId) {
        InventoryItem item = getById(itemId);
        repository.deleteById(item.getItemId());
        return item;
    }

    // ---------- Form helpers ----------

    /** Pre-fills the edit form from an existing item. */
    public InventoryForm toForm(InventoryItem item) {
        InventoryForm form = new InventoryForm();
        form.setItemType(item.getItemType());
        form.setIsbn(item.getIsbn());
        form.setBookTitle(item.getBookTitle());
        form.setQuantity(item.getQuantity());
        form.setReorderLevel(item.getReorderLevel());
        form.setUnitCost(item.getUnitCost());

        if (item instanceof PrintedBookStock printed) {
            form.setWarehouse(printed.getWarehouse());
            form.setShelfCode(printed.getShelfCode());
        } else if (item instanceof EBookStock ebook) {
            form.setPlatform(ebook.getPlatform());
            form.setLicenseExpiry(ebook.getLicenseExpiry());
        }
        return form;
    }

    private InventoryItem buildItem(String itemId, InventoryForm f) {
        String type = f.getItemType() == null ? "" : f.getItemType();
        return switch (type) {
            case PrintedBookStock.TYPE -> new PrintedBookStock(itemId, trim(f.getIsbn()), trim(f.getBookTitle()),
                    f.getQuantity(), f.getReorderLevel(), f.getUnitCost(), LocalDate.now(),
                    trim(f.getWarehouse()), trim(f.getShelfCode()).toUpperCase(Locale.ROOT));
            case EBookStock.TYPE -> new EBookStock(itemId, trim(f.getIsbn()), trim(f.getBookTitle()),
                    f.getQuantity(), f.getReorderLevel(), f.getUnitCost(), LocalDate.now(),
                    trim(f.getPlatform()), f.getLicenseExpiry());
            default -> throw new IllegalArgumentException("Choose printed book or e-book.");
        };
    }

    private void ensureNoDuplicate(String isbn, String type, String excludeId) {
        String target = normalizeIsbn(isbn);
        boolean exists = repository.findAll().stream()
                .anyMatch(i -> i.getItemType().equals(type)
                        && normalizeIsbn(i.getIsbn()).equals(target)
                        && !i.getItemId().equalsIgnoreCase(excludeId == null ? "" : excludeId));
        if (exists) {
            throw new DuplicateStockException("This ISBN is already in stock as the same type. "
                    + "Edit the existing item or restock it instead.");
        }
    }

    public static String normalizeIsbn(String isbn) {
        return isbn == null ? "" : isbn.replace("-", "").replace(" ", "").toUpperCase(Locale.ROOT);
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
