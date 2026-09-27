package com.bookstore.common.web;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice
public class NavigationAdvice {

    @ModelAttribute("modules")
    public List<DashboardModule> modules() {
        return DashboardModule.ALL;
    }
}
