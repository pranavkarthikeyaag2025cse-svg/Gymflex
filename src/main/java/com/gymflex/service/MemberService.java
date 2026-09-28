package com.gymflex.service;

import com.gymflex.dto.Dtos.MemberRequest;
import com.gymflex.dto.Dtos.MemberEditRequest;
import com.gymflex.dto.Dtos.MemberResponse;
import com.gymflex.dto.Dtos.MembershipUpdateRequest;
import com.gymflex.entity.Member;
import com.gymflex.entity.Membership;
import com.gymflex.entity.Plan;
import com.gymflex.exception.ResourceNotFoundException;
import com.gymflex.repository.MemberRepository;
import com.gymflex.repository.MembershipRepository;
import com.gymflex.repository.PlanRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MemberService {
    private final MemberRepository members;
    private final MembershipRepository memberships;
    private final PlanRepository plans;
    private final Clock clock;

    public MemberService(MemberRepository members, MembershipRepository memberships, PlanRepository plans, Clock clock) {
        this.members = members;
        this.memberships = memberships;
        this.plans = plans;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<MemberResponse> findAll() {
        return members.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public MemberResponse findById(Long id) {
        return toResponse(getMember(id));
    }

    public MemberResponse create(MemberRequest request) {
        if (members.existsByEmailIgnoreCase(request.email())) {
            throw new DataIntegrityViolationException("Email is already registered.");
        }
        return toResponse(members.save(new Member(request.name().trim(), request.email().trim(), request.phone().trim(), request.address().trim())));
    }

    public MemberResponse update(Long id, MemberRequest request) {
        Member member = getMember(id);
        if (members.existsByEmailIgnoreCaseAndIdNot(request.email(), id)) {
            throw new DataIntegrityViolationException("Email is already registered.");
        }
        member.update(request.name().trim(), request.email().trim(), request.phone().trim(), request.address().trim());
        return toResponse(member);
    }

    public MemberResponse updateDetails(Long id, MemberEditRequest request) {
        MemberRequest profile = request.member();
        MembershipUpdateRequest details = request.membership();
        if (details.expiryDate().isBefore(details.startDate())) {
            throw new IllegalArgumentException("Membership expiry date cannot be before its start date.");
        }
        if (members.existsByEmailIgnoreCaseAndIdNot(profile.email(), id)) {
            throw new DataIntegrityViolationException("Email is already registered.");
        }

        Member member = getMember(id);
        Membership membership = memberships.findTopByMemberIdOrderByExpiryDateDescIdDesc(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member " + id + " has no membership to edit."));
        Plan plan = plans.findById(details.planId())
                .orElseThrow(() -> new ResourceNotFoundException("Plan " + details.planId() + " was not found."));

        member.update(profile.name().trim(), profile.email().trim(), profile.phone().trim(), profile.address().trim());
        membership.updateDetails(details.startDate(), details.expiryDate(),
                MembershipService.statusFor(details.expiryDate(), LocalDate.now(clock)), plan);
        return toResponse(member);
    }

    public void delete(Long id) {
        members.delete(getMember(id));
    }

    public Member getMember(Long id) {
        return members.findById(id).orElseThrow(() -> new ResourceNotFoundException("Member " + id + " was not found."));
    }

    private MemberResponse toResponse(Member member) {
        Membership latest = member.getMemberships().stream()
                .max(Comparator.comparing(Membership::getExpiryDate).thenComparing(Membership::getId))
                .orElse(null);
        return new MemberResponse(member.getId(), member.getName(), member.getEmail(), member.getPhone(), member.getAddress(),
                member.getCreatedAt(), latest == null ? null : latest.getPlan().getPlanName(),
                latest == null ? null : latest.getStartDate(), latest == null ? null : latest.getExpiryDate(),
                latest == null ? "NO_MEMBERSHIP" : MembershipService.statusFor(latest.getExpiryDate(), LocalDate.now(clock)));
    }
}
