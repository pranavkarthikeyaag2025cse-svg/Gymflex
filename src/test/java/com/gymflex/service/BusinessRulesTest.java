package com.gymflex.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

import com.gymflex.dto.Dtos.MemberEditRequest;
import com.gymflex.dto.Dtos.MemberRequest;
import com.gymflex.dto.Dtos.MembershipUpdateRequest;
import com.gymflex.dto.Dtos.RenewRequest;
import com.gymflex.entity.CheckIn;
import com.gymflex.entity.Member;
import com.gymflex.entity.Membership;
import com.gymflex.entity.Plan;
import com.gymflex.repository.CheckInRepository;
import com.gymflex.repository.MemberRepository;
import com.gymflex.repository.MembershipRepository;
import com.gymflex.repository.PlanRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class BusinessRulesTest {
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-09-28T10:00:00Z"), ZoneOffset.UTC);

    @Mock private MemberRepository members;
    @Mock private PlanRepository plans;
    @Mock private MembershipRepository memberships;
    @Mock private CheckInRepository checkIns;

    private MembershipService membershipService;
    private CheckInService checkInService;
    private MemberService memberService;

    @BeforeEach
    void setUp() {
        membershipService = new MembershipService(members, plans, memberships, CLOCK);
        checkInService = new CheckInService(checkIns, members, memberships, CLOCK);
        memberService = new MemberService(members, memberships, plans, CLOCK);
    }

    @Test
    void renewalExtendsStoredExpiryEvenWhenMembershipIsExpired() {
        Member member = memberWithResponseValues();
        Plan renewalPlan = plan(2L, "Quarterly", 3);
        LocalDate existingExpiry = LocalDate.of(2025, 6, 10);
        Membership membership = new Membership(existingExpiry.minusMonths(1), existingExpiry, "EXPIRED", member, org.mockito.Mockito.mock(Plan.class));
        when(memberships.findById(42L)).thenReturn(Optional.of(membership));
        when(plans.findById(2L)).thenReturn(Optional.of(renewalPlan));

        membershipService.renew(42L, new RenewRequest(2L));

        assertEquals(LocalDate.of(2025, 9, 10), membership.getExpiryDate());
        assertEquals("EXPIRED", membership.getStatus());
    }

    @Test
    void checkInRejectsMembershipExpiredBeforeToday() {
        Member member = member();
        Membership membership = org.mockito.Mockito.mock(Membership.class);
        when(members.findById(7L)).thenReturn(Optional.of(member));
        when(memberships.findTopByMemberIdOrderByExpiryDateDescIdDesc(7L)).thenReturn(Optional.of(membership));
        when(membership.getExpiryDate()).thenReturn(LocalDate.of(2026, 9, 27));

        ResponseStatusException error = assertThrows(ResponseStatusException.class, () -> checkInService.checkIn(7L));

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
        assertEquals("Check-in rejected: Member's membership has expired.", error.getReason());
        verify(checkIns, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void checkInRejectsSecondVisitOnSameDay() {
        Member member = member();
        Membership membership = org.mockito.Mockito.mock(Membership.class);
        when(members.findById(7L)).thenReturn(Optional.of(member));
        when(memberships.findTopByMemberIdOrderByExpiryDateDescIdDesc(7L)).thenReturn(Optional.of(membership));
        when(membership.getExpiryDate()).thenReturn(LocalDate.now(CLOCK));
        when(checkIns.existsByMemberIdAndCheckInDate(7L, LocalDate.now(CLOCK))).thenReturn(true);

        ResponseStatusException error = assertThrows(ResponseStatusException.class, () -> checkInService.checkIn(7L));

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        assertEquals("Member has already checked in today.", error.getReason());
        verify(checkIns, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void checkInUsesOneCapturedTimeAcrossMidnightBoundary() {
        LocalDate expectedDate = LocalDate.of(2026, 9, 28);
        Clock midnightClock = new Clock() {
            private boolean firstRead = true;

            @Override
            public Instant instant() {
                if (firstRead) {
                    firstRead = false;
                    return Instant.parse("2026-09-28T23:59:59.999Z");
                }
                return Instant.parse("2026-09-29T00:00:00.001Z");
            }

            @Override
            public ZoneOffset getZone() { return ZoneOffset.UTC; }

            @Override
            public Clock withZone(java.time.ZoneId zone) { return this; }
        };
        Member member = member();
        when(member.getId()).thenReturn(7L);
        when(member.getName()).thenReturn("Test Member");
        Membership membership = org.mockito.Mockito.mock(Membership.class);
        when(members.findById(7L)).thenReturn(Optional.of(member));
        when(memberships.findTopByMemberIdOrderByExpiryDateDescIdDesc(7L)).thenReturn(Optional.of(membership));
        when(membership.getExpiryDate()).thenReturn(LocalDate.of(2026, 9, 30));
        when(checkIns.existsByMemberIdAndCheckInDate(7L, expectedDate)).thenReturn(false);
        when(checkIns.save(any(CheckIn.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CheckInService service = new CheckInService(checkIns, members, memberships, midnightClock);
        service.checkIn(7L);

        ArgumentCaptor<CheckIn> saved = ArgumentCaptor.forClass(CheckIn.class);
        verify(checkIns).save(saved.capture());
        assertEquals(expectedDate, saved.getValue().getCheckInDate());
        assertEquals(expectedDate, saved.getValue().getCheckInDateTime().toLocalDate());
        verify(checkIns).existsByMemberIdAndCheckInDate(7L, saved.getValue().getCheckInDate());
    }

    @Test
    void memberEditRejectsExpiryBeforeStartDate() {
        MemberRequest profile = new MemberRequest("Test Member", "test@example.com", "1234567890", "Gym Street");
        MembershipUpdateRequest dates = new MembershipUpdateRequest(1L,
                LocalDate.of(2026, 10, 2), LocalDate.of(2026, 10, 1));

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> memberService.updateDetails(7L, new MemberEditRequest(profile, dates)));

        assertEquals("Membership expiry date cannot be before its start date.", error.getMessage());
    }

    @Test
    void memberEditUpdatesMembershipDatesPlanAndStatus() {
        Member member = member();
        Membership membership = org.mockito.Mockito.mock(Membership.class);
        Plan plan = org.mockito.Mockito.mock(Plan.class);
        LocalDate startDate = LocalDate.of(2026, 10, 1);
        LocalDate expiryDate = LocalDate.of(2026, 12, 31);
        when(members.findById(7L)).thenReturn(Optional.of(member));
        when(memberships.findTopByMemberIdOrderByExpiryDateDescIdDesc(7L)).thenReturn(Optional.of(membership));
        when(plans.findById(2L)).thenReturn(Optional.of(plan));
        MemberRequest profile = new MemberRequest("Test Member", "test@example.com", "1234567890", "Gym Street");
        MembershipUpdateRequest dates = new MembershipUpdateRequest(2L, startDate, expiryDate);

        memberService.updateDetails(7L, new MemberEditRequest(profile, dates));

        verify(member).update("Test Member", "test@example.com", "1234567890", "Gym Street");
        verify(membership).updateDetails(startDate, expiryDate, "ACTIVE", plan);
    }

    private Member member() {
        return org.mockito.Mockito.mock(Member.class);
    }

    private Member memberWithResponseValues() {
        Member member = member();
        when(member.getId()).thenReturn(7L);
        when(member.getName()).thenReturn("Test Member");
        when(member.getPhone()).thenReturn("1234567890");
        return member;
    }

    private Plan plan(Long id, String name, int duration) {
        Plan plan = org.mockito.Mockito.mock(Plan.class);
        when(plan.getId()).thenReturn(id);
        when(plan.getPlanName()).thenReturn(name);
        when(plan.getDurationInMonths()).thenReturn(duration);
        return plan;
    }
}
