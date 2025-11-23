package com.acrisio.accesscontrol.domain.rules;

import com.acrisio.accesscontrol.api.dto.AccessRequestCreateDTO;
import com.acrisio.accesscontrol.domain.enums.RequestStatus;
import com.acrisio.accesscontrol.domain.model.AccessRequest;
import com.acrisio.accesscontrol.domain.model.Module;
import com.acrisio.accesscontrol.domain.model.User;
import com.acrisio.accesscontrol.domain.enums.RequestStatus;
import com.acrisio.accesscontrol.domain.repository.AccessRequestRepository;
import com.acrisio.accesscontrol.infrastructure.util.InternationalizationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class DuplicateActiveRequestRule implements AccessRequestRule {

    private final AccessRequestRepository accessRequestRepository;
    private final InternationalizationUtil message;
    
    @Override
    public void validate(User user, Set<Module> requestedModules, AccessRequestCreateDTO dto) {

        var existing = accessRequestRepository.findByUser(user);
        for (Module requested : requestedModules) {
            boolean conflict = existing.stream()
                    .anyMatch(req -> req.getStatus() == RequestStatus.ACTIVE &&
                            req.getModules().stream().anyMatch(m -> m.getId().equals(requested.getId())));
            if (conflict) {
                throw new IllegalArgumentException(
                        message.getMessage("rule.duplicateActiveRequestRule.info") + " " + requested.getName()
                );
            }
        }
    }
}
