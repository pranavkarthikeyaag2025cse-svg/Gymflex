package com.gymflex.controller;

import com.gymflex.dto.Dtos.MembershipResponse;
import com.gymflex.dto.Dtos.RegistrationRequest;
import com.gymflex.dto.Dtos.RenewRequest;
import com.gymflex.service.MembershipService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/memberships")
public class MembershipController {
    private final MembershipService memberships;

    public MembershipController(MembershipService memberships) { this.memberships = memberships; }

    @PostMapping("/register")
    public ResponseEntity<MembershipResponse> register(@Valid @RequestBody RegistrationRequest request) {
        MembershipResponse created = memberships.register(request);
        return ResponseEntity.created(URI.create("/api/memberships/" + created.id())).body(created);
    }

    @PostMapping("/{id}/renew")
    public MembershipResponse renew(@PathVariable Long id, @Valid @RequestBody RenewRequest request) {
        return memberships.renew(id, request);
    }

    @GetMapping
    public List<MembershipResponse> findAll() { return memberships.findAll(); }

    @GetMapping("/expiring-soon")
    public List<MembershipResponse> expiringSoon() { return memberships.expiringSoon(); }
}
