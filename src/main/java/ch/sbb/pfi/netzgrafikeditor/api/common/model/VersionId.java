package ch.sbb.pfi.netzgrafikeditor.api.common.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.NonNull;
import lombok.Value;

@Value
public class VersionId implements Id {
    @JsonValue @NonNull Long value;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public VersionId(long value) {
        this.value = value;
    }

    public static VersionId of(long value) {
        return new VersionId(value);
    }

    @Override
    public String toString() {
        return "#" + value;
    }
}
