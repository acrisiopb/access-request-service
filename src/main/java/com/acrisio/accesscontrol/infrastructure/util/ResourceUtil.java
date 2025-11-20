package com.acrisio.accesscontrol.infrastructure.util;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Classe utilitária para internacionalização de mensagens na aplicação.
 * Responsável por recuperar mensagens localizadas com base em códigos,
 * utilizando o locale
 * da requisição atual ou o locale padrão do sistema.
 */
@Component
public class ResourceUtil {

    @Autowired
    private ResourceBundleMessageSource messageSource;

    /**
     * Recupera uma mensagem internacionalizada com base em um código fornecido.
     * O locale é determinado pelo contexto da requisição HTTP atual, se disponível,
     * ou pelo locale padrão do sistema caso não haja uma requisição associada.
     *
     * O código da mensagem a ser recuperada, conforme definido nos
     * arquivos de recursos.
     * A mensagem localizada correspondente ao código fornecido, no idioma
     * apropriado.
     * Caso o código não seja encontrado, retorna uma string padrão conforme
     * configuração do ResourceBundleMessageSource.
     *
     * @see ResourceBundleMessageSource#getMessage(String, Object[], Locale)
     */
    public String getMessage(String code) {
        return getMessage(code, "");
    }

    public String getMessage(String code, String link) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        Locale locale = (attributes != null) ? attributes.getRequest().getLocale() : Locale.getDefault();
        return messageSource.getMessage(code, null, locale) + link;
    }
}