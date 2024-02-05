package ch.sbb.pfi.netzgrafikeditor.common;

public class ValidationErrorException extends Exception {
    public ValidationErrorException(String message) {
        super("Validation Error: " + message);
    }
}
