package ch.sbb.pfi.netzgrafikeditor.api.common.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.NonNull;
import lombok.Value;

@Value
public class VariantId implements Id {
    @JsonValue @NonNull Long value;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public VariantId(long value) {
        this.value = value;
    }

    public static VariantId of(long value) {
        return new VariantId(value);
    }

    @Override
    public String toString() {
        return "#" + value;
    }
}
