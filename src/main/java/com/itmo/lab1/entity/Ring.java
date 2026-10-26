package com.itmo.lab1.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * Кольцо. Может быть связано с одним BookCreature (связь many-to-one со стороны существа).
 * Поле name — не null и не пустая. Поле power — nullable, должно быть больше 0, если задано.
 */
@Entity
@Table(name = "ring", schema = "s465029")
public class Ring {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false)
    @NotNull(message = "Имя кольца не может быть null")
    @NotEmpty(message = "Имя кольца не может быть пустым")
    private String name;

    @Column(name = "power")
    @Min(value = 1, message = "Сила кольца должна быть больше 0")
    private Long power;

    public Ring() {
    }

    public Ring(String name, Long power) {
        this.name = name;
        this.power = power;
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
