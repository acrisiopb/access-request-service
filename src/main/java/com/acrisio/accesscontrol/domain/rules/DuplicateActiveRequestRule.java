package com.acrisio.accesscontrol.domain.rules;

import com.acrisio.accesscontrol.api.dto.AccessRequestCreateDTO;
import com.acrisio.accesscontrol.domain.model.Module;
import com.acrisio.accesscontrol.domain.model.User;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class DuplicateActiveRequestRule implements AccessRequestRule {

    @Override
    public void validate(User user, Set<Module> requestedModules, AccessRequestCreateDTO dto) {

        Set<Module> requestedBefore = user.getRequestedModules();

        for (Module m : requestedModules) {
            if (requestedBefore.contains(m)) {
                throw new IllegalArgumentException(
                        "You already have an active request for: " + m.getName()
                );
            }
        }
    }
}
