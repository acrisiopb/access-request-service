package com.acrisio.accesscontrol.domain.rules;

import com.acrisio.accesscontrol.api.dto.AccessRequestCreateDTO;
import com.acrisio.accesscontrol.domain.model.Module;
import com.acrisio.accesscontrol.domain.model.User;

import java.util.Set;

public interface AccessRequestRule {
    void validate(User user, Set<Module> requestedModules, AccessRequestCreateDTO dto);
}
