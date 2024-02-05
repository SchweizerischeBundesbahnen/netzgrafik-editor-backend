package ch.sbb.pfi.netzgrafikeditor.api.common.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.NonNull;
import lombok.Value;

@Value
public class ProjectId implements Id {
    @JsonValue @NonNull Long value;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public ProjectId(long value) {
        this.value = value;
    }

    public static ProjectId of(long value) {
        return new ProjectId(value);
    }

    @Override
    public String toString() {
        return "#" + value;
    }
}
