package com.acrisio.accesscontrol.api.controller;

import com.acrisio.accesscontrol.api.dto.AccessRequestCreateDTO;
import com.acrisio.accesscontrol.api.dto.AccessRequestIdDTO;
import com.acrisio.accesscontrol.api.dto.AccessRequestResponseDTO;
import com.acrisio.accesscontrol.service.AccessRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/request")
@RequiredArgsConstructor
public class AccessRequestController {

    private final AccessRequestService accessRequestService;

    @PostMapping
    public ResponseEntity<AccessRequestResponseDTO> create(
            @Valid @RequestBody AccessRequestCreateDTO dto) {

        return ResponseEntity.ok(accessRequestService.createRequest( dto)
        );
    }

    @PostMapping("/cancel")
    public ResponseEntity<AccessRequestResponseDTO> cancel(
            @RequestBody AccessRequestIdDTO dto) {

        return ResponseEntity.ok(
                accessRequestService.cancel(dto.id())
        );
    }

    @PostMapping("/renew")
    public ResponseEntity<AccessRequestResponseDTO> renew(
            @RequestBody AccessRequestIdDTO dto) {

        return ResponseEntity.ok(
                accessRequestService.renew(dto.id())
        );
    }

    @PostMapping("/find")
    public ResponseEntity<AccessRequestResponseDTO> find(
            @RequestBody AccessRequestIdDTO dto) {

        return ResponseEntity.ok(
                accessRequestService.findById(dto.id())
        );
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(@RequestBody AccessRequestIdDTO dto) {

        accessRequestService.delete(dto.id());
        return ResponseEntity.noContent().build();
    }
}
