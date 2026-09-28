package com.gymflex.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "plans")
public class Plan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String planName;

    @Column(nullable = false)
    private int durationInMonths;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(length = 500)
    private String description;

    @OneToMany(mappedBy = "plan")
    private List<Membership> memberships = new ArrayList<>();

    protected Plan() {}

    public Plan(String planName, int durationInMonths, BigDecimal price, String description) {
        this.planName = planName;
        this.durationInMonths = durationInMonths;
        this.price = price;
        this.description = description;
    }

    public Long getId() { return id; }
    public String getPlanName() { return planName; }
    public int getDurationInMonths() { return durationInMonths; }
    public BigDecimal getPrice() { return price; }
    public String getDescription() { return description; }

    public void update(String planName, int durationInMonths, BigDecimal price, String description) {
        this.planName = planName;
        this.durationInMonths = durationInMonths;
        this.price = price;
        this.description = description;
    }
}
