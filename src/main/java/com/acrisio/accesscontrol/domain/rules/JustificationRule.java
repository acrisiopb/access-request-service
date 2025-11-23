package com.acrisio.accesscontrol.domain.rules;

import com.acrisio.accesscontrol.api.dto.AccessRequestCreateDTO;
import com.acrisio.accesscontrol.domain.model.Module;
import com.acrisio.accesscontrol.domain.model.User;
import com.acrisio.accesscontrol.infrastructure.util.InternationalizationUtil;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import java.text.Normalizer;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Component
public class JustificationRule implements AccessRequestRule {
    private final InternationalizationUtil message;

    private static final List<String> GENERIC_WORDS = List.of(
            "teste", "testando", "aaa", "aaaa", "aaaaa", "preciso", "favor liberar",
            "ok", "libera", "liberação", "kkk", "kkkk", "kkkkkk"
    );

    @Override
    public void validate(User user, Set<Module> requestedModules, AccessRequestCreateDTO dto) {
        String justification = dto.justification();

        if (justification == null) {
            throw new IllegalArgumentException(message.getMessage("rule.justificationRule.info"));
        }

        String clean = normalize(justification);

        if (clean.length() < 20 || clean.length() > 500) {
            throw new IllegalArgumentException(message.getMessage("rule.justificationRule.infoII"));
        }

        // Lista negra de genéricos
        for (String word : GENERIC_WORDS) {
            if (clean.equals(word) || clean.contains(word)) {
                throw new IllegalArgumentException(message.getMessage("rule.justificationRule.infoIII"));
        }
        }

        // repetição exagerada de caracteres (ex: aaaaaaaa, kkkkkkkkk)
        if (clean.matches("^(.)\\1{4,}$")) {
            throw new IllegalArgumentException(message.getMessage("rule.justificationRule.infoIII"));
        }

        // Só uma palavra muito curta -> genérico
        if (clean.split(" ").length == 1 && clean.length() <= 10) {
            throw new IllegalArgumentException(message.getMessage("rule.justificationRule.infoIII"));
        }
    }

    private String normalize(String text) {
        return Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "")
                .trim()
                .toLowerCase();
    }
}
