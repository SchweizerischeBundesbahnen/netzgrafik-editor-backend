package ch.sbb.pfi.netzgrafikeditor.api.project.model;

import jakarta.validation.constraints.NotNull;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Value
@Builder
@Jacksonized
public class ProjectCreateUpdateDto {
    @NotNull @NonNull String name;

    @NotNull @NonNull String summary;

    @NotNull @NonNull String description;

    @NotNull @NonNull List<String> writeUsers;

    @NotNull @NonNull List<String> readUsers;
}
