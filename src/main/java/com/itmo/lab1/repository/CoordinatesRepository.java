package com.itmo.lab1.repository;

import com.itmo.lab1.entity.Coordinates;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(readOnly = true)
public class CoordinatesRepository extends BaseRepository<Coordinates, Long> {

    @PersistenceContext
    private EntityManager em;

    public CoordinatesRepository() {
        super(Coordinates.class);
    }

    @Transactional
    public Coordinates save(Coordinates coords) {
        return super.save(coords);
    }

    @Transactional
    public void delete(Coordinates coords) {
        super.delete(coords);
    }
}
