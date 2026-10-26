package com.itmo.lab1.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * DTO кольца.
 */
public class RingDto {

    private Long id;

    @NotNull(message = "Имя кольца не может быть null")
    @NotEmpty(message = "Имя кольца не может быть пустым")
    private String name;

    @Min(value = 1, message = "Сила кольца должна быть больше 0")
    private Long power;

    public RingDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getPower() {
        return power;
    }

    public void setPower(Long power) {
        this.power = power;
    }
}
