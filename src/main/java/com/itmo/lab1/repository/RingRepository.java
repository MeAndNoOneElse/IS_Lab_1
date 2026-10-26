package com.itmo.lab1.repository;

import com.itmo.lab1.entity.Ring;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.List;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * DAO для Ring: CRUD, пагинация, фильтрация (точное совпадение по строковой колонке), сортировка.
 * Кольца не имеют связей, поэтому LEFT JOIN FETCH не используется.
 */
@Repository
@Transactional(readOnly = true)
public class RingRepository extends BaseRepository<Ring, Long> {

    @PersistenceContext
    private EntityManager em;

    public RingRepository() {
        super(Ring.class);
    }

    @Transactional
    public Ring save(Ring ring) {
        return super.save(ring);
    }

    @Transactional
    public void delete(Ring ring) {
        super.delete(ring);
    }

    public Ring getReference(Long id) {
        // Proxy без запроса к БД — достаточно для установки связи ManyToOne.
        return em.getReference(Ring.class, id);
    }

    @Transactional
    public void deleteById(Long id) {
        // Bulk delete без загрузки entière сущности — один DELETE-запрос вместо SELECT + DELETE.
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaDelete<Ring> cd = cb.createCriteriaDelete(Ring.class);
        Root<Ring> root = cd.from(Ring.class);
        cd.where(cb.equal(root.get("id"), id));
        em.createQuery(cd).executeUpdate();
    }

    public boolean existsById(Long id) {
        // Легковесная проверка существования: только COUNT, без загрузки данных.
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<Ring> root = cq.from(Ring.class);
        cq.select(cb.count(root));
        cq.where(cb.equal(root.get("id"), id));
        return em.createQuery(cq).getSingleResult() > 0;
    }

    public List<Ring> findPaged(int page, int size,
                                String sortColumn, String sortDir,
                                String filterColumn, String filterValue) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Ring> cq = cb.createQuery(Ring.class);
        Root<Ring> root = cq.from(Ring.class);

        List<Predicate> predicates = buildPredicates(cb, root, filterColumn, filterValue);
        cq.where(predicates.toArray(new Predicate[0]));

        // Сортировка
        if (sortColumn != null && !sortColumn.isBlank()) {
            Order order = "desc".equalsIgnoreCase(sortDir)
                    ? cb.desc(root.get(sortColumn))
                    : cb.asc(root.get(sortColumn));
            cq.orderBy(order);
        }

        TypedQuery<Ring> query = em.createQuery(cq);
        query.setFirstResult((page - 1) * size);
        query.setMaxResults(size);
        return query.getResultList();
    }

    public long count(String filterColumn, String filterValue) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<Ring> root = cq.from(Ring.class);
        cq.select(cb.count(root));
        List<Predicate> predicates = buildPredicates(cb, root, filterColumn, filterValue);
        cq.where(predicates.toArray(new Predicate[0]));
        return em.createQuery(cq).getSingleResult();
    }
}
