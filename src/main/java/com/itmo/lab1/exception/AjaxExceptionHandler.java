package com.itmo.lab1.exception;

import java.util.HashMap;
import java.util.Map;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Глобальный обработчик исключений для AJAX-запросов.
 * Возвращает JSON вида {@code {success: false, message: "..."}} вместо HTML-страницы ошибки.
 */
@RestControllerAdvice
public class AjaxExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, Object> handleNotFound(EntityNotFoundException ex) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", ex.getMessage());
        return result;
    }

    @ExceptionHandler(ReferencedEntityException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleReferenced(ReferencedEntityException ex) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", ex.getMessage());
        return result;
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, Object> handleDataIntegrity(DataIntegrityViolationException ex) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", "Нарушение целостности данных: " + ex.getMostSpecificCause().getMessage());
        return result;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", "Ошибка валидации данных: " + ex.getMessage());
        return result;
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", "Некорректный тип параметра: " + ex.getName());
        return result;
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, Object> handleGeneric(Exception ex) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", "Внутренняя ошибка: " + ex.getMessage());
        return result;
    }
}
