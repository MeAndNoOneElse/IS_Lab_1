package com.itmo.lab1.repository;

import com.itmo.lab1.entity.BookCreature;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * DAO для BookCreature: CRUD, пагинация, фильтрация (точное совпадение по строковой колонке), сортировка.
 */
@Repository
@Transactional(readOnly = true)
public class BookCreatureRepository extends BaseRepository<BookCreature, Long> {

    @PersistenceContext
    private EntityManager em;

    public BookCreatureRepository() {
        super(BookCreature.class);
    }

    @Transactional
    public BookCreature save(BookCreature creature) {
        return super.save(creature);
    }

    @Transactional
    public void delete(BookCreature creature) {
        super.delete(creature);
    }

    @Transactional
    public void deleteById(Long id) {
        // Bulk delete без загрузки entière сущности — один DELETE-запрос вместо SELECT + DELETE.
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaDelete<BookCreature> cd = cb.createCriteriaDelete(BookCreature.class);
        Root<BookCreature> root = cd.from(BookCreature.class);
        cd.where(cb.equal(root.get("id"), id));
        em.createQuery(cd).executeUpdate();
    }

    public boolean existsById(Long id) {
        // Легковесная проверка существования: только COUNT, без загрузки данных.
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<BookCreature> root = cq.from(BookCreature.class);
        cq.select(cb.count(root));
        cq.where(cb.equal(root.get("id"), id));
        return em.createQuery(cq).getSingleResult() > 0;
    }

    public BookCreature findByIdWithRelations(Long id) {
        // Загружаем существо со всеми связями одним запросом — нужно для edit/update,
        // чтобы избежать N+1 при доступе к координатам, городу, кольцу.
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<BookCreature> cq = cb.createQuery(BookCreature.class);
        Root<BookCreature> root = cq.from(BookCreature.class);
        root.fetch("coordinates", JoinType.LEFT);
        root.fetch("creatureLocation", JoinType.LEFT);
        root.fetch("ring", JoinType.LEFT);
        cq.where(cb.equal(root.get("id"), id));
        TypedQuery<BookCreature> query = em.createQuery(cq);
        List<BookCreature> result = query.getResultList();
        return result.isEmpty() ? null : result.get(0);
    }

    public List<BookCreature> findPaged(int page, int size,
                                        String sortColumn, String sortDir,
                                        String filterColumn, String filterValue,
                                        String maxDistance,
                                        String creationFrom, String creationTo,
                                        String creatureLocationId, String ringId) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<BookCreature> cq = cb.createQuery(BookCreature.class);
        Root<BookCreature> root = cq.from(BookCreature.class);

        // Загружаем связанные сущности одним запросом (LEFT JOIN FETCH),
        // чтобы избежать N+1 при доступе к getCoordinates/getCreatureLocation/getRing в toListDto.
        root.fetch("coordinates", JoinType.LEFT);
        root.fetch("creatureLocation", JoinType.LEFT);
        root.fetch("ring", JoinType.LEFT);

        List<Predicate> predicates = buildPredicates(cb, root, filterColumn, filterValue);

        // Фильтр по расстоянию координат от начала осей (0,0): sqrt(x^2 + y^2) <= maxDistance.
        if (maxDistance != null && !maxDistance.isBlank()) {
            Join<Object, Object> coordsJoin = root.join("coordinates", JoinType.LEFT);
            Expression<Double> distance = cb.sqrt(
                    cb.sum(
                            cb.prod(coordsJoin.get("x"), coordsJoin.get("x")),
                            cb.prod(coordsJoin.get("y"), coordsJoin.get("y"))
                    )
            );
            predicates.add(cb.le(distance, Double.parseDouble(maxDistance)));
        }

        // Фильтр по дате создания: creationFrom <= creationDate <= creationTo.
        if (creationFrom != null && !creationFrom.isBlank()) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("creationDate"), java.sql.Date.valueOf(creationFrom)));
        }
        if (creationTo != null && !creationTo.isBlank()) {
            predicates.add(cb.lessThanOrEqualTo(root.get("creationDate"), java.sql.Date.valueOf(creationTo)));
        }

        // Фильтр по городу (существо привязано к этому городу).
        if (creatureLocationId != null && !creatureLocationId.isBlank()) {
            Join<Object, Object> locJoin = root.join("creatureLocation", JoinType.LEFT);
            predicates.add(cb.equal(locJoin.get("id"), Long.parseLong(creatureLocationId)));
        }
        // Фильтр по кольцу (существо носит это кольцо).
        if (ringId != null && !ringId.isBlank()) {
            Join<Object, Object> ringJoin = root.join("ring", JoinType.LEFT);
            predicates.add(cb.equal(ringJoin.get("id"), Long.parseLong(ringId)));
        }

        cq.where(predicates.toArray(new Predicate[0]));

        // Сортировка (поддерживаются вложенные поля, например coordinates.x / creatureLocation.name / ring.name).
        if (sortColumn != null && !sortColumn.isBlank()) {
            Path<?> sortPath = sortPath(root, sortColumn);
            Order order = "desc".equalsIgnoreCase(sortDir)
                    ? cb.desc(sortPath)
                    : cb.asc(sortPath);
            cq.orderBy(order);
        }

        TypedQuery<BookCreature> query = em.createQuery(cq);
        query.setFirstResult((page - 1) * size);
        query.setMaxResults(size);
        // DISTINCT нужен при LEFT JOIN FETCH нескольких связей, чтобы исключить
        // posibles дубли строк в результате.
        return query.getResultList().stream().distinct().collect(Collectors.toList());
    }

    public long count(String filterColumn, String filterValue, String maxDistance,
                      String creationFrom, String creationTo,
                      String creatureLocationId, String ringId) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<BookCreature> root = cq.from(BookCreature.class);
        cq.select(cb.count(root));
        List<Predicate> predicates = buildPredicates(cb, root, filterColumn, filterValue);

        // Тот же фильтр по расстоянию, что в findPaged — нужно для точного totalPages.
        if (maxDistance != null && !maxDistance.isBlank()) {
            Join<Object, Object> coordsJoin = root.join("coordinates", JoinType.LEFT);
            Expression<Double> distance = cb.sqrt(
                    cb.sum(
                            cb.prod(coordsJoin.get("x"), coordsJoin.get("x")),
                            cb.prod(coordsJoin.get("y"), coordsJoin.get("y"))
                    )
            );
            predicates.add(cb.le(distance, Double.parseDouble(maxDistance)));
        }

        // Тот же фильтр по дате создания, что в findPaged.
        if (creationFrom != null && !creationFrom.isBlank()) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("creationDate"), java.sql.Date.valueOf(creationFrom)));
        }
        if (creationTo != null && !creationTo.isBlank()) {
            predicates.add(cb.lessThanOrEqualTo(root.get("creationDate"), java.sql.Date.valueOf(creationTo)));
        }

        // Тот же фильтр по городу, что в findPaged.
        if (creatureLocationId != null && !creatureLocationId.isBlank()) {
            Join<Object, Object> locJoin = root.join("creatureLocation", JoinType.LEFT);
            predicates.add(cb.equal(locJoin.get("id"), Long.parseLong(creatureLocationId)));
        }
        // Тот же фильтр по кольцу, что в findPaged.
        if (ringId != null && !ringId.isBlank()) {
            Join<Object, Object> ringJoin = root.join("ring", JoinType.LEFT);
            predicates.add(cb.equal(ringJoin.get("id"), Long.parseLong(ringId)));
        }

        cq.where(predicates.toArray(new Predicate[0]));
        return em.createQuery(cq).getSingleResult();
    }

    public boolean existsReferencedByOthers(Long creatureId) {
        // Легковесный запрос: проверяем наличие FK-ссылок, не загружая entière сущность.
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<BookCreature> root = cq.from(BookCreature.class);
        cq.select(cb.count(root));
        cq.where(cb.equal(root.get("id"), creatureId),
                cb.or(cb.isNotNull(root.get("creatureLocation")),
                      cb.isNotNull(root.get("ring"))));
        return em.createQuery(cq).getSingleResult() > 0;
    }

    /**
     * Разрешить путь сортировки: поддерживаются простые поля (id, name, age…) и
     * вложенные через точку (coordinates.x, creatureLocation.name, ring.name).
     */
    private Path<?> sortPath(Root<?> root, String sortColumn) {
        if (sortColumn.contains(".")) {
            String[] parts = sortColumn.split("\\.", 2);
            return root.get(parts[0]).get(parts[1]);
        }
        return root.get(sortColumn);
    }
}
