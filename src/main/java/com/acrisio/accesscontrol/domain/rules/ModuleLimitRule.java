package com.acrisio.accesscontrol.domain.rules;

import com.acrisio.accesscontrol.api.dto.AccessRequestCreateDTO;
import com.acrisio.accesscontrol.domain.enums.Department;
import com.acrisio.accesscontrol.domain.model.Module;
import com.acrisio.accesscontrol.domain.model.User;
import com.acrisio.accesscontrol.infrastructure.util.InternationalizationUtil;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

import java.util.Set;
@RequiredArgsConstructor
@Component
public class ModuleLimitRule implements AccessRequestRule {
    private final InternationalizationUtil message;
    @Override
    public void validate(User user, Set<Module> requestedModules, AccessRequestCreateDTO dto) {

        int activeCount = user.getActiveAccesses() != null
                ? user.getActiveAccesses().size()
                : 0;

        int limit = (user.getDepartment() == Department.TI) ? 10 : 5;

        if (activeCount + requestedModules.size() > limit) {
            throw new IllegalArgumentException(
                   message.getMessage("rule.moduleLimitRule.info") + "  " + user.getDepartment()
                            +  message.getMessage("rule.moduleLimitRule.infoII") + " " + limit + " " +
                           message.getMessage("rule.moduleCompatibilityRule.infoI")
                    );
        }
    }
}
