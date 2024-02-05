package ch.sbb.pfi.netzgrafikeditor.api.variant.model;

import jakarta.validation.constraints.NotNull;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class VariantCreateFromVersionDto {
    @NonNull @NotNull String name;
}
