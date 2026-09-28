package com.gymflex.controller;

import com.gymflex.dto.Dtos.DashboardStats;
import com.gymflex.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final DashboardService dashboard;

    public DashboardController(DashboardService dashboard) { this.dashboard = dashboard; }

    @GetMapping("/stats")
    public DashboardStats stats() { return dashboard.stats(); }
}
