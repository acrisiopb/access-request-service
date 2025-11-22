package com.acrisio.accesscontrol.infrastructure.util;

import com.acrisio.accesscontrol.api.dto.AccessRequestFilterDTO;
import com.acrisio.accesscontrol.domain.model.AccessRequest;
import com.acrisio.accesscontrol.domain.model.Module;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

import java.time.OffsetDateTime;

public class AccessRequestSpecification {

    public static Specification<AccessRequest> filter(AccessRequestFilterDTO f) {
        return (root, query, cb) -> {

            // JOIN para módulos (módulo contém nome e descrição)
            Join<AccessRequest, Module> moduleJoin = root.join("modules", jakarta.persistence.criteria.JoinType.LEFT);

            var predicates = cb.conjunction();

            if (f.search() != null && !f.search().isBlank()) {
                String like = "%" + f.search().toLowerCase() + "%";
                predicates.getExpressions().add(
                        cb.or(
                                cb.like(cb.lower(root.get("protocol")), like),
                                cb.like(cb.lower(moduleJoin.get("name")), like)
                        )
                );
            }

            if (f.status() != null) {
                predicates.getExpressions().add(
                        cb.equal(root.get("status"), f.status())
                );
            }

            if (f.urgent() != null) {
                predicates.getExpressions().add(
                        cb.equal(root.get("urgent"), f.urgent())
                );
            }

            if (f.startDate() != null) {
                predicates.getExpressions().add(
                        cb.greaterThanOrEqualTo(
                                root.get("createdAt"),
                                f.startDate().atStartOfDay().atOffset(OffsetDateTime.now().getOffset())
                        )
                );
            }

            if (f.endDate() != null) {
                predicates.getExpressions().add(
                        cb.lessThanOrEqualTo(
                                root.get("createdAt"),
                                f.endDate().plusDays(1).atStartOfDay().atOffset(OffsetDateTime.now().getOffset())
                        )
                );
            }

            return predicates;
        };
    }
}
