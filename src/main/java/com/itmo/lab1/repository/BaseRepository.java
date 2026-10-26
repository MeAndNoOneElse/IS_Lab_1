package com.itmo.lab1.repository;

import com.itmo.lab1.entity.BookCreature;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

/**
 * Базовый DAO на EclipseLink EntityManager с типобезопасным Criteria API.
 */
public abstract class BaseRepository<T, ID> {

    @PersistenceContext
    protected EntityManager em;

    private final Class<T> entityClass;

    protected BaseRepository(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    public T findById(ID id) {
        return em.find(entityClass, id);
    }

    public List<T> findAll() {
        CriteriaQuery<T> cq = em.getCriteriaBuilder().createQuery(entityClass);
        cq.select(cq.from(entityClass));
        return em.createQuery(cq).getResultList();
    }

    public T save(T entity) {
        if (em.contains(entity)) {
            return em.merge(entity);
        }
        em.persist(entity);
        return entity;
    }

    public void delete(T entity) {
        if (!em.contains(entity)) {
            entity = em.find(entityClass, getId(entity));
        }
        em.remove(entity);
    }

    @SuppressWarnings("unchecked")
    protected ID getId(T entity) {
        try {
            return (ID) entityClass.getDeclaredMethod("getId").invoke(entity);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot get id of " + entityClass.getName(), e);
        }
    }

    protected List<Predicate> buildPredicates(CriteriaBuilder cb, Root<T> root,
                                              String filterColumn, String filterValue) {
        List<Predicate> predicates = new ArrayList<>();
        if (filterColumn != null && !filterColumn.isBlank()
                && filterValue != null && !filterValue.isBlank()) {
            try {
                Path<?> attribute = root.get(filterColumn);
                Object param = convertFilterValue(attribute.getJavaType(), filterValue);
                predicates.add(cb.equal(attribute, param));
            } catch (IllegalArgumentException e) {
                // Неизвестная колонка фильтра или недопустимое значение — игнорируем фильтр,
                // вместо 500-й ошибки при опечатке в имени колонки.
            }
        }
        return predicates;
    }

    /**
     * Привести строковое значение фильтра к Java-типу атрибута сущности.
     * Перечисления — по имени константы, числа и булево — парсинг, остальное — как строка.
     * Это необходимо для корректной фильтрации по Enum-полям (например, creatureType):
     * EclipseLink не может привязать строку к Enum-атрибуту без такого приведения.
     */
    @SuppressWarnings("unchecked")
    private Object convertFilterValue(Class<?> attrType, String value) {
        if (attrType.isEnum()) {
            return Enum.valueOf((Class<Enum>) attrType, value.trim());
        }
        if (attrType == int.class || attrType == Integer.class) {
            return Integer.parseInt(value.trim());
        }
        if (attrType == long.class || attrType == Long.class) {
            return Long.parseLong(value.trim());
        }
        if (attrType == float.class || attrType == Float.class) {
            return Float.parseFloat(value.trim());
        }
        if (attrType == double.class || attrType == Double.class) {
            return Double.parseDouble(value.trim());
        }
        if (attrType == boolean.class || attrType == Boolean.class) {
            return Boolean.parseBoolean(value.trim());
        }
        return value;
    }
}
