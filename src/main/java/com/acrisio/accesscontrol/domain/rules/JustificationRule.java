package com.acrisio.accesscontrol.domain.rules;

import com.acrisio.accesscontrol.api.dto.AccessRequestCreateDTO;
import com.acrisio.accesscontrol.domain.model.User;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class JustificationRule implements AccessRequestRule {

    @Override
    public void validate(User user, Set<com.acrisio.accesscontrol.domain.model.Module> requestedModules, AccessRequestCreateDTO dto) {
        String justification = dto.justification();

        if (justification == null || justification.trim().length() < 20) {
            throw new IllegalArgumentException("Justification must be at least 20 characters");
        }

        // Regras contra texto genérico
        String lower = justification.trim().toLowerCase();
        if (lower.equals("teste") || lower.equals("aaa") || lower.equals("preciso")) {
            throw new IllegalArgumentException("Justification too generic");
        }
    }
}
