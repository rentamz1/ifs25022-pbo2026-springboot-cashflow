package org.delcom.ifs25022pbo2026springbootcashflow.domain.exception;

/**
 * Dilempar ketika input tidak memenuhi aturan bisnis (keterangan kosong, jumlah tidak valid, dst).
 */
public class ValidationException extends DomainException {
    public ValidationException(String message) {
        super(message);
    }
}
