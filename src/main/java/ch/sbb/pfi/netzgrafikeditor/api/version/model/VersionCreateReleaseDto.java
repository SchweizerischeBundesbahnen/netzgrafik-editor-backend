package ch.sbb.pfi.netzgrafikeditor.api.version.model;

import jakarta.validation.constraints.NotNull;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class VersionCreateReleaseDto {

    @NonNull @NotNull String comment;
}
