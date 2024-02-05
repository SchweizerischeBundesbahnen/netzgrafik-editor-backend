package ch.sbb.pfi.netzgrafikeditor.api.common.model;

import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Builder
@AllArgsConstructor
@Value
public class ErrorDto {
    @NonNull @NotNull Integer code;

    @NonNull @NotNull String message;
}
