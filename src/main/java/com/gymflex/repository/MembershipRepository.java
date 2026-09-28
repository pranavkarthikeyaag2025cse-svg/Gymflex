package com.gymflex.repository;

import com.gymflex.entity.Membership;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MembershipRepository extends JpaRepository<Membership, Long> {
    Optional<Membership> findTopByMemberIdOrderByExpiryDateDescIdDesc(Long memberId);
    List<Membership> findByExpiryDateBetweenOrderByExpiryDateAsc(LocalDate from, LocalDate through);
    boolean existsByPlanId(Long planId);
}
