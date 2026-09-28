package com.gymflex.controller;

import com.gymflex.dto.Dtos.AttendanceSummary;
import com.gymflex.dto.Dtos.CheckInRequest;
import com.gymflex.dto.Dtos.CheckInResponse;
import com.gymflex.service.CheckInService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/checkins")
public class CheckInController {
    private final CheckInService checkIns;

    public CheckInController(CheckInService checkIns) { this.checkIns = checkIns; }

    @PostMapping
    public ResponseEntity<CheckInResponse> checkIn(@Valid @RequestBody CheckInRequest request) {
        return ResponseEntity.status(201).body(checkIns.checkIn(request.memberId()));
    }

    @GetMapping
    public List<CheckInResponse> all(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return checkIns.all(date);
    }

    @GetMapping("/member/{memberId}")
    public List<CheckInResponse> forMember(@PathVariable Long memberId) { return checkIns.forMember(memberId); }

    @GetMapping("/monthly/{memberId}")
    public AttendanceSummary monthly(@PathVariable Long memberId) { return checkIns.monthly(memberId); }
}
