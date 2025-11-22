package com.acrisio.accesscontrol.domain.rules;

import com.acrisio.accesscontrol.api.dto.AccessRequestCreateDTO;
import com.acrisio.accesscontrol.domain.enums.Department;
import com.acrisio.accesscontrol.domain.model.Module;
import com.acrisio.accesscontrol.domain.model.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

class RulesUnitTests {

    @Test
    void limiteNaoTiUltrapassado() {
        ModuleLimitRule rule = new ModuleLimitRule();
        User user = User.builder().id(100L).department(Department.FINANCE).accesses(java.util.stream.IntStream.range(0,5).mapToObj(i -> com.acrisio.accesscontrol.domain.model.Access.builder().expiresAt(java.time.OffsetDateTime.now().plusDays(180)).module(Module.builder().id((long) i).build()).build()).toList()).build();
        Module m = Module.builder().id(999L).build();
        Assertions.assertThrows(RuntimeException.class, () -> rule.validate(user, Set.of(m), new AccessRequestCreateDTO(user.getId(), List.of(m.getId()), "Justificativa válida para teste.", true)));
    }

    @Test
    void departamentoSemPermissao() {
        DepartmentPermissionRule rule = new DepartmentPermissionRule();
        User user = User.builder().id(101L).department(Department.RH).build();
        Module m = Module.builder().id(8L).name("ESTOQUE").build();
        Assertions.assertThrows(RuntimeException.class, () -> rule.validate(user, Set.of(m), new AccessRequestCreateDTO(user.getId(), List.of(m.getId()), "Justificativa válida para teste.", true)));
    }

    @Test
    void incompatibilidadeModulo() {
        ModuleCompatibilityRule rule = new ModuleCompatibilityRule();
        Module a = Module.builder().id(4L).name("APROVADOR_FINANCEIRO").build();
        Module b = Module.builder().id(5L).name("SOLICITANTE_FINANCEIRO").incompatibleModules(Set.of(a)).build();
        User user = User.builder().id(102L).department(Department.FINANCE).accesses(java.util.List.of(com.acrisio.accesscontrol.domain.model.Access.builder().module(a).expiresAt(java.time.OffsetDateTime.now().plusDays(180)).build())).build();
        Assertions.assertThrows(RuntimeException.class, () -> rule.validate(user, Set.of(b), new AccessRequestCreateDTO(user.getId(), List.of(b.getId()), "Justificativa válida para teste.", true)));
    }

    @Test
    void justificativaMuitoCurta() {
        JustificationRule rule = new JustificationRule();
        User user = User.builder().id(103L).department(Department.OTHER).build();
        Module m = Module.builder().id(1L).name("PORTAL").build();
        Assertions.assertThrows(RuntimeException.class, () -> rule.validate(user, Set.of(m), new AccessRequestCreateDTO(user.getId(), List.of(m.getId()), "teste", true)));
    }
}