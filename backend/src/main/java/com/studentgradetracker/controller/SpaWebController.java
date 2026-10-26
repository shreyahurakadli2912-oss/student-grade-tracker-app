package com.studentgradetracker.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller to route client-side React SPA navigation to index.html.
 * Maps application frontend routes while ensuring APIs and static assets are untouched.
 */
@Controller
public class SpaWebController {

    @GetMapping(value = {
        "/",
        "/dashboard",
        "/students",
        "/import",
        "/subjects",
        "/reports",
        "/analytics",
        "/gradescale",
        "/settings"
    })
    public String forwardSpaRoutes() {
        return "forward:/index.html";
    }
}
