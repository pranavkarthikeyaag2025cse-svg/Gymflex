package com.gymflex.service;

import com.gymflex.dto.Dtos.PlanRequest;
import com.gymflex.dto.Dtos.PlanResponse;
import com.gymflex.entity.Plan;
import com.gymflex.exception.ResourceNotFoundException;
import com.gymflex.repository.MembershipRepository;
import com.gymflex.repository.PlanRepository;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PlanService {
    private final PlanRepository plans;
    private final MembershipRepository memberships;

    public PlanService(PlanRepository plans, MembershipRepository memberships) {
        this.plans = plans;
        this.memberships = memberships;
    }

    @Transactional(readOnly = true)
    public List<PlanResponse> findAll() {
        return plans.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public PlanResponse findById(Long id) {
        return toResponse(getPlan(id));
    }

    public PlanResponse create(PlanRequest request) {
        if (plans.findByPlanNameIgnoreCase(request.planName().trim()).isPresent()) {
            throw new DataIntegrityViolationException("A plan with this name already exists.");
        }
        return toResponse(plans.save(new Plan(request.planName().trim(), request.durationInMonths(), request.price(), clean(request.description()))));
    }

    public PlanResponse update(Long id, PlanRequest request) {
        Plan plan = getPlan(id);
        if (plans.existsByPlanNameIgnoreCaseAndIdNot(request.planName().trim(), id)) {
            throw new DataIntegrityViolationException("A plan with this name already exists.");
        }
        plan.update(request.planName().trim(), request.durationInMonths(), request.price(), clean(request.description()));
        return toResponse(plan);
    }

    public void delete(Long id) {
        Plan plan = getPlan(id);
        if (memberships.existsByPlanId(id)) {
            throw new DataIntegrityViolationException("This plan is used by a membership and cannot be deleted.");
        }
        plans.delete(plan);
    }

    public Plan getPlan(Long id) {
        return plans.findById(id).orElseThrow(() -> new ResourceNotFoundException("Plan " + id + " was not found."));
    }

    private PlanResponse toResponse(Plan plan) {
        return new PlanResponse(plan.getId(), plan.getPlanName(), plan.getDurationInMonths(), plan.getPrice(), plan.getDescription());
    }

    private String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
