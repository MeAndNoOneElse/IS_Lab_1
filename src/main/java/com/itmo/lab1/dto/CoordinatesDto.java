package com.itmo.lab1.dto;

import jakarta.validation.constraints.Min;

/**
 * DTO координат.
 */
public class CoordinatesDto {

    private Long id;

    @Min(value = 1, message = "Координата X должна быть больше 0")
    private Long x;

    @Min(value = 1, message = "Координата Y должна быть больше 0")
    private Long y;

    public CoordinatesDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getX() {
        return x;
    }

    public void setX(Long x) {
        this.x = x;
    }

    public Long getY() {
        return y;
    }

    public void setY(Long y) {
        this.y = y;
    }
}
