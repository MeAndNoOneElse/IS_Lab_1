package com.itmo.lab1.service;

import com.itmo.lab1.dto.BookCreatureTypeDto;
import com.itmo.lab1.dto.MagicCityDto;
import com.itmo.lab1.entity.BookCreatureType;
import com.itmo.lab1.entity.MagicCity;
import com.itmo.lab1.exception.EntityNotFoundException;
import com.itmo.lab1.exception.ReferencedEntityException;
import com.itmo.lab1.repository.BookCreatureRepository;
import com.itmo.lab1.repository.MagicCityRepository;
import com.itmo.lab1.websocket.ChangeBroadcaster;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Бизнес-логика магических городов (MagicCity): CRUD, пагинированный список с фильтрами
 * и сортировкой, поиск по ID.
 * <p>
 * {@code @Transactional} на уровне класса — каждый публичный метод выполняется в своей
 * транзакции. Методы только для чтения anotнированы {@code @Transactional(readOnly = true)},
 * чтобы оптимизировать работу с БД (нет накладных расходов на управление транзакцией записи).
 */
@Service
@Transactional
public class MagicCityService {

    private final MagicCityRepository repository;
    private final BookCreatureRepository creatureRepository;
    private final ChangeBroadcaster broadcaster;

    public MagicCityService(MagicCityRepository repository,
                            BookCreatureRepository creatureRepository,
                            ChangeBroadcaster broadcaster) {
        this.repository = repository;
        this.creatureRepository = creatureRepository;
        this.broadcaster = broadcaster;
    }

    /**
     * Возвращает все города в виде списка DTO (используется для выпадающих списков в формах).
     */
    @Transactional(readOnly = true)
    public List<MagicCityDto> findAll() {
        return repository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    /**
     * Возвращает страницу городов с пагинацией, сортировкой и фильтрацией.
     * Параметры: page, size, sortColumn, sortDir, filterColumn, filterValue.
     */
    @Transactional(readOnly = true)
    public List<MagicCityDto> findPaged(int page, int size, String sortColumn, String sortDir,
                                        String filterColumn, String filterValue) {
        return repository.findPaged(page, size, sortColumn, sortDir, filterColumn, filterValue).stream()
                .map(this::toDto).collect(Collectors.toList());
    }

    /**
     * Возвращает общее количество городов, удовлетворяющих фильтрам (для расчёта страниц).
     */
    @Transactional(readOnly = true)
    public long count(String filterColumn, String filterValue) {
        return repository.count(filterColumn, filterValue);
    }

    /**
     * Находит город по ID и возвращает его в виде {@link MagicCityDto}.
     * Бросает {@link EntityNotFoundException}, если город с таким ID не существует.
     */
    @Transactional(readOnly = true)
    public MagicCityDto findById(Long id) {
        MagicCity c = repository.findById(id);
        if (c == null) {
            throw new EntityNotFoundException("Город с id=" + id + " не найден");
        }
        return toDto(c);
    }

    /**
     * Создаёт новый город из {@link MagicCityDto}.
     * Поля: название, площадь, население, дата основания, тип правителя (хоббит/эльф/человек/голлум),
     * флаг столицы, плотность населения.
     * После сохранения рассылается WebSocket-уведомление через {@link #broadcast()}.
     */
    public MagicCityDto create(MagicCityDto dto) {
        MagicCity city = new MagicCity();
        city.setName(dto.getName());
        city.setArea(dto.getArea());
        city.setPopulation(dto.getPopulation());
        city.setEstablishmentDate(dto.getEstablishmentDate());
        city.setGovernor(dto.getGovernor() != null
                ? BookCreatureType.valueOf(dto.getGovernor().getCode()) : null);
        city.setCapital(dto.getCapital());
        city.setPopulationDensity(dto.getPopulationDensity());
        repository.save(city);
        broadcast();
        return toDto(city);
    }

    /**
     * Отправляет всем подключённым WebSocket-клиентам уведомление об изменении городов.
     */
    private void broadcast() {
        broadcaster.broadcastCreatureChanged();
    }

    /**
     * Обновляет город по ID данными из {@link MagicCityDto}.
     * Бросает {@link EntityNotFoundException}, если город не найден.
     * После сохранения рассылается WebSocket-уведомление.
     */
    public MagicCityDto update(Long id, MagicCityDto dto) {
        MagicCity city = repository.findById(id);
        if (city == null) {
            throw new EntityNotFoundException("Город с id=" + id + " не найден");
        }
        city.setName(dto.getName());
        city.setArea(dto.getArea());
        city.setPopulation(dto.getPopulation());
        city.setEstablishmentDate(dto.getEstablishmentDate());
        city.setGovernor(dto.getGovernor() != null
                ? BookCreatureType.valueOf(dto.getGovernor().getCode()) : null);
        city.setCapital(dto.getCapital());
        city.setPopulationDensity(dto.getPopulationDensity());
        repository.save(city);
        broadcast();
        return toDto(city);
    }

    /**
     * Удаляет город по ID.
     * Сначала проверяет существование (COUNT без загрузки сущности), затем выполняет bulk DELETE.
     * После удаления рассылается WebSocket-уведомление.
     */
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("Город с id=" + id + " не найден");
        }
        if (creatureRepository.existsReferencedByCity(id)) {
            throw new ReferencedEntityException(
                    "Невозможно удалить город id=" + id
                            + ": он используется одним или несколькими существами. "
                            + "Отсоедините город от существ перед удалением.");
        }
        repository.deleteById(id);
        broadcast();
    }

    /**
     * Преобразует сущность {@link MagicCity} в DTO {@link MagicCityDto}.
     * Если у города задан тип правителя, преобразует его в {@link BookCreatureTypeDto}.
     */
    private MagicCityDto toDto(MagicCity c) {
        MagicCityDto dto = new MagicCityDto();
        dto.setId(c.getId());
        dto.setName(c.getName());
        dto.setArea(c.getArea());
        dto.setPopulation(c.getPopulation());
        dto.setEstablishmentDate(c.getEstablishmentDate());
        if (c.getGovernor() != null) {
            BookCreatureTypeDto g = new BookCreatureTypeDto(c.getGovernor().name(), c.getGovernor().name());
            dto.setGovernor(g);
        }
        dto.setCapital(c.getCapital());
        dto.setPopulationDensity(c.getPopulationDensity());
        return dto;
    }
}
