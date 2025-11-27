package com.acrisio.accesscontrol.infrastructure.init;

import com.acrisio.accesscontrol.domain.enums.Department;
import com.acrisio.accesscontrol.domain.model.Access;
import com.acrisio.accesscontrol.domain.model.Module;
import com.acrisio.accesscontrol.domain.model.User;
import com.acrisio.accesscontrol.domain.repository.AccessRepositoy;
import com.acrisio.accesscontrol.domain.repository.ModuleRepository;
import com.acrisio.accesscontrol.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;

@Component
@Profile("seed")
@RequiredArgsConstructor
public class DatabaseSeeder implements ApplicationRunner {

    private final ModuleRepository moduleRepository;
    private final UserRepository userRepository;
    private final AccessRepositoy accessRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (moduleRepository.count() == 0) {
            Map<String, Module> modules = seedModules();
            seedIncompatibilities(modules);
        }
        if (userRepository.count() == 0) {
            seedUsers();
        }
        if (accessRepository.count() == 0) {
            seedAccesses();
        }
    }

    private Map<String, Module> seedModules() {
        List<Module> list = new ArrayList<>();
        list.add(module("PORTAL", "Acesso geral ao portal", true, Set.of(Department.TI, Department.FINANCE, Department.RH, Department.OPERATIONS, Department.OTHER)));
        list.add(module("RELATORIOS", "Acesso a relatórios corporativos", true, Set.of(Department.TI, Department.FINANCE, Department.RH, Department.OPERATIONS, Department.OTHER)));
        list.add(module("GESTAO_FINANCEIRA", "Módulo financeiro", true, Set.of(Department.TI, Department.FINANCE)));
        list.add(module("APROVADOR_FINANCEIRO", "Aprovação de finanças", true, Set.of(Department.TI, Department.FINANCE)));
        list.add(module("SOLICITANTE_FINANCEIRO", "Solicitação de recursos", true, Set.of(Department.TI, Department.FINANCE)));
        list.add(module("ADMINISTRADOR_RH", "Administração de recursos humanos", true, Set.of(Department.TI, Department.RH)));
        list.add(module("COLABORADOR_RH", "Funcionalidades básicas de RH", true, Set.of(Department.TI, Department.RH)));
        list.add(module("ESTOQUE", "Controle de estoque", true, Set.of(Department.TI, Department.OPERATIONS)));
        list.add(module("COMPRAS", "Módulo compras", true, Set.of(Department.TI, Department.OPERATIONS)));
        list.add(module("AUDITORIA", "Acesso auditoria", true, Set.of(Department.TI)));
        moduleRepository.saveAll(list);
        Map<String, Module> map = new HashMap<>();
        for (Module m : list) {
            map.put(m.getName(), m);
        }
        return map;
    }

    private void seedIncompatibilities(Map<String, Module> modules) {
        Module aprovador = modules.get("APROVADOR_FINANCEIRO");
        Module solicitante = modules.get("SOLICITANTE_FINANCEIRO");
        Module adminRh = modules.get("ADMINISTRADOR_RH");
        Module colaboradorRh = modules.get("COLABORADOR_RH");
        aprovador.getIncompatibleModules().add(solicitante);
        solicitante.getIncompatibleModules().add(aprovador);
        adminRh.getIncompatibleModules().add(colaboradorRh);
        colaboradorRh.getIncompatibleModules().add(adminRh);
        moduleRepository.saveAll(List.of(aprovador, solicitante, adminRh, colaboradorRh));
    }

    private void seedUsers() {
        List<User> users = new ArrayList<>();
        users.add(user("Alice Dev", "alice@corp.com", Department.TI, passwordEncoder.encode("alice123")));
        users.add(user("Bruno Finance", "bruno@corp.com", Department.FINANCE, passwordEncoder.encode("bruno123")));
        users.add(user("Carla RH", "carla@corp.com", Department.RH, passwordEncoder.encode("carla123")));
        users.add(user("Diego Ops", "diego@corp.com", Department.OPERATIONS, passwordEncoder.encode("diego123")));
        users.add(user("Eva Other", "eva@corp.com", Department.OTHER, passwordEncoder.encode("eva123")));
        users.add(user("Test Admin", "test@admin.com", Department.TI, passwordEncoder.encode("test123")));
        userRepository.saveAll(users);
    }

    private void seedAccesses() {
        Optional<User> alice = userRepository.findByEmail("alice@corp.com");
        Optional<User> bruno = userRepository.findByEmail("bruno@corp.com");
        Optional<User> carla = userRepository.findByEmail("carla@corp.com");
        Optional<User> diego = userRepository.findByEmail("diego@corp.com");
        Optional<User> eva = userRepository.findByEmail("eva@corp.com");
        Optional<User> testAdmin = userRepository.findByEmail("test@admin.com");

        Map<String, Module> modules = new HashMap<>();
        for (Module m : moduleRepository.findAll()) {
            modules.put(m.getName(), m);
        }

        List<Access> accesses = new ArrayList<>();
        OffsetDateTime now = OffsetDateTime.now();
        bruno.ifPresent(u -> accesses.add(access(u, modules.get("APROVADOR_FINANCEIRO"), now.plusDays(180))));
        carla.ifPresent(u -> accesses.add(access(u, modules.get("ADMINISTRADOR_RH"), now.plusDays(180))));
        diego.ifPresent(u -> {
            accesses.add(access(u, modules.get("ESTOQUE"), now.plusDays(180)));
            accesses.add(access(u, modules.get("COMPRAS"), now.plusDays(180)));
        });
        alice.ifPresent(u -> accesses.add(access(u, modules.get("RELATORIOS"), now.plusDays(180))));
        eva.ifPresent(u -> accesses.add(access(u, modules.get("RELATORIOS"), now.plusDays(180))));
        testAdmin.ifPresent(u -> {
            accesses.add(access(u, modules.get("RELATORIOS"), now.plusDays(180)));
            accesses.add(access(u, modules.get("COLABORADOR_RH"), now.plusDays(180)));
            accesses.add(access(u, modules.get("GESTAO_FINANCEIRA"), now.plusDays(15)));
        });
        accessRepository.saveAll(accesses);
    }

    private Module module(String name, String description, Boolean active, Set<Department> departments) {
        Module m = new Module();
        m.setName(name);
        m.setDescription(description);
        m.setActive(active);
        m.setPermittedDepartments(new HashSet<>(departments));
        m.setIncompatibleModules(new HashSet<>());
        return m;
    }

    private User user(String name, String email, Department department, String passwordHash) {
        User u = new User();
        u.setName(name);
        u.setEmail(email);
        u.setDepartment(department);
        u.setPasswordHash(passwordHash);
        return u;
    }

    private Access access(User user, Module module, OffsetDateTime expiresAt) {
        Access a = new Access();
        a.setUser(user);
        a.setModule(module);
        a.setGrantedAt(OffsetDateTime.now());
        a.setExpiresAt(expiresAt);
        return a;
    }
}
