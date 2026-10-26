package com.itmo.lab1.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Date;

/**
 * Волшебный город. Поля name, governor, capital — не null; name не пустая;
 * area, population, populationDensity — больше 0.
 */
@Entity
@Table(name = "magic_city", schema = "s465029")
public class MagicCity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false)
    @NotNull(message = "Имя города не может быть null")
    @NotEmpty(message = "Имя города не может быть пустым")
    private String name;

    @Column(name = "area", nullable = false)
    @DecimalMin(value = "0.0", inclusive = false, message = "Площадь должна быть больше 0")
    private Float area;

    @Column(name = "population", nullable = false)
    @Min(value = 1, message = "Население должно быть больше 0")
    private Long population;

    @Column(name = "establishment_date")
    private Date establishmentDate;

    @Column(name = "governor", nullable = false)
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Правитель города не может быть null")
    private BookCreatureType governor;

    @Column(name = "capital", nullable = false)
    @NotNull(message = "Признак столицы не может быть null")
    private Boolean capital;

    @Column(name = "population_density", nullable = false)
    @NotNull(message = "Плотность населения не может быть null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Плотность населения должна быть больше 0")
    private Double populationDensity;

    public MagicCity() {
    }

    public MagicCity(String name, Float area, Long population, Date establishmentDate,
                     BookCreatureType governor, Boolean capital, Double populationDensity) {
        this.name = name;
        this.area = area;
        this.population = population;
        this.establishmentDate = establishmentDate;
        this.governor = governor;
        this.capital = capital;
        this.populationDensity = populationDensity;
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

    public BookCreatureType getGovernor() {
        return governor;
    }

    public void setGovernor(BookCreatureType governor) {
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
