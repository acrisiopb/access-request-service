package com.acrisio.accesscontrol.domain.rules;

import com.acrisio.accesscontrol.api.dto.AccessRequestCreateDTO;
import com.acrisio.accesscontrol.domain.model.Access;
import com.acrisio.accesscontrol.domain.model.Module;
import com.acrisio.accesscontrol.domain.model.User;
import com.acrisio.accesscontrol.infrastructure.util.InternationalizationUtil;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Set;

@Component
public class DuplicateActiveAccessRule implements AccessRequestRule {
    private InternationalizationUtil message;
    @Override
    public void validate(User user, Set<Module> requestedModules, AccessRequestCreateDTO dto) {

        if (user.getActiveAccesses() == null) {
            return;
        }

        OffsetDateTime now = OffsetDateTime.now();

        for (Module requested : requestedModules) {
            for (Access access : user.getActiveAccesses()) {

                boolean sameModule =
                        access.getModule().getId().equals(requested.getId());

                boolean notExpired =
                        access.getExpiresAt() != null && access.getExpiresAt().isAfter(now);

                if (sameModule && notExpired) {
                    throw new IllegalArgumentException(
                            message.getMessage("rule.duplicateActiveAccessRule.info") + " " + requested.getName()
                    );
                }
            }
        }
    }
}
