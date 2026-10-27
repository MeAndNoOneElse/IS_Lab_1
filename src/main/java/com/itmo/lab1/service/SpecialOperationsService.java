package com.itmo.lab1.service;

import com.itmo.lab1.dto.BookCreatureListDto;
import com.itmo.lab1.dto.SpecialOpResultDto;
import com.itmo.lab1.entity.BookCreature;
import com.itmo.lab1.repository.BookCreatureRepository;
import com.itmo.lab1.websocket.ChangeBroadcaster;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Вызов 5 специальных операций, реализованных как PL/pgSQL-функции в базе данных
 * (схема {@code s465029}). Операции не вписываются в обычный CRUD и выполняются
 * через нативные SQL-запросы через {@link EntityManager}.
 * <p>
 * Каждая операция возвращает {@link SpecialOpResultDto} с кодом операции, данными
 * и человекочитаемым сообщением для отображения на странице {@code special/operations}.
 */
@Service
@Transactional
public class SpecialOperationsService {

    @PersistenceContext
    private EntityManager em;

    private final BookCreatureRepository creatureRepository;
    private final ChangeBroadcaster broadcaster;

    public SpecialOperationsService(BookCreatureRepository creatureRepository,
                                    ChangeBroadcaster broadcaster) {
        this.creatureRepository = creatureRepository;
        this.broadcaster = broadcaster;
    }

    /**
     * 1. Сумма уровней атаки всех существ.
     * Вызывает PL/pgSQL-функцию {@code s465029.sum_attack_level()}.
     * Возвращает {@link SpecialOpResultDto} с числом — суммой всех attackLevel.
     */
    public SpecialOpResultDto sumAttackLevel() {
        Number sum = (Number) em.createNativeQuery(
                "SELECT s465029.sum_attack_level()").getSingleResult();
        return new SpecialOpResultDto("sumAttackLevel", sum.longValue(),
                "Сумма уровней атаки всех существ: " + sum, true);
    }

    /**
     * 2. Существа, чьё имя начинается с заданной подстроки (регистронезависимо).
     * Вызывает PL/pgSQL-функцию {@code s465029.find_by_name_substring(?)} с параметром подстроки.
     * Каждая строка результата преобразуется в {@link BookCreatureListDto} через {@link #mapRow}.
     */
    @Transactional(readOnly = true)
    public SpecialOpResultDto findByNameSubstring(String substring) {
        List<Object[]> rows = em.createNativeQuery(
                "SELECT * FROM s465029.find_by_name_substring(?)")
                .setParameter(1, substring)
                .getResultList();

        List<BookCreatureListDto> result = rows.stream().map(this::mapRow).collect(Collectors.toList());
        return new SpecialOpResultDto("findByNameSubstring", result,
                "Найдено существ с именем, начинающимся на '" + substring + "': " + result.size(), true);
    }

    /**
     * 3. Уникальные значения уровней атаки всех существ.
     * Вызывает PL/pgSQL-функцию {@code s465029.unique_attack_levels()}.
     * Возвращает список целых чисел — уникальные attackLevel.
     */
    public SpecialOpResultDto uniqueAttackLevels() {
        List<Integer> levels = em.createNativeQuery(
                "SELECT * FROM s465029.unique_attack_levels()").getResultList();
        return new SpecialOpResultDto("uniqueAttackLevels", levels,
                "Уникальные уровни атаки: " + levels, true);
    }

    /**
     * 4. Обмен кольцами местами между двумя существами по их ID ({@code id1} и {@code id2}).
     * Вызывает PL/pgSQL-функцию {@code s465029.swap_rings(?, ?)}.
     * Функция void — не возвращает строк, поэтому {@code getSingleResult()} бросает
     * {@code NoResultException}, который перехватывается и игнорируется (ожидаемое поведение).
     * После выполнения рассылается WebSocket-уведомление.
     */
    public SpecialOpResultDto swapRings(Long id1, Long id2) {
        // void-функция не возвращает строк — getSingleResult бросит NoResultException, ловим его.
        try {
            em.createNativeQuery("SELECT s465029.swap_rings(?, ?)")
                    .setParameter(1, id1)
                    .setParameter(2, id2)
                    .getSingleResult();
        } catch (jakarta.persistence.NoResultException e) {
            // ожидаемо для void-функции
        }
        broadcast();
        return new SpecialOpResultDto("swapRings",
                "Кольца персонажей id=" + id1 + " и id=" + id2 + " обменяны",
                "Обмен кольцами выполнен успешно", true);
    }

    /**
     * Отправляет всем подключённым WebSocket-клиентам уведомление об изменении данных.
     */
    private void broadcast() {
        broadcaster.broadcastCreatureChanged();
    }

    /**
     * 5. Уничтожить все города, правителями которых являются эльфы.
     * Вызывает PL/pgSQL-функцию {@code s465029.destroy_elf_cities()}.
     * Функция возвращает количество удалённых городов.
     * После выполнения рассылается WebSocket-уведомление.
     */
    public SpecialOpResultDto destroyElfCities() {
        Number count = (Number) em.createNativeQuery(
                "SELECT s465029.destroy_elf_cities()").getSingleResult();
        broadcast();
        return new SpecialOpResultDto("destroyElfCities", count.intValue(),
                "Уничтожено городов эльфов: " + count, true);
    }

    /**
     * Преобразует одну строку результата нативного SQL-запроса (массив {@code Object[]})
     * в {@link BookCreatureListDto}.
     * Обрабатывает возможные {@code null} значения в колонках возраста, типа и уровня атаки.
     */
    private BookCreatureListDto mapRow(Object[] row) {
        BookCreatureListDto dto = new BookCreatureListDto();
        dto.setId(((Number) row[0]).longValue());
        dto.setName((String) row[1]);
        if (row[2] != null) dto.setAge(((Number) row[2]).intValue());
        dto.setCreatureType(row[3] != null ? row[3].toString() : null);
        if (row[4] != null) dto.setAttackLevel(((Number) row[4]).intValue());
        return dto;
    }
}
