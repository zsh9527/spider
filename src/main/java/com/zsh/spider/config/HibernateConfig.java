package com.zsh.spider.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.type.jackson.JacksonJsonFormatMapper;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * HibernateConfig
 *
 * @author zsh
 * @version 1.0.0
 * @date 2024/03/12 10:00
 */
@Configuration
@RequiredArgsConstructor
public class HibernateConfig {

    private final ObjectMapper objectMapper;

    @Bean
    public HibernatePropertiesCustomizer jsonFormatMapperCustomizer(){
        return properties -> properties.put(
            AvailableSettings.JSON_FORMAT_MAPPER,
            new JacksonJsonFormatMapper(objectMapper)
        );
    }
}
