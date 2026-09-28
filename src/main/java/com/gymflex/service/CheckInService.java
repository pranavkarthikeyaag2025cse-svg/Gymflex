package com.gymflex.service;

import com.gymflex.dto.Dtos.AttendanceSummary;
import com.gymflex.dto.Dtos.CheckInResponse;
import com.gymflex.entity.CheckIn;
import com.gymflex.entity.Member;
import com.gymflex.entity.Membership;
import com.gymflex.exception.ResourceNotFoundException;
import com.gymflex.repository.CheckInRepository;
import com.gymflex.repository.MemberRepository;
import com.gymflex.repository.MembershipRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class CheckInService {
    private final CheckInRepository checkIns;
    private final MemberRepository members;
    private final MembershipRepository memberships;
    private final Clock clock;

    public CheckInService(CheckInRepository checkIns, MemberRepository members, MembershipRepository memberships, Clock clock) {
        this.checkIns = checkIns;
        this.members = members;
        this.memberships = memberships;
        this.clock = clock;
    }

    public CheckInResponse checkIn(Long memberId) {
        Member member = members.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member " + memberId + " was not found."));
        Membership membership = memberships.findTopByMemberIdOrderByExpiryDateDescIdDesc(memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Check-in rejected: Member has no active membership."));
        LocalDateTime checkedInAt = LocalDateTime.now(clock);
        LocalDate today = checkedInAt.toLocalDate();
        if (membership.getExpiryDate().isBefore(today)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Check-in rejected: Member's membership has expired.");
        }
        if (checkIns.existsByMemberIdAndCheckInDate(memberId, today)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Member has already checked in today.");
        }
        try {
            return toResponse(checkIns.save(new CheckIn(checkedInAt, member)));
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Member has already checked in today.");
        }
    }

    @Transactional(readOnly = true)
    public List<CheckInResponse> forMember(Long memberId) {
        if (!members.existsById(memberId)) throw new ResourceNotFoundException("Member " + memberId + " was not found.");
        return checkIns.findByMemberIdOrderByCheckInDateTimeDesc(memberId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public AttendanceSummary monthly(Long memberId) {
        Member member = members.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member " + memberId + " was not found."));
        YearMonth month = YearMonth.now(clock);
        List<CheckInResponse> records = checkIns.findByMemberIdAndCheckInDateBetweenOrderByCheckInDateTimeDesc(
                memberId, month.atDay(1), month.atEndOfMonth()).stream().map(this::toResponse).toList();
        return new AttendanceSummary(member.getName(), month.toString(), records.size(), records);
    }

    @Transactional(readOnly = true)
    public List<CheckInResponse> all(LocalDate date) {
        List<CheckIn> records = date == null ? checkIns.findAllByOrderByCheckInDateTimeDesc()
                : checkIns.findByCheckInDateOrderByCheckInDateTimeDesc(date);
        return records.stream().map(this::toResponse).toList();
    }

    private CheckInResponse toResponse(CheckIn checkIn) {
        return new CheckInResponse(checkIn.getId(), checkIn.getMember().getId(), checkIn.getMember().getName(), checkIn.getCheckInDateTime());
    }
}
