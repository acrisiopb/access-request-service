package com.acrisio.accesscontrol.api.controller;

import com.acrisio.accesscontrol.api.dto.ModuleDTO;
import com.acrisio.accesscontrol.service.ModuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/modules")
@RequiredArgsConstructor
public class ModuleController {

    private final ModuleService moduleService;

    @PostMapping
    public ResponseEntity<ModuleDTO> create(@Valid @RequestBody ModuleDTO dto) {
        return ResponseEntity.ok(moduleService.create(dto));
    }

    @GetMapping
    public ResponseEntity<List<ModuleDTO>> listAll() {
        return ResponseEntity.ok(moduleService.listAll());
    }

    @PostMapping("/find")
    public ResponseEntity<ModuleDTO> find(@RequestBody ModuleDTO dto) {
        return ResponseEntity.ok(moduleService.findById(dto.id()));
    }

    @PutMapping
    public ResponseEntity<ModuleDTO> update(@Valid @RequestBody ModuleDTO dto) {
        return ResponseEntity.ok(moduleService.update(dto));
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(@RequestBody ModuleDTO dto) {
        moduleService.delete(dto.id());
        return ResponseEntity.noContent().build();
    }

}
