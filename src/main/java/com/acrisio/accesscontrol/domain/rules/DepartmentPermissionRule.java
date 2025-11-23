package com.acrisio.accesscontrol.domain.rules;

import com.acrisio.accesscontrol.api.dto.AccessRequestCreateDTO;
import com.acrisio.accesscontrol.domain.enums.Department;
import com.acrisio.accesscontrol.domain.model.Module;
import com.acrisio.accesscontrol.domain.model.User;
import com.acrisio.accesscontrol.infrastructure.util.InternationalizationUtil;
import org.springframework.stereotype.Component;

import java.util.Set;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class DepartmentPermissionRule implements AccessRequestRule {

    private final InternationalizationUtil message;
    @Override
    public void validate(User user, Set<Module> requestedModules, AccessRequestCreateDTO dto) {

        Department dept = user.getDepartment();

        for (Module module : requestedModules) {

            String code = module.getName().toUpperCase();

            // TI → todos os módulos
            if (dept == Department.TI) continue;

            // Financeiro
            if (dept == Department.FINANCE) {
                if (code.equals("GESTAO_FINANCEIRA") ||
                        code.equals("APROVADOR_FINANCEIRO") ||
                        code.equals("SOLICITANTE_FINANCEIRO") ||
                        code.equals("RELATORIOS") ||
                        code.equals("PORTAL")) {
                    continue;
                }
                throw new IllegalArgumentException( message.getMessage("rule.departmentPermissionRule.info") + " " + module.getName());
            }

            // RH
            if (dept == Department.RH) {
                if (code.equals("ADMINISTRADOR_RH") ||
                        code.equals("COLABORADOR_RH") ||
                        code.equals("RELATORIOS") ||
                        code.equals("PORTAL")) {
                    continue;
                }
                throw new IllegalArgumentException( message.getMessage("rule.departmentPermissionRule.info") + " " + module.getName());
            }

            // Operações
            if (dept == Department.OPERATIONS) {
                if (code.equals("ESTOQUE") ||
                        code.equals("COMPRAS") ||
                        code.equals("RELATORIOS") ||
                        code.equals("PORTAL")) {
                    continue;
                }
                throw new IllegalArgumentException( message.getMessage("rule.departmentPermissionRule.info") + " " + module.getName());
            }

            // Outros departamentos
            if (code.equals("PORTAL") || code.equals("RELATORIOS")) {
                continue;
            }

            throw new IllegalArgumentException( message.getMessage("rule.departmentPermissionRule.info") + " " + module.getName());
        }
    }
}
