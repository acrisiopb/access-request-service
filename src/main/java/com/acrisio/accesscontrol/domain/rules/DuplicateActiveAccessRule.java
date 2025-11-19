package com.acrisio.accesscontrol.domain.rules;

import com.acrisio.accesscontrol.api.dto.AccessRequestCreateDTO;
import com.acrisio.accesscontrol.domain.model.Module;
import com.acrisio.accesscontrol.domain.model.User;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class DuplicateActiveAccessRule implements AccessRequestRule {

    @Override
    public void validate(User user, Set<Module> requestedModules, AccessRequestCreateDTO dto) {

        Set<Module> active = user.getActiveModules();

        for (Module m : requestedModules) {
            if (active.contains(m)) {
                throw new IllegalArgumentException(
                        "You already have access to module: " + m.getName()
                );
            }
        }
    }
}
