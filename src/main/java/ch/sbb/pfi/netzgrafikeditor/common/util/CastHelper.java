package ch.sbb.pfi.netzgrafikeditor.common.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CastHelper {

    public static <T> Optional<T> tryCast(Object o, Class<T> type) {
        if (type.isInstance(o)) {
            return Optional.of((T) o);
        }
        return Optional.empty();
    }
}
