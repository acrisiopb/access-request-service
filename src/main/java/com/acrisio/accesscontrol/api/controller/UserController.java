package com.acrisio.accesscontrol.api.controller;

import com.acrisio.accesscontrol.api.dto.UserCreateDTO;
import com.acrisio.accesscontrol.api.dto.UserDTO;
import com.acrisio.accesscontrol.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserDTO> create(@Valid @RequestBody UserCreateDTO dto) {
        return ResponseEntity.ok(userService.create(dto));
    }

    @GetMapping
    public ResponseEntity<List<UserDTO>> findAll() {
        return ResponseEntity.ok(userService.findAll());
    }

    @PostMapping("/find")
    public ResponseEntity<UserDTO> find(@RequestBody UserDTO dto) {
        return ResponseEntity.ok(userService.findById(dto.id()));
    }

    @PutMapping
    public ResponseEntity<UserDTO> update(@Valid @RequestBody UserDTO dto) {
        return ResponseEntity.ok(userService.update(dto));
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(@RequestBody UserDTO dto) {
        userService.delete(dto.id());
        return ResponseEntity.noContent().build();
    }
}
