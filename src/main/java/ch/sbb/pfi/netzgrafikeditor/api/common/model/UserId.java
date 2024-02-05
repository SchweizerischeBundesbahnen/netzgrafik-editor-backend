package ch.sbb.pfi.netzgrafikeditor.api.common.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.NonNull;
import lombok.Value;

@Value
public class UserId implements Id {
    @JsonValue @NonNull String value;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public UserId(String value) {
        this.value = value;
    }

    public static UserId of(String value) {
        return new UserId(value);
    }

    @Override
    public String toString() {
        return "#" + value;
    }
}
