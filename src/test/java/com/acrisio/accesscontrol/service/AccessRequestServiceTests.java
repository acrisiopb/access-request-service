
package com.acrisio.accesscontrol.service;

import java.time.OffsetDateTime;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.acrisio.accesscontrol.api.dto.AccessRequestCreateDTO;
import com.acrisio.accesscontrol.api.dto.AccessRequestResponseDTO;
import com.acrisio.accesscontrol.domain.enums.RequestStatus;
import com.acrisio.accesscontrol.domain.model.Access;
import com.acrisio.accesscontrol.domain.model.Module;
import com.acrisio.accesscontrol.domain.model.User;
import com.acrisio.accesscontrol.domain.repository.AccessRepositoy;
import com.acrisio.accesscontrol.domain.repository.ModuleRepository;
import com.acrisio.accesscontrol.domain.repository.UserRepository;


@SpringBootTest
@org.springframework.test.context.ActiveProfiles("test")
@org.springframework.test.context.TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
        "spring.datasource.driver-class-name=org.h2.Driver"
})
@org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase(replace = org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class AccessRequestServiceIntegrationTests {

    @Autowired
    AccessRequestService service;
    @Autowired
    UserRepository userRepository;
    @Autowired
    ModuleRepository moduleRepository;
    @Autowired
    AccessRepositoy accessRepository;

    @Test
    void tiPodeAcessarTodos() {
        User user = userRepository.findById(1L).orElseThrow();
        Module mod = moduleRepository.findById(10L).orElseThrow();
        AccessRequestResponseDTO res = service.createRequest(new AccessRequestCreateDTO(user.getId(), List.of(mod.getId()), "Acesso necessário para auditoria do sistema conforme demanda corporativa.", true));
        Assertions.assertEquals(RequestStatus.ACTIVE, res.status());
    }

    @Test
    void financeiroNegadoParaEstoque() {
        User user = userRepository.findById(2L).orElseThrow();
        Module mod = moduleRepository.findById(8L).orElseThrow();
        AccessRequestResponseDTO res = service.createRequest(new AccessRequestCreateDTO(user.getId(), List.of(mod.getId()), "Necessidade operacional devidamente justificada para avaliação.", true));
        Assertions.assertEquals(RequestStatus.DENIED, res.status());
        Assertions.assertTrue(res.deniedReason().toLowerCase().contains("departamento"));
    }

    @Test
    void incompatibilidadeFinanceiro() {
        User user = userRepository.findById(2L).orElseThrow();
        Module mod = moduleRepository.findById(5L).orElseThrow();
        AccessRequestResponseDTO res = service.createRequest(new AccessRequestCreateDTO(user.getId(), List.of(mod.getId()), "Demanda funcional com escopo descrito e objetivos claros.", true));
        Assertions.assertEquals(RequestStatus.DENIED, res.status());
        Assertions.assertTrue(res.deniedReason().toLowerCase().contains("incompat"));
    }

    @Test
    void justificativaGenerica() {
        User user = userRepository.findById(5L).orElseThrow();
        Module mod = moduleRepository.findById(1L).orElseThrow();
        AccessRequestResponseDTO res = service.createRequest(new AccessRequestCreateDTO(user.getId(), List.of(mod.getId()), "teste", true));
        Assertions.assertEquals(RequestStatus.DENIED, res.status());
        Assertions.assertTrue(res.deniedReason().toLowerCase().contains("justificativa"));
    }

    @Test
    void jaPossuiAcessoAtivo() {
        User user = userRepository.findById(1L).orElseThrow();
        Module mod = moduleRepository.findById(2L).orElseThrow();
        AccessRequestResponseDTO res = service.createRequest(new AccessRequestCreateDTO(user.getId(), List.of(mod.getId()), "Solicitação válida para teste de duplicidade de acesso ativo ao módulo.", true));
        Assertions.assertEquals(RequestStatus.DENIED, res.status());
        Assertions.assertTrue(res.deniedReason().toLowerCase().contains("já possui acesso"));
    }

    @Test
 
   void jaPossuiSolicitacaoAtiva() {
        User user = userRepository.findById(5L).orElseThrow();
        Module mod = moduleRepository.findById(1L).orElseThrow();
        AccessRequestResponseDTO r1 = service.createRequest(new AccessRequestCreateDTO(user.getId(), List.of(mod.getId()), "Solicitação inicial com justificativa detalhada e objetivos específicos.", true));
        if (r1.status() == RequestStatus.DENIED) {
            Access access = Access.builder().user(user).module(mod).grantedAt(OffsetDateTime.now()).expiresAt(OffsetDateTime.now().plusDays(180)).build();
            accessRepository.save(access);
            r1 = service.createRequest(new AccessRequestCreateDTO(user.getId(), List.of(2L), "Solicitação alternativa válida.", true));
        }
        AccessRequestResponseDTO r2 = service.createRequest(new AccessRequestCreateDTO(user.getId(), List.of(mod.getId()), "Segunda solicitação para validar regra de duplicidade de solicitação ativa com detalhamento técnico.", true));
        Assertions.assertEquals(RequestStatus.DENIED, r2.status());
    }
}