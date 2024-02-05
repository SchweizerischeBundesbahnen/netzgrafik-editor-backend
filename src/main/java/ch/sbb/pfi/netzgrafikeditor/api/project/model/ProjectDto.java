package ch.sbb.pfi.netzgrafikeditor.api.project.model;

import ch.sbb.pfi.netzgrafikeditor.api.common.model.ProjectId;
import ch.sbb.pfi.netzgrafikeditor.api.common.model.UserId;
import ch.sbb.pfi.netzgrafikeditor.api.variant.model.VariantSummaryDto;

import jakarta.validation.constraints.NotNull;

import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;
import lombok.With;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Value
@Builder
@With
@Jacksonized
public class ProjectDto {
    @NonNull @NotNull ProjectId id;

    @NonNull @NotNull String name;

    @NonNull @NotNull LocalDateTime createdAt;

    @NonNull @NotNull UserId createdBy;

    @NonNull @NotNull String summary;

    @NonNull @NotNull String description;

    @NonNull @NotNull @Singular Collection<VariantSummaryDto> variants;

    @NotNull @NonNull List<String> writeUsers;

    @NotNull @NonNull List<String> readUsers;

    @NotNull @NonNull Boolean isArchived;

    @NonNull @NotNull Boolean isWritable;

    @NonNull @NotNull Boolean isDeletable;
}
