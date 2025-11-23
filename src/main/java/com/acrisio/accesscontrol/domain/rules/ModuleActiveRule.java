package com.acrisio.accesscontrol.domain.rules;

import com.acrisio.accesscontrol.api.dto.AccessRequestCreateDTO;
import com.acrisio.accesscontrol.domain.model.Module;
import com.acrisio.accesscontrol.domain.model.User;
import com.acrisio.accesscontrol.infrastructure.util.InternationalizationUtil;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class ModuleActiveRule implements AccessRequestRule {
    private InternationalizationUtil message;

    @Override
    public void validate(User user, Set<Module> requestedModules, AccessRequestCreateDTO dto) {
        for (Module module : requestedModules) {
            if (!module.getActive()) {
                throw new IllegalArgumentException(message.getMessage("rule.moduleActiveRule.info") + " " + module.getName()
                );
            }
        }
    }
}
