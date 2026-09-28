package com.gymflex.controller;

import com.gymflex.dto.Dtos.PlanRequest;
import com.gymflex.dto.Dtos.PlanResponse;
import com.gymflex.service.PlanService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/plans")
public class PlanController {
    private final PlanService plans;

    public PlanController(PlanService plans) { this.plans = plans; }

    @GetMapping
    public List<PlanResponse> findAll() { return plans.findAll(); }

    @GetMapping("/{id}")
    public PlanResponse findById(@PathVariable Long id) { return plans.findById(id); }

    @PostMapping
    public ResponseEntity<PlanResponse> create(@Valid @RequestBody PlanRequest request) {
        PlanResponse created = plans.create(request);
        return ResponseEntity.created(URI.create("/api/plans/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public PlanResponse update(@PathVariable Long id, @Valid @RequestBody PlanRequest request) {
        return plans.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        plans.delete(id);
        return ResponseEntity.noContent().build();
    }
}
