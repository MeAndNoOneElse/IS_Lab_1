package com.itmo.lab1.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;

/**
 * Координаты. Связь один-к-одному с {@link BookCreature}.
 */
@Entity
@Table(name = "coordinates", schema = "s465029")
public class Coordinates {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "x", nullable = false)
    @Min(value = 1, message = "Координата X должна быть больше 0")
    private Long x;

    @Column(name = "y", nullable = false)
    @Min(value = 1, message = "Координата Y должна быть больше 0")
    private Long y;

    @OneToOne(mappedBy = "coordinates")
    private BookCreature bookCreature;

    public Coordinates() {
    }

    public Coordinates(Long x, Long y) {
        this.x = x;
        this.y = y;
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

    public BookCreature getBookCreature() {
        return bookCreature;
    }

    public void setBookCreature(BookCreature bookCreature) {
        this.bookCreature = bookCreature;
    }
}
