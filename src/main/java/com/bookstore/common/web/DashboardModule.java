package com.bookstore.common.web;

import java.util.List;
import java.util.Optional;


public final class DashboardModule {

    public static final List<DashboardModule> ALL = List.of(
            new DashboardModule("books", "Book Management",
                    "Add, search and edit books in the catalogue.", "/modules/books", false),
            new DashboardModule("customers", "Customer Management",
                    "Register customers and keep their details up to date.", "/modules/customers", false),
            new DashboardModule("inventory", "Inventory Management",
                    "Track stock levels, restock and spot low stock.", "/inventory", true),
            new DashboardModule("orders", "Order / Sales Management",
                    "Create orders and record sales.", "/modules/orders", false),
            new DashboardModule("suppliers", "Supplier Management",
                    "Manage the suppliers who deliver our books.", "/modules/suppliers", false),
            new DashboardModule("staff", "Staff / User Management",
                    "Manage staff accounts and roles.", "/modules/staff", false)
    );

    private final String key;
    private final String name;
    private final String description;
    private final String url;
    private final boolean implemented;

    public DashboardModule(String key, String name, String description, String url, boolean implemented) {
        this.key = key;
        this.name = name;
        this.description = description;
        this.url = url;
        this.implemented = implemented;
    }

    public static Optional<DashboardModule> findByKey(String key) {
        return ALL.stream().filter(m -> m.key.equals(key)).findFirst();
    }

    public String getKey() { return key; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getUrl() { return url; }
    public boolean isImplemented() { return implemented; }
}
