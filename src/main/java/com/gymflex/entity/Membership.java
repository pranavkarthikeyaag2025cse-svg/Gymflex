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
import java.time.LocalDate;

@Entity
@Table(name = "memberships")
public class Membership {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate expiryDate;

    @Column(nullable = false, length = 20)
    private String status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    protected Membership() {}

    public Membership(LocalDate startDate, LocalDate expiryDate, String status, Member member, Plan plan) {
        this.startDate = startDate;
        this.expiryDate = expiryDate;
        this.status = status;
        this.member = member;
        this.plan = plan;
    }

    public Long getId() { return id; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public String getStatus() { return status; }
    public Member getMember() { return member; }
    public Plan getPlan() { return plan; }

    public void renew(LocalDate expiryDate, String status, Plan plan) {
        this.expiryDate = expiryDate;
        this.status = status;
        this.plan = plan;
    }

    public void updateDetails(LocalDate startDate, LocalDate expiryDate, String status, Plan plan) {
        this.startDate = startDate;
        this.expiryDate = expiryDate;
        this.status = status;
        this.plan = plan;
    }

    public void refreshStatus(String status) { this.status = status; }
}
