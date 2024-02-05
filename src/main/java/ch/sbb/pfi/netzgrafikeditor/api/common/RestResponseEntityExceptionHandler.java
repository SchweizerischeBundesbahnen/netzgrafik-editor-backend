package ch.sbb.pfi.netzgrafikeditor.api.common;

import ch.sbb.pfi.netzgrafikeditor.api.common.model.ErrorDto;
import ch.sbb.pfi.netzgrafikeditor.common.ConflictException;
import ch.sbb.pfi.netzgrafikeditor.common.ForbiddenOperationException;
import ch.sbb.pfi.netzgrafikeditor.common.NotFoundException;
import ch.sbb.pfi.netzgrafikeditor.common.ValidationErrorException;
import ch.sbb.pfi.netzgrafikeditor.common.util.CastHelper;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
@Slf4j
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    protected ResponseEntity<ErrorDto> handleNotFound(Exception ex, WebRequest request) {
        return this.handleBusinessLogicExceptions(ex, HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(ConflictException.class)
    protected ResponseEntity<ErrorDto> handleConflict(Exception ex, WebRequest request) {
        return this.handleBusinessLogicExceptions(ex, HttpStatus.CONFLICT, request);
    }

    @ExceptionHandler(ValidationErrorException.class)
    protected ResponseEntity<ErrorDto> handleValidationError(Exception ex, WebRequest request) {
        return this.handleBusinessLogicExceptions(ex, HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(ForbiddenOperationException.class)
    protected ResponseEntity<ErrorDto> handleForbiddenOperation(Exception ex, WebRequest request) {
        return this.handleBusinessLogicExceptions(ex, HttpStatus.FORBIDDEN, request);
    }

    @ExceptionHandler(Throwable.class)
    protected ResponseEntity<Object> handleInternalServerError(Exception ex, WebRequest request) {
        return handleExceptionInternal(
                ex,
                new Error(ex.getMessage()),
                new HttpHeaders(),
                HttpStatus.INTERNAL_SERVER_ERROR,
                request);
    }

    /** Handle bean validation errors */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception ex,
            Object body,
            HttpHeaders headers,
            HttpStatusCode statusCode,
            WebRequest request) {
        this.log(statusCode, ex, request);
        // for security reasons, we don't provide the exception message to not leak implementation
        // details
        return new ResponseEntity<>(
                new ErrorDto(
                        statusCode.value(),
                        HttpStatus.valueOf(statusCode.value()).getReasonPhrase()),
                headers,
                statusCode);
    }

    private ResponseEntity<ErrorDto> handleBusinessLogicExceptions(
            Exception ex, HttpStatus status, WebRequest request) {
        this.log(status, ex, request);
        return new ResponseEntity<>(new ErrorDto(status.value(), ex.getMessage()), status);
    }

    private void log(HttpStatusCode returnedStatusCode, Exception ex, WebRequest request) {
        if (log.isInfoEnabled()) {
            CastHelper.tryCast(request, ServletWebRequest.class)
                    .ifPresentOrElse(
                            servletWebRequest -> {
                                log.info(
                                        "Endpoint {}:{} returned {} because: {}",
                                        servletWebRequest.getRequest().getMethod(),
                                        servletWebRequest.getRequest().getRequestURI(),
                                        returnedStatusCode,
                                        ex.getMessage());

                                if (returnedStatusCode.is5xxServerError()) {
                                    ex.printStackTrace();
                                }
                            },
                            () ->
                                    log.info(
                                            "Unknown endpoint returned {} because: {}",
                                            returnedStatusCode,
                                            ex.getMessage()));
        }
    }
}
