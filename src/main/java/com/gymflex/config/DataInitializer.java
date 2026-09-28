package com.gymflex.config;

import com.gymflex.entity.Plan;
import com.gymflex.repository.PlanRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {
    @Bean
    CommandLineRunner seedPlans(PlanRepository plans) {
        return args -> {
            List<Plan> defaults = List.of(
                    new Plan("Monthly", 1, new BigDecimal("199.00"), "Flexible month-to-month access."),
                    new Plan("Quarterly", 3, new BigDecimal("499.00"), "Three months of uninterrupted training."),
                    new Plan("Yearly", 12, new BigDecimal("1799.00"), "Best value for a full year of training."));
            for (Plan plan : defaults) {
                if (plans.findByPlanNameIgnoreCase(plan.getPlanName()).isEmpty()) {
                    plans.save(plan);
                }
            }
        };
    }
}
