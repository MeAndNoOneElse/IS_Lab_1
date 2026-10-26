package com.itmo.lab1.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Date;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * DTO волшебного города.
 */
public class MagicCityDto {

    private Long id;

    @NotNull(message = "Имя города не может быть null")
    @NotEmpty(message = "Имя города не может быть пустым")
    private String name;

    @NotNull(message = "Площадь не может быть null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Площадь должна быть больше 0")
    private Float area;

    @NotNull(message = "Население не может быть null")
    private Long population;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private Date establishmentDate;

    @NotNull(message = "Правитель не может быть null")
    private BookCreatureTypeDto governor;

    @NotNull(message = "Признак столицы не может быть null")
    private Boolean capital;

    @NotNull(message = "Плотность населения не может быть null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Плотность населения должна быть больше 0")
    private Double populationDensity;

    public MagicCityDto() {
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

    public Float getArea() {
        return area;
    }

    public void setArea(Float area) {
        this.area = area;
    }

    public Long getPopulation() {
        return population;
    }

    public void setPopulation(Long population) {
        this.population = population;
    }

    public Date getEstablishmentDate() {
        return establishmentDate;
    }

    public void setEstablishmentDate(Date establishmentDate) {
        this.establishmentDate = establishmentDate;
    }

    public BookCreatureTypeDto getGovernor() {
        return governor;
    }

    public void setGovernor(BookCreatureTypeDto governor) {
        this.governor = governor;
    }

    public Boolean getCapital() {
        return capital;
    }

    public void setCapital(Boolean capital) {
        this.capital = capital;
    }

    public Double getPopulationDensity() {
        return populationDensity;
    }

    public void setPopulationDensity(Double populationDensity) {
        this.populationDensity = populationDensity;
    }
}
