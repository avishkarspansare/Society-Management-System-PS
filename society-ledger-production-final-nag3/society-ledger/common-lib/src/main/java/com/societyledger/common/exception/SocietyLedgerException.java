package com.societyledger.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class SocietyLedgerException extends RuntimeException {

    private final String errorCode;
    private final HttpStatus httpStatus;

    public SocietyLedgerException(String message, String errorCode, HttpStatus httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public static SocietyLedgerException notFound(String entity, Long id) {
        return new SocietyLedgerException(
                entity + " not found with id: " + id,
                "NOT_FOUND",
                HttpStatus.NOT_FOUND
        );
    }

    public static SocietyLedgerException forbidden(String message) {
        return new SocietyLedgerException(message, "FORBIDDEN", HttpStatus.FORBIDDEN);
    }

    public static SocietyLedgerException badRequest(String message) {
        return new SocietyLedgerException(message, "BAD_REQUEST", HttpStatus.BAD_REQUEST);
    }

    public static SocietyLedgerException conflict(String message) {
        return new SocietyLedgerException(message, "CONFLICT", HttpStatus.CONFLICT);
    }
}
