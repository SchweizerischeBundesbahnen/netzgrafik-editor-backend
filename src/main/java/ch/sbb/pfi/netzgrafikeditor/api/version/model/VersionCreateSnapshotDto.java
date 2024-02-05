package ch.sbb.pfi.netzgrafikeditor.api.version.model;

import jakarta.validation.constraints.NotNull;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class VersionCreateSnapshotDto {

    @NonNull @NotNull String name;

    @NonNull @NotNull String comment;

    @NonNull @NotNull String model;
}
