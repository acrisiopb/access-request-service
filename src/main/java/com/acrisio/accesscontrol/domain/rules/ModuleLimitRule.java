package com.acrisio.accesscontrol.domain.rules;

import com.acrisio.accesscontrol.api.dto.AccessRequestCreateDTO;
import com.acrisio.accesscontrol.domain.enums.Department;
import com.acrisio.accesscontrol.domain.model.Module;
import com.acrisio.accesscontrol.domain.model.User;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class ModuleLimitRule implements AccessRequestRule {

    @Override
    public void validate(User user, Set<Module> requestedModules, AccessRequestCreateDTO dto) {

        int activeCount = user.getActiveAccesses().size();
        int limit = user.getDepartment() == Department.TI ? 10 : 5;

        if (activeCount + requestedModules.size() > limit) {
            throw new IllegalArgumentException("You exceeded the module access limit");
        }
    }
}
