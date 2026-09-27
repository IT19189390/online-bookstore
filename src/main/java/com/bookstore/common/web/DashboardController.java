package com.bookstore.common.web;

import com.bookstore.inventory.service.InventoryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class DashboardController {

    private final InventoryService inventoryService;

    public DashboardController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/")
    public String dashboard(Model model) {
        model.addAttribute("active", "dashboard");
        model.addAttribute("summary", inventoryService.getSummary());
        model.addAttribute("attentionItems", inventoryService.getItemsNeedingAttention(5));
        return "dashboard";
    }

    @GetMapping("/modules/{key}")
    public String placeholder(@PathVariable("key") String key, Model model) {
        DashboardModule module = DashboardModule.findByKey(key).orElse(null);
        if (module == null) {
            return "redirect:/";
        }
        model.addAttribute("active", key);
        model.addAttribute("module", module);
        return "placeholder";
    }
}
