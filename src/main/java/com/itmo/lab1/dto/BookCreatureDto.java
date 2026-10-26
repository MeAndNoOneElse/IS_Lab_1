package com.itmo.lab1.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * DTO для создания и редактирования BookCreature.
 * Содержит все редактируемые поля и ссылки на существующие вспомогательные объекты (по id).
 */
public class BookCreatureDto {

    private Long id;

    @NotNull(message = "Имя не может быть null")
    @NotEmpty(message = "Имя не может быть пустым")
    private String name;

    @NotNull(message = "Координаты не могут быть null")
    private CoordinatesDto coordinates;

    private LocalDate creationDate;

    @NotNull(message = "Возраст не может быть null")
    @Min(value = 1, message = "Возраст должен быть больше 0")
    private Integer age;

    private BookCreatureTypeDto creatureType;

    private Long creatureLocationId; // ссылка на существующий MagicCity

    @NotNull(message = "Уровень атаки не может быть null")
    @Min(value = 1, message = "Уровень атаки должен быть больше 0")
    private Integer attackLevel;

    @DecimalMin(value = "0.0", inclusive = false, message = "Уровень защиты должен быть больше 0")
    private Float defenseLevel;

    private Long ringId; // ссылка на существующее Ring

    public BookCreatureDto() {
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

    public CoordinatesDto getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(CoordinatesDto coordinates) {
        this.coordinates = coordinates;
    }

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDate creationDate) {
        this.creationDate = creationDate;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public BookCreatureTypeDto getCreatureType() {
        return creatureType;
    }

    public void setCreatureType(BookCreatureTypeDto creatureType) {
        this.creatureType = creatureType;
    }

    public Long getCreatureLocationId() {
        return creatureLocationId;
    }

    public void setCreatureLocationId(Long creatureLocationId) {
        this.creatureLocationId = creatureLocationId;
    }

    public Integer getAttackLevel() {
        return attackLevel;
    }

    public void setAttackLevel(Integer attackLevel) {
        this.attackLevel = attackLevel;
    }

    public Float getDefenseLevel() {
        return defenseLevel;
    }

    public void setDefenseLevel(Float defenseLevel) {
        this.defenseLevel = defenseLevel;
    }

    public Long getRingId() {
        return ringId;
    }

    public void setRingId(Long ringId) {
        this.ringId = ringId;
    }
}
