package com.itmo.lab1.exception;

/**
 * Исключение: удаление заблокировано, т.к. объект связан с другими объектами.
 */
public class ReferencedEntityException extends RuntimeException {

    public ReferencedEntityException(String message) {
        super(message);
    }
}
