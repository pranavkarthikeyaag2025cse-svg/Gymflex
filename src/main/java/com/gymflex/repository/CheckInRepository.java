package com.gymflex.repository;

import com.gymflex.entity.CheckIn;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CheckInRepository extends JpaRepository<CheckIn, Long> {
    boolean existsByMemberIdAndCheckInDate(Long memberId, LocalDate checkInDate);
    List<CheckIn> findByMemberIdOrderByCheckInDateTimeDesc(Long memberId);
    List<CheckIn> findByMemberIdAndCheckInDateBetweenOrderByCheckInDateTimeDesc(Long memberId, LocalDate from, LocalDate through);
    List<CheckIn> findByCheckInDateOrderByCheckInDateTimeDesc(LocalDate date);
    List<CheckIn> findAllByOrderByCheckInDateTimeDesc();
    long countByCheckInDate(LocalDate date);
}
