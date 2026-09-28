package com.gymflex.service;

import com.gymflex.dto.Dtos.CheckInResponse;
import com.gymflex.dto.Dtos.DashboardStats;
import com.gymflex.entity.Membership;
import com.gymflex.repository.CheckInRepository;
import com.gymflex.repository.MemberRepository;
import com.gymflex.repository.MembershipRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DashboardService {
    private final MemberRepository members;
    private final MembershipRepository memberships;
    private final CheckInRepository checkIns;
    private final Clock clock;

    public DashboardService(MemberRepository members, MembershipRepository memberships, CheckInRepository checkIns, Clock clock) {
        this.members = members;
        this.memberships = memberships;
        this.checkIns = checkIns;
        this.clock = clock;
    }

    public DashboardStats stats() {
        LocalDate today = LocalDate.now(clock);
        List<Membership> allMemberships = memberships.findAll();
        long active = allMemberships.stream().filter(m -> !m.getExpiryDate().isBefore(today)).count();
        long expired = allMemberships.stream().filter(m -> m.getExpiryDate().isBefore(today)).count();
        long expiring = allMemberships.stream().filter(m -> !m.getExpiryDate().isBefore(today)
                && !m.getExpiryDate().isAfter(today.plusDays(7))).count();
        List<CheckInResponse> recent = checkIns.findByCheckInDateOrderByCheckInDateTimeDesc(today).stream()
                .limit(8).map(record -> new CheckInResponse(record.getId(), record.getMember().getId(),
                        record.getMember().getName(), record.getCheckInDateTime())).toList();
        return new DashboardStats(members.count(), active, expired, expiring, checkIns.countByCheckInDate(today), recent);
    }
}
