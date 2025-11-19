package com.acrisio.accesscontrol.domain.rules;

import com.acrisio.accesscontrol.api.dto.AccessRequestCreateDTO;
import com.acrisio.accesscontrol.domain.model.Module;
import com.acrisio.accesscontrol.domain.model.User;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class ModuleCompatibilityRule implements AccessRequestRule {

    @Override
    public void validate(User user, Set<Module> requestedModules, AccessRequestCreateDTO dto) {

        Set<Module> active = user.getActiveModules();

        for (Module requested : requestedModules) {
            for (Module activeModule : active) {
                if (requested.getIncompatibleModules().contains(activeModule)) {
                    throw new IllegalArgumentException(
                            "Module " + requested.getName() +
                                    " is incompatible with active module " + activeModule.getName()
                    );
                }
            }
        }
    }
}
