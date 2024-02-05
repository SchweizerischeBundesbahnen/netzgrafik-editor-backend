package ch.sbb.pfi.netzgrafikeditor.common;

import ch.sbb.pfi.netzgrafikeditor.api.common.model.Id;

import java.util.function.Supplier;

public class NotFoundException extends Exception {

    public static Supplier<NotFoundException> of(String table, Id id) {
        return () -> new NotFoundException(table, id);
    }

    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(String table, Id id) {
        super(String.format("No entry with id %s found in table %s", id, table));
    }

    public NotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
