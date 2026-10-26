package com.itmo.lab1.dto;

/**
 * Универсальный DTO для результата специальной операции БД.
 */
public class SpecialOpResultDto {

    private String operation;
    private Object result;
    private String message;
    private boolean success;

    public SpecialOpResultDto() {
    }

    public SpecialOpResultDto(String operation, Object result, String message, boolean success) {
        this.operation = operation;
        this.result = result;
        this.message = message;
        this.success = success;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
