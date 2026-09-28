package com.gymflex.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class Dtos {
    private Dtos() {}

    public record PlanRequest(
            @NotBlank @Size(max = 80) String planName,
            @NotNull @Positive Integer durationInMonths,
            @NotNull @PositiveOrZero BigDecimal price,
            @Size(max = 500) String description) {}

    public record PlanResponse(Long id, String planName, int durationInMonths, BigDecimal price, String description) {}

    public record MemberRequest(
            @NotBlank @Size(max = 100) String name,
            @NotBlank @Email @Size(max = 150) String email,
            @NotBlank @Pattern(regexp = "^\\+?[0-9()\\- .]{7,20}$", message = "Enter a valid phone number") String phone,
            @NotBlank @Size(max = 500) String address) {}

    public record MembershipUpdateRequest(
            @NotNull @Positive Long planId,
            @NotNull LocalDate startDate,
            @NotNull LocalDate expiryDate) {}

    public record MemberEditRequest(
            @NotNull @Valid MemberRequest member,
            @NotNull @Valid MembershipUpdateRequest membership) {}

    public record RegistrationRequest(
            @NotBlank @Size(max = 100) String name,
            @NotBlank @Email @Size(max = 150) String email,
            @NotBlank @Pattern(regexp = "^\\+?[0-9()\\- .]{7,20}$", message = "Enter a valid phone number") String phone,
            @NotBlank @Size(max = 500) String address,
            @NotNull @Positive Long planId,
            @NotNull LocalDate startDate) {}

    public record MemberResponse(Long id, String name, String email, String phone, String address,
                                 LocalDateTime createdAt, String planName, LocalDate startDate,
                                 LocalDate expiryDate, String status) {}

    public record RenewRequest(@NotNull @Positive Long planId) {}

        public record MembershipResponse(Long id, Long memberId, String memberName, String phone, Long planId,
                                     String planName, LocalDate startDate, LocalDate expiryDate,
                                     String status) {}

    public record CheckInRequest(@NotNull @Positive Long memberId) {}

    public record CheckInResponse(Long id, Long memberId, String memberName, LocalDateTime checkInDateTime) {}

    public record AttendanceSummary(String memberName, String month, long totalAttendance,
                                   List<CheckInResponse> checkIns) {}

    public record DashboardStats(long totalMembers, long activeMembers, long expiredMembers,
                                 long expiringSoon, long todaysCheckIns, List<CheckInResponse> recentCheckIns) {}

    public record ErrorResponse(LocalDateTime timestamp, int status, String message) {}
}
