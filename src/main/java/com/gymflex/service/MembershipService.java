package com.gymflex.service;

import com.gymflex.dto.Dtos.MembershipResponse;
import com.gymflex.dto.Dtos.RegistrationRequest;
import com.gymflex.dto.Dtos.RenewRequest;
import com.gymflex.entity.Member;
import com.gymflex.entity.Membership;
import com.gymflex.entity.Plan;
import com.gymflex.exception.ResourceNotFoundException;
import com.gymflex.repository.MemberRepository;
import com.gymflex.repository.MembershipRepository;
import com.gymflex.repository.PlanRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MembershipService {
    private final MemberRepository members;
    private final PlanRepository plans;
    private final MembershipRepository memberships;
    private final Clock clock;

    public MembershipService(MemberRepository members, PlanRepository plans, MembershipRepository memberships, Clock clock) {
        this.members = members;
        this.plans = plans;
        this.memberships = memberships;
        this.clock = clock;
    }

    public MembershipResponse register(RegistrationRequest request) {
        if (members.existsByEmailIgnoreCase(request.email())) {
            throw new DataIntegrityViolationException("Email is already registered.");
        }
        Plan plan = getPlan(request.planId());
        Member member = members.save(new Member(request.name().trim(), request.email().trim(), request.phone().trim(), request.address().trim()));
        LocalDate expiry = request.startDate().plusMonths(plan.getDurationInMonths());
        Membership membership = memberships.save(new Membership(request.startDate(), expiry, statusFor(expiry, LocalDate.now(clock)), member, plan));
        return toResponse(membership);
    }

    public MembershipResponse renew(Long membershipId, RenewRequest request) {
        Membership membership = memberships.findById(membershipId)
                .orElseThrow(() -> new ResourceNotFoundException("Membership " + membershipId + " was not found."));
        Plan plan = getPlan(request.planId());
        // Renewal always extends the stored expiry, including for memberships that have expired.
        LocalDate newExpiry = membership.getExpiryDate().plusMonths(plan.getDurationInMonths());
        membership.renew(newExpiry, statusFor(newExpiry, LocalDate.now(clock)), plan);
        return toResponse(membership);
    }

    @Transactional(readOnly = true)
    public List<MembershipResponse> findAll() {
        return memberships.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<MembershipResponse> expiringSoon() {
        LocalDate today = LocalDate.now(clock);
        return memberships.findByExpiryDateBetweenOrderByExpiryDateAsc(today, today.plusDays(7))
                .stream().map(this::toResponse).toList();
    }

    public static String statusFor(LocalDate expiryDate, LocalDate today) {
        if (expiryDate.isBefore(today)) return "EXPIRED";
        if (!expiryDate.isAfter(today.plusDays(7))) return "EXPIRING_SOON";
        return "ACTIVE";
    }

    private Plan getPlan(Long id) {
        return plans.findById(id).orElseThrow(() -> new ResourceNotFoundException("Plan " + id + " was not found."));
    }

    private MembershipResponse toResponse(Membership membership) {
        return new MembershipResponse(membership.getId(), membership.getMember().getId(), membership.getMember().getName(), membership.getMember().getPhone(),
                membership.getPlan().getId(), membership.getPlan().getPlanName(), membership.getStartDate(),
                membership.getExpiryDate(), statusFor(membership.getExpiryDate(), LocalDate.now(clock)));
    }
}
