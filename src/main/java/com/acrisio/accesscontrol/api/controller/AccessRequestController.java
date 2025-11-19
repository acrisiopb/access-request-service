package com.acrisio.accesscontrol.api.controller;

import com.acrisio.accesscontrol.api.dto.AccessRequestCreateDTO;
import com.acrisio.accesscontrol.api.dto.AccessRequestResponseDTO;
import com.acrisio.accesscontrol.service.AccessRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
public class AccessRequestController {

    private final AccessRequestService accessRequestService;

    @PostMapping
    public ResponseEntity<AccessRequestResponseDTO> create(@RequestParam Long userId, @Valid @RequestBody AccessRequestCreateDTO dto) {
        AccessRequestResponseDTO response = accessRequestService.createRequest(userId, dto);
        return ResponseEntity.ok(response);
    }

}
