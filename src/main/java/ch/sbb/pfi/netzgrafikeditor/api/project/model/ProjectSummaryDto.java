package ch.sbb.pfi.netzgrafikeditor.api.project.model;

import ch.sbb.pfi.netzgrafikeditor.api.common.model.ProjectId;

import jakarta.validation.constraints.NotNull;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class ProjectSummaryDto {
    @NonNull @NotNull ProjectId id;

    @NonNull @NotNull String name;

    @NotNull @NonNull Boolean isArchived;
}
