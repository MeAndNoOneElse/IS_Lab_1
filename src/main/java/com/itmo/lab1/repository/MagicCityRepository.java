package com.itmo.lab1.repository;

import com.itmo.lab1.entity.MagicCity;
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
 * DAO для MagicCity: CRUD, пагинация, фильтрация (точное совпадение по строковой колонке), сортировка.
 * Города не имеют связей, поэтому LEFT JOIN FETCH не используется.
 */
@Repository
@Transactional(readOnly = true)
public class MagicCityRepository extends BaseRepository<MagicCity, Long> {

    @PersistenceContext
    private EntityManager em;

    public MagicCityRepository() {
        super(MagicCity.class);
    }

    @Transactional
    public MagicCity save(MagicCity city) {
        return super.save(city);
    }

    @Transactional
    public void delete(MagicCity city) {
        super.delete(city);
    }

    @Transactional
    public void deleteById(Long id) {
        // Bulk delete без загрузки entière сущности — один DELETE-запрос вместо SELECT + DELETE.
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaDelete<MagicCity> cd = cb.createCriteriaDelete(MagicCity.class);
        Root<MagicCity> root = cd.from(MagicCity.class);
        cd.where(cb.equal(root.get("id"), id));
        em.createQuery(cd).executeUpdate();
    }

    public boolean existsById(Long id) {
        // Легковесная проверка существования: только COUNT, без загрузки данных.
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<MagicCity> root = cq.from(MagicCity.class);
        cq.select(cb.count(root));
        cq.where(cb.equal(root.get("id"), id));
        return em.createQuery(cq).getSingleResult() > 0;
    }

    public List<MagicCity> findPaged(int page, int size,
                                     String sortColumn, String sortDir,
                                     String filterColumn, String filterValue) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<MagicCity> cq = cb.createQuery(MagicCity.class);
        Root<MagicCity> root = cq.from(MagicCity.class);

        List<Predicate> predicates = buildPredicates(cb, root, filterColumn, filterValue);
        cq.where(predicates.toArray(new Predicate[0]));

        // Сортировка
        if (sortColumn != null && !sortColumn.isBlank()) {
            Order order = "desc".equalsIgnoreCase(sortDir)
                    ? cb.desc(root.get(sortColumn))
                    : cb.asc(root.get(sortColumn));
            cq.orderBy(order);
        }

        TypedQuery<MagicCity> query = em.createQuery(cq);
        query.setFirstResult((page - 1) * size);
        query.setMaxResults(size);
        return query.getResultList();
    }

    public long count(String filterColumn, String filterValue) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<MagicCity> root = cq.from(MagicCity.class);
        cq.select(cb.count(root));
        List<Predicate> predicates = buildPredicates(cb, root, filterColumn, filterValue);
        cq.where(predicates.toArray(new Predicate[0]));
        return em.createQuery(cq).getSingleResult();
    }

    public MagicCity getReference(Long id) {
        // Proxy без запроса к БД — достаточно для установки связи ManyToOne.
        return em.getReference(MagicCity.class, id);
    }
}
