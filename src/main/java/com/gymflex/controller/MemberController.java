package com.gymflex.controller;

import com.gymflex.dto.Dtos.MemberRequest;
import com.gymflex.dto.Dtos.MemberEditRequest;
import com.gymflex.dto.Dtos.MemberResponse;
import com.gymflex.service.MemberService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
public class MemberController {
    private final MemberService members;

    public MemberController(MemberService members) {
        this.members = members;
    }

    @GetMapping
    public List<MemberResponse> findAll() { return members.findAll(); }

    @GetMapping("/{id}")
    public MemberResponse findById(@PathVariable Long id) { return members.findById(id); }

    @PostMapping
    public ResponseEntity<MemberResponse> create(@Valid @RequestBody MemberRequest request) {
        MemberResponse created = members.create(request);
        return ResponseEntity.created(URI.create("/api/members/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public MemberResponse update(@PathVariable Long id, @Valid @RequestBody MemberRequest request) {
        return members.update(id, request);
    }

    @PutMapping("/{id}/details")
    public MemberResponse updateDetails(@PathVariable Long id, @Valid @RequestBody MemberEditRequest request) {
        return members.updateDetails(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        members.delete(id);
        return ResponseEntity.noContent().build();
    }
}
