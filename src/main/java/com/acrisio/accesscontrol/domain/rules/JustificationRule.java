package com.acrisio.accesscontrol.domain.rules;

import com.acrisio.accesscontrol.api.dto.AccessRequestCreateDTO;
import com.acrisio.accesscontrol.domain.model.Module;
import com.acrisio.accesscontrol.domain.model.User;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.List;
import java.util.Set;

@Component
public class JustificationRule implements AccessRequestRule {

    private static final List<String> GENERIC_WORDS = List.of(
            "teste", "testando", "aaa", "aaaa", "aaaaa", "preciso", "favor liberar",
            "ok", "libera", "liberação", "kkk", "kkkk", "kkkkkk"
    );

    @Override
    public void validate(User user, Set<Module> requestedModules, AccessRequestCreateDTO dto) {
        String justification = dto.justification();

        if (justification == null) {
            throw new IllegalArgumentException("Justificativa deve ser informada");
        }

        String clean = normalize(justification);

        if (clean.length() < 20 || clean.length() > 500) {
            throw new IllegalArgumentException("Justificativa deve ter entre 20 e 500 caracteres");
        }

        // Lista negra de genéricos
        for (String word : GENERIC_WORDS) {
            if (clean.equals(word) || clean.contains(word)) {
                throw new IllegalArgumentException("Justificativa genérica ou insuficiente");
        }
        }

        // repetição exagerada de caracteres (ex: aaaaaaaa, kkkkkkkkk)
        if (clean.matches("^(.)\\1{4,}$")) {
            throw new IllegalArgumentException("Justificativa genérica ou insuficiente");
        }

        // Só uma palavra muito curta -> genérico
        if (clean.split(" ").length == 1 && clean.length() <= 10) {
            throw new IllegalArgumentException("Justificativa genérica ou insuficiente");
        }
    }

    private String normalize(String text) {
        return Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "")
                .trim()
                .toLowerCase();
    }
}
