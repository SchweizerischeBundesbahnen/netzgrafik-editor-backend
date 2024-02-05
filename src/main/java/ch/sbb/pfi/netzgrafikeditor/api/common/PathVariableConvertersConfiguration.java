package ch.sbb.pfi.netzgrafikeditor.api.common;

import ch.sbb.pfi.netzgrafikeditor.api.common.model.ProjectId;
import ch.sbb.pfi.netzgrafikeditor.api.common.model.VariantId;
import ch.sbb.pfi.netzgrafikeditor.api.common.model.VersionId;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;

@Configuration
public class PathVariableConvertersConfiguration {

    @Bean
    public LongToProjectIdConverter projectIdConverter() {
        return source -> ProjectId.of(Long.parseLong(source));
    }

    @Bean
    public LongToVariantIdConverter variantIdConverter() {
        return source -> VariantId.of(Long.parseLong(source));
    }

    @Bean
    public LongToVersionIdConverter versionIdConverter() {
        return source -> VersionId.of(Long.parseLong(source));
    }

    // Otherwise spring can not determine types (type eraser)
    interface LongToProjectIdConverter extends Converter<String, ProjectId> {}

    interface LongToVariantIdConverter extends Converter<String, VariantId> {}

    interface LongToVersionIdConverter extends Converter<String, VersionId> {}
}
