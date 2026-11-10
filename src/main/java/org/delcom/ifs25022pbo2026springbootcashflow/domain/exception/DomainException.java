package org.delcom.ifs25022pbo2026springbootcashflow.domain.exception;

/**
 * Checked exception dasar untuk seluruh kesalahan di domain Cashflow.
 */
public class DomainException extends Exception {
    public DomainException(String message) {
        super(message);
    }

    public DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
