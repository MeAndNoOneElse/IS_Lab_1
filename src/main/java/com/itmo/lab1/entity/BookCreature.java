package com.itmo.lab1.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * Основная сущность — книжное существо.
 * <p>
 * Ограничения полей (из задания, выполняются и на уровне ORM, и на уровне БД):
 * <ul>
 *   <li>id — &gt; 0, уникально, генерируется автоматически БД</li>
 *   <li>name — не null, не пустая строка</li>
 *   <li>coordinates — не null</li>
 *   <li>creationDate — не null, генерируется автоматически</li>
 *   <li>age — &gt; 0</li>
 *   <li>creatureType — может быть null</li>
 *   <li>creatureLocation — может быть null</li>
 *   <li>attackLevel — &gt; 0</li>
 *   <li>defenseLevel — &gt; 0, может быть null</li>
 *   <li>ring — может быть null</li>
 * </ul>
 */
@Entity
@Table(name = "book_creature", schema = "s465029")
public class BookCreature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", nullable = false)
    @NotNull(message = "Имя не может быть null")
    @NotEmpty(message = "Имя не может быть пустым")
    private String name;

    @OneToOne(optional = false)
    @JoinColumn(name = "coordinates_id", nullable = false)
    @NotNull(message = "Координаты не могут быть null")
    private Coordinates coordinates;

    @Column(name = "creation_date", nullable = false, updatable = false)
    @NotNull(message = "Дата создания не может быть null")
    private LocalDate creationDate;

    @Column(name = "age", nullable = false)
    @Min(value = 1, message = "Возраст должен быть больше 0")
    private Integer age;

    @Column(name = "creature_type")
    @Enumerated(EnumType.STRING)
    private BookCreatureType creatureType;

    @ManyToOne
    @JoinColumn(name = "creature_location_id")
    private MagicCity creatureLocation;

    @Column(name = "attack_level", nullable = false)
    @Min(value = 1, message = "Уровень атаки должен быть больше 0")
    private Integer attackLevel;

    @Column(name = "defense_level")
    @DecimalMin(value = "0.0", inclusive = false, message = "Уровень защиты должен быть больше 0")
    private Float defenseLevel;

    @ManyToOne
    @JoinColumn(name = "ring_id")
    private Ring ring;

    public BookCreature() {
    }

    public BookCreature(String name, Coordinates coordinates, LocalDate creationDate,
                        Integer age, BookCreatureType creatureType, MagicCity creatureLocation,
                        Integer attackLevel, Float defenseLevel, Ring ring) {
        this.name = name;
        this.coordinates = coordinates;
        this.creationDate = creationDate;
        this.age = age;
        this.creatureType = creatureType;
        this.creatureLocation = creatureLocation;
        this.attackLevel = attackLevel;
        this.defenseLevel = defenseLevel;
        this.ring = ring;
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

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(Coordinates coordinates) {
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

    public BookCreatureType getCreatureType() {
        return creatureType;
    }

    public void setCreatureType(BookCreatureType creatureType) {
        this.creatureType = creatureType;
    }

    public MagicCity getCreatureLocation() {
        return creatureLocation;
    }

    public void setCreatureLocation(MagicCity creatureLocation) {
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

    public Ring getRing() {
        return ring;
    }

    public void setRing(Ring ring) {
        this.ring = ring;
    }
}
