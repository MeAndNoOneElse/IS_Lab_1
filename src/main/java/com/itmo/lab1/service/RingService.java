package com.itmo.lab1.service;

import com.itmo.lab1.dto.RingDto;
import com.itmo.lab1.entity.Ring;
import com.itmo.lab1.exception.EntityNotFoundException;
import com.itmo.lab1.exception.ReferencedEntityException;
import com.itmo.lab1.repository.BookCreatureRepository;
import com.itmo.lab1.repository.RingRepository;
import com.itmo.lab1.websocket.ChangeBroadcaster;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Бизнес-логика колец (Ring): CRUD, пагинированный список с фильтрами и сортировкой, поиск по ID.
 * <p>
 * Кольцо имеет два поля: название и силу (power). Используется в связях с существами
 * (существо может иметь одно кольцо).
 */
@Service
@Transactional
public class RingService {

    private final RingRepository repository;
    private final BookCreatureRepository creatureRepository;
    private final ChangeBroadcaster broadcaster;

    public RingService(RingRepository repository,
                       BookCreatureRepository creatureRepository,
                       ChangeBroadcaster broadcaster) {
        this.repository = repository;
        this.creatureRepository = creatureRepository;
        this.broadcaster = broadcaster;
    }

    /**
     * Возвращает все кольца в виде списка DTO (для выпадающих списков в формах).
     */
    @Transactional(readOnly = true)
    public List<RingDto> findAll() {
        return repository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    /**
     * Возвращает страницу колец с пагинацией, сортировкой и фильтрацией.
     */
    @Transactional(readOnly = true)
    public List<RingDto> findPaged(int page, int size, String sortColumn, String sortDir,
                                   String filterColumn, String filterValue) {
        return repository.findPaged(page, size, sortColumn, sortDir, filterColumn, filterValue).stream()
                .map(this::toDto).collect(Collectors.toList());
    }

    /**
     * Возвращает общее количество колец, удовлетворяющих фильтрам (для расчёта страниц).
     */
    @Transactional(readOnly = true)
    public long count(String filterColumn, String filterValue) {
        return repository.count(filterColumn, filterValue);
    }

    /**
     * Находит кольцо по ID и возвращает его в виде {@link RingDto}.
     * Бросает {@link EntityNotFoundException}, если кольцо с таким ID не существует.
     */
    @Transactional(readOnly = true)
    public RingDto findById(Long id) {
        Ring r = repository.findById(id);
        if (r == null) {
            throw new EntityNotFoundException("Кольцо с id=" + id + " не найдено");
        }
        return toDto(r);
    }

    /**
     * Создаёт новое кольцо из {@link RingDto}.
     * Поля: название и сила.
     * После сохранения рассылается WebSocket-уведомление через {@link #broadcast()}.
     */
    public RingDto create(RingDto dto) {
        Ring ring = new Ring();
        ring.setName(dto.getName());
        ring.setPower(dto.getPower());
        repository.save(ring);
        broadcast();
        return toDto(ring);
    }

    /**
     * Отправляет всем подключённым WebSocket-клиентам уведомление об изменении колец.
     */
    private void broadcast() {
        broadcaster.broadcastCreatureChanged();
    }

    /**
     * Обновляет кольцо по ID данными из {@link RingDto}.
     * Бросает {@link EntityNotFoundException}, если кольцо не найдено.
     * После сохранения рассылается WebSocket-уведомление.
     */
    public RingDto update(Long id, RingDto dto) {
        Ring ring = repository.findById(id);
        if (ring == null) {
            throw new EntityNotFoundException("Кольцо с id=" + id + " не найдено");
        }
        ring.setName(dto.getName());
        ring.setPower(dto.getPower());
        repository.save(ring);
        broadcast();
        return toDto(ring);
    }

    /**
     * Удаляет кольцо по ID.
     * Сначала проверяет существование (COUNT без загрузки сущности), затем выполняет bulk DELETE.
     * После удаления рассылается WebSocket-уведомление.
     */
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("Кольцо с id=" + id + " не найдено");
        }
        if (creatureRepository.existsReferencedByRing(id)) {
            throw new ReferencedEntityException(
                    "Невозможно удалить кольцо id=" + id
                            + ": оно используется одним или несколькими существами. "
                            + "Отсоедините кольцо от существ перед удалением.");
        }
        repository.deleteById(id);
        broadcast();
    }

    /**
     * Преобразует сущность {@link Ring} в DTO {@link RingDto}.
     */
    private RingDto toDto(Ring r) {
        RingDto dto = new RingDto();
        dto.setId(r.getId());
        dto.setName(r.getName());
        dto.setPower(r.getPower());
        return dto;
    }
}
