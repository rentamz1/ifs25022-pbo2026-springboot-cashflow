package org.delcom.ifs25022pbo2026springbootcashflow.domain.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class DomainExceptionTest {

    @Test
    void keepsMessage() {
        DomainException exception = new DomainException("pesan");

        assertEquals("pesan", exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void keepsMessageAndCause() {
        Exception cause = new IllegalStateException("akar");
        DomainException exception = new DomainException("pesan", cause);

        assertEquals("pesan", exception.getMessage());
        assertSame(cause, exception.getCause());
    }

    @Test
    void entityNotFoundCarriesIdAndMessage() {
        EntityNotFoundException exception = new EntityNotFoundException(42);

        assertEquals(42, exception.getId());
        assertEquals("Transaksi dengan ID 42 tidak ditemukan.", exception.getMessage());
        assertInstanceOf(DomainException.class, exception);
    }

    @Test
    void validationExceptionCarriesMessage() {
        ValidationException exception = new ValidationException("tidak valid");

        assertEquals("tidak valid", exception.getMessage());
        assertInstanceOf(DomainException.class, exception);
    }
}
