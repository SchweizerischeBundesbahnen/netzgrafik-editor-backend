package ch.sbb.pfi.netzgrafikeditor.api.variant.model;

import ch.sbb.pfi.netzgrafikeditor.api.common.model.ProjectId;
import ch.sbb.pfi.netzgrafikeditor.api.common.model.VariantId;
import ch.sbb.pfi.netzgrafikeditor.api.version.model.VersionDto;

import jakarta.validation.constraints.NotNull;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.With;
import lombok.extern.jackson.Jacksonized;

import java.util.Collection;

@Value
@Builder
@With
@Jacksonized
public class VariantDto {
    @NonNull @NotNull VariantId id;

    @NonNull @NotNull ProjectId projectId;

    @NonNull @NotNull Collection<VersionDto> versions;

    @NonNull @NotNull VersionDto latestVersion;

    @NotNull @NonNull Boolean isArchived;

    @NonNull @NotNull Boolean isWritable;

    @NonNull @NotNull Boolean isDeletable;
}
