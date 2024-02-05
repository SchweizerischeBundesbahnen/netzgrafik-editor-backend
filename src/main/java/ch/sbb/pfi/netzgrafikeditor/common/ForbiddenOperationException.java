package ch.sbb.pfi.netzgrafikeditor.common;

public class ForbiddenOperationException extends Exception {
    public ForbiddenOperationException(String message) {
        super("Forbidden Operation: " + message);
    }
}
