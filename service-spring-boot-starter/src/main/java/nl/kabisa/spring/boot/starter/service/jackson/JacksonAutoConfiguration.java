package nl.kabisa.spring.boot.starter.service.jackson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.ZonedDateTimeSerializer;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * This configuration class configures Jackson.
 * <p>
 * You can override these settings using the 'spring.jackson.*' properties in the application.properties or
 * application.yaml configuration file.
 * You can completely disable this configuration with:
 * <p>
 * service.starter.jackson.enabled=false
 *
 * @author Mark Spreksel
 * @see org.springframework.boot.autoconfigure.jackson.JacksonProperties
 */
@Data
@AutoConfiguration
@ConfigurationProperties("service.starter.jackson")
@ConditionalOnClass(Jackson2ObjectMapperBuilder.class)
@ConditionalOnProperty(prefix = "service.starter.jackson", name = "enabled", havingValue = "true", matchIfMissing = true)
@Slf4j
class JacksonAutoConfiguration {

    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final String ZONED_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
        log.info("Configuring Jackson2ObjectMapperBuilderCustomizer");
        return new JacksonCustomizer();
    }

    private class JacksonCustomizer implements Jackson2ObjectMapperBuilderCustomizer, Ordered {

        @Override
        public int getOrder() {
            return -10; // run before standard Spring Boot customizer
        }

        @Override
        public void customize(Jackson2ObjectMapperBuilder builder) {
            builder.simpleDateFormat(DATE_TIME_FORMAT);
            builder.serializers(new LocalDateSerializer(DateTimeFormatter.ofPattern(DATE_FORMAT)));
            builder.serializers(new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)));
            builder.serializers(new ZonedDateTimeSerializer(DateTimeFormatter.ofPattern(ZONED_DATE_TIME_FORMAT).withZone(ZoneOffset.UTC)));
            builder.indentOutput(true);
            builder.serializationInclusion(JsonInclude.Include.NON_EMPTY);
            builder.failOnUnknownProperties(false);
            builder.featuresToEnable(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS);
            builder.featuresToDisable(MapperFeature.USE_GETTERS_AS_SETTERS);
            builder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); //For Date and Calendar
        }

    }

}
