package com.acrisio.accesscontrol.domain.rules;

import com.acrisio.accesscontrol.api.dto.AccessRequestCreateDTO;
import com.acrisio.accesscontrol.domain.model.Module;
import com.acrisio.accesscontrol.domain.model.User;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class DepartmentPermissionRule implements AccessRequestRule {

    @Override
    public void validate(User user, Set<Module> requestedModules, AccessRequestCreateDTO dto) {
        for (Module m : requestedModules) {
            if (!m.getPermittedDepartments().contains(user.getDepartment())) {
                throw new IllegalArgumentException(
                        "Your department is not allowed to access module: " + m.getName()
                );
            }
        }
    }
}
