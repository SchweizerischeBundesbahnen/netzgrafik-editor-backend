package ch.sbb.pfi.netzgrafikeditor.api.variant.model;

import ch.sbb.pfi.netzgrafikeditor.api.common.model.ProjectId;
import ch.sbb.pfi.netzgrafikeditor.api.common.model.VariantId;
import ch.sbb.pfi.netzgrafikeditor.api.version.model.VersionDto;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.Optional;

@Value
@Builder
@Jacksonized
public class VariantSummaryDto {
    @NonNull @NotNull VariantId id;

    @NonNull @NotNull ProjectId projectId;

    @NonNull @NotNull Boolean isArchived;

    @Builder.Default
    @Schema(nullable = true, required = true)
    Optional<VersionDto> latestReleaseVersion = Optional.empty();

    @Builder.Default
    @Schema(nullable = true, required = true)
    Optional<VersionDto> latestSnapshotVersion = Optional.empty();
}
