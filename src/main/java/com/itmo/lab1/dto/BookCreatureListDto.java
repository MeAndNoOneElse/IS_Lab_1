package com.itmo.lab1.dto;

import java.time.LocalDate;

/**
 * DTO для отображения BookCreature в списке (таблица с пагинацией).
 */
public class BookCreatureListDto {

    private Long id;
    private String name;
    private CoordinatesDto coordinates;
    private LocalDate creationDate;
    private Integer age;
    private String creatureType;
    private String creatureLocation; // имя города
    private Integer attackLevel;
    private Float defenseLevel;
    private String ring; // имя кольца

    public BookCreatureListDto() {
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

    public String getCreatureType() {
        return creatureType;
    }

    public void setCreatureType(String creatureType) {
        this.creatureType = creatureType;
    }

    public String getCreatureLocation() {
        return creatureLocation;
    }

    public void setCreatureLocation(String creatureLocation) {
        this.creatureLocation = creatureLocation;
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

    public String getRing() {
        return ring;
    }

    public void setRing(String ring) {
        this.ring = ring;
    }

    @Override
    public String toString() {
        return name != null ? name : "BookCreatureListDto[id=" + id + "]";
    }
}
