package com.gymflex.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "check_ins", uniqueConstraints = @UniqueConstraint(name = "uk_checkin_member_day", columnNames = {"member_id", "check_in_date"}))
public class CheckIn {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "check_in_date_time", nullable = false)
    private LocalDateTime checkInDateTime;

    @Column(name = "check_in_date", nullable = false)
    private LocalDate checkInDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    protected CheckIn() {}

    public CheckIn(LocalDateTime checkInDateTime, Member member) {
        this.checkInDateTime = checkInDateTime;
        this.checkInDate = checkInDateTime.toLocalDate();
        this.member = member;
    }

    public Long getId() { return id; }
    public LocalDateTime getCheckInDateTime() { return checkInDateTime; }
    public LocalDate getCheckInDate() { return checkInDate; }
    public Member getMember() { return member; }
}
