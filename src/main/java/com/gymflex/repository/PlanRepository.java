package com.gymflex.repository;

import com.gymflex.entity.Plan;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanRepository extends JpaRepository<Plan, Long> {
    Optional<Plan> findByPlanNameIgnoreCase(String planName);
    boolean existsByPlanNameIgnoreCaseAndIdNot(String planName, Long id);
}
