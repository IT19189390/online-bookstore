package com.bookstore.inventory.repository;

import com.bookstore.common.storage.TextFileHandler;
import com.bookstore.inventory.model.EBookStock;
import com.bookstore.inventory.model.InventoryItem;
import com.bookstore.inventory.model.PrintedBookStock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Repository
public class InventoryRepository {

    private static final Logger log = LoggerFactory.getLogger(InventoryRepository.class);

    public static final String FILE_NAME = "inventory.txt";
    private static final String HEADER =
            "itemType|itemId|isbn|bookTitle|quantity|reorderLevel|unitCost|lastUpdated|warehouse-or-platform|shelfCode-or-licenseExpiry";
    private static final String ID_PREFIX = "INV";
    private static final int FIELD_COUNT = 10;

    private final TextFileHandler fileHandler;

    public InventoryRepository(TextFileHandler fileHandler) {
        this.fileHandler = fileHandler;
    }

    // READ
    public synchronized List<InventoryItem> findAll() {
        List<InventoryItem> items = new ArrayList<>();
        int lineNo = 0;
        for (String line : fileHandler.readLines(FILE_NAME)) {
            lineNo++;
            try {
                items.add(parse(line));
            } catch (RuntimeException e) {
                log.warn("Skipping invalid line {} in {}: {} ({})", lineNo, FILE_NAME, line, e.getMessage());
            }
        }
        return items;
    }

    public synchronized Optional<InventoryItem> findById(String itemId) {
        return findAll().stream()
                .filter(item -> item.getItemId().equalsIgnoreCase(itemId))
                .findFirst();
    }

    public synchronized void save(InventoryItem item) {
        List<InventoryItem> items = findAll();
        boolean replaced = false;
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getItemId().equalsIgnoreCase(item.getItemId())) {
                items.set(i, item);
                replaced = true;
                break;
            }
        }
        if (!replaced) {
            items.add(item);
        }
        writeAll(items);
    }

    public synchronized boolean deleteById(String itemId) {
        List<InventoryItem> items = findAll();
        boolean removed = items.removeIf(item -> item.getItemId().equalsIgnoreCase(itemId));
        if (removed) {
            writeAll(items);
        }
        return removed;
    }

    /** Next ID like INV008, based on the highest existing number. */
    public synchronized String nextId() {
        int max = 0;
        for (InventoryItem item : findAll()) {
            String id = item.getItemId();
            if (id.startsWith(ID_PREFIX)) {
                try {
                    max = Math.max(max, Integer.parseInt(id.substring(ID_PREFIX.length())));
                } catch (NumberFormatException ignored) {
                    // non-standard ID, ignore
                }
            }
        }
        return String.format("%s%03d", ID_PREFIX, max + 1);
    }

    private void writeAll(List<InventoryItem> items) {
        List<String> lines = new ArrayList<>();
        for (InventoryItem item : items) {
            lines.add(item.toFileString()); // polymorphic: each subclass adds its own fields
        }
        fileHandler.writeLines(FILE_NAME, HEADER, lines);
    }

    private InventoryItem parse(String line) {
        String[] p = line.split("\\|", -1);
        if (p.length != FIELD_COUNT) {
            throw new IllegalArgumentException("expected " + FIELD_COUNT + " fields but found " + p.length);
        }
        String type = p[0].trim();
        String id = p[1].trim();
        String isbn = p[2].trim();
        String title = p[3].trim();
        int quantity = Integer.parseInt(p[4].trim());
        int reorderLevel = Integer.parseInt(p[5].trim());
        double unitCost = Double.parseDouble(p[6].trim());
        LocalDate lastUpdated = LocalDate.parse(p[7].trim());

        return switch (type) {
            case PrintedBookStock.TYPE -> new PrintedBookStock(id, isbn, title, quantity, reorderLevel,
                    unitCost, lastUpdated, p[8].trim(), p[9].trim());
            case EBookStock.TYPE -> new EBookStock(id, isbn, title, quantity, reorderLevel,
                    unitCost, lastUpdated, p[8].trim(), LocalDate.parse(p[9].trim()));
            default -> throw new IllegalArgumentException("unknown item type '" + type + "'");
        };
    }
}
