package com.societyledger.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class SocietyLedgerException extends RuntimeException {

    private final String errorCode;
    private final HttpStatus httpStatus;

    public SocietyLedgerException(String message, String errorCode, HttpStatus httpStatus) {
        super(message);
        this.errorCode  = errorCode;
        this.httpStatus = httpStatus;
    }

    /** Convenience factory: 404 Not Found */
    public static SocietyLedgerException notFound(String entityName, Long id) {
        return new SocietyLedgerException(
                entityName + " with id " + id + " not found.",
                entityName.toUpperCase() + "_NOT_FOUND",
                HttpStatus.NOT_FOUND);
    }

    /** Convenience factory: 403 Forbidden */
    public static SocietyLedgerException forbidden(String detail) {
        return new SocietyLedgerException(detail, "FORBIDDEN", HttpStatus.FORBIDDEN);
    }

    /** Convenience factory: 400 Bad Request */
    public static SocietyLedgerException badRequest(String message, String errorCode) {
        return new SocietyLedgerException(message, errorCode, HttpStatus.BAD_REQUEST);
    }
}
