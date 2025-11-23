package infrastructure.util;

import com.acrisio.accesscontrol.api.dto.AccessRequestFilterDTO;
import com.acrisio.accesscontrol.domain.enums.RequestStatus;
import com.acrisio.accesscontrol.domain.model.AccessRequest;
import com.acrisio.accesscontrol.domain.model.Module;
import com.acrisio.accesscontrol.infrastructure.util.AccessRequestSpecification;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Expression;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccessRequestSpecificationTests {

    @Mock
    private Root<AccessRequest> root;
    @Mock
    private CriteriaQuery<?> query;
    @Mock
    private CriteriaBuilder cb;
    @Mock
    private Join<AccessRequest, Module> moduleJoin;

    @BeforeEach
    void setUp() {
        doReturn(moduleJoin).when(root).join(eq("modules"), eq(jakarta.persistence.criteria.JoinType.LEFT));

        jakarta.persistence.criteria.Predicate base = mock(jakarta.persistence.criteria.Predicate.class);
        when(cb.conjunction()).thenReturn(base);
    }

    @Test
    void filter_NoFilters_CreatesBaseSpecification() {
        AccessRequestFilterDTO filter = new AccessRequestFilterDTO(null, null, null, null, null);

        Specification<AccessRequest> spec = AccessRequestSpecification.filter(filter);
        spec.toPredicate(root, query, cb);

        verify(root, times(1)).join(eq("modules"), eq(jakarta.persistence.criteria.JoinType.LEFT));
        verify(cb, times(1)).conjunction();
        verifyNoMoreInteractions(cb);
    }

    @Test
    void filter_WithSearchTerm_AddsLikePredicates() {
        String searchTerm = "financeiro";
        AccessRequestFilterDTO filter = new AccessRequestFilterDTO(searchTerm, null, null, null, null);

        Path protocolPath = mock(Path.class);
        Path moduleNamePath = mock(Path.class);
        when(root.get(eq("protocol"))).thenReturn(protocolPath);
        when(moduleJoin.get(eq("name"))).thenReturn(moduleNamePath);

        Expression<String> protocolLower = mock(Expression.class);
        Expression<String> moduleLower = mock(Expression.class);
        when(cb.lower(eq(protocolPath))).thenReturn(protocolLower);
        when(cb.lower(eq(moduleNamePath))).thenReturn(moduleLower);

        Specification<AccessRequest> spec = AccessRequestSpecification.filter(filter);
        spec.toPredicate(root, query, cb);

        verify(cb, times(1)).like(eq(protocolLower), eq("%financeiro%"));
        verify(cb, times(1)).like(eq(moduleLower), eq("%financeiro%"));
    }

    @Test
    void filter_WithStatusAndUrgent_AddsEqualPredicates() {
        AccessRequestFilterDTO filter = new AccessRequestFilterDTO(null, RequestStatus.ACTIVE, true, null, null);

        Path statusPath = mock(Path.class);
        Path urgentPath = mock(Path.class);
        when(root.get(eq("status"))).thenReturn(statusPath);
        when(root.get(eq("urgent"))).thenReturn(urgentPath);

        Specification<AccessRequest> spec = AccessRequestSpecification.filter(filter);
        spec.toPredicate(root, query, cb);

        verify(cb, times(1)).equal(eq(statusPath), eq(RequestStatus.ACTIVE));
        verify(cb, times(1)).equal(eq(urgentPath), eq(true));
    }

    @Test
    void filter_WithStartAndEndDate_AddsDateRangePredicates() {
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 1, 31);
        AccessRequestFilterDTO filter = new AccessRequestFilterDTO(null, null, null, startDate, endDate);

        Path createdAtPath = mock(Path.class);
        when(root.get(eq("createdAt"))).thenReturn(createdAtPath);


        Specification<AccessRequest> spec = AccessRequestSpecification.filter(filter);
        spec.toPredicate(root, query, cb);

        OffsetDateTime expectedStart = startDate.atStartOfDay().atOffset(OffsetDateTime.now().getOffset());
        OffsetDateTime expectedEnd = endDate.plusDays(1).atStartOfDay().atOffset(OffsetDateTime.now().getOffset());
        verify(cb, times(1)).greaterThanOrEqualTo(eq(createdAtPath), eq(expectedStart));
        verify(cb, times(1)).lessThanOrEqualTo(eq(createdAtPath), eq(expectedEnd));
    }
}