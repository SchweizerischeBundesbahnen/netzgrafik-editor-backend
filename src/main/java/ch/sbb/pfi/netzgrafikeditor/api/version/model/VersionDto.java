package ch.sbb.pfi.netzgrafikeditor.api.version.model;

import ch.sbb.pfi.netzgrafikeditor.api.common.model.UserId;
import ch.sbb.pfi.netzgrafikeditor.api.common.model.VariantId;
import ch.sbb.pfi.netzgrafikeditor.api.common.model.VersionId;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDateTime;
import java.util.Optional;

@Value
@Builder
@Jacksonized
public class VersionDto {
    @NonNull @NotNull VersionId id;

    @NonNull @NotNull VariantId variantId;

    @NonNull @NotNull Integer releaseVersion;

    @Builder.Default
    @Schema(nullable = true, required = true)
    Optional<Integer> snapshotVersion = Optional.empty();

    @NonNull @NotNull String name;

    @NonNull @NotNull String comment;

    @NonNull @NotNull LocalDateTime createdAt;

    @NonNull @NotNull UserId createdBy;
}
