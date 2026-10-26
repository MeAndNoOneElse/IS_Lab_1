package com.itmo.lab1.exception;

/**
 * Исключение: объект не найден в БД.
 */
public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(String message) {
        super(message);
    }
}
