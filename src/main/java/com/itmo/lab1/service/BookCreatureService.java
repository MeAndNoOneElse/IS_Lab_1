package com.itmo.lab1.service;

import com.itmo.lab1.dto.BookCreatureDto;
import com.itmo.lab1.dto.BookCreatureListDto;
import com.itmo.lab1.dto.CoordinatesDto;
import com.itmo.lab1.entity.BookCreature;
import com.itmo.lab1.entity.BookCreatureType;
import com.itmo.lab1.entity.MagicCity;
import com.itmo.lab1.entity.Ring;
import com.itmo.lab1.exception.EntityNotFoundException;
import com.itmo.lab1.exception.ReferencedEntityException;
import com.itmo.lab1.mapper.BookCreatureMapper;
import com.itmo.lab1.repository.BookCreatureRepository;
import com.itmo.lab1.repository.MagicCityRepository;
import com.itmo.lab1.repository.RingRepository;
import com.itmo.lab1.websocket.ChangeBroadcaster;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Бизнес-логика существ (BookCreature): CRUD, пагинированный список с фильтрами и сортировкой,
 * проверка связанности перед удалением, привязка к существующим Coordinates / MagicCity / Ring.
 * <p>
 * Разделён по ответственности: работа с координатами делегирована {@link CoordinateService},
 * преобразование entity↔DTO — MapStruct-мапперу {@link BookCreatureMapper}.
 */
@Service
@Transactional
public class BookCreatureService {

    private final BookCreatureRepository creatureRepository;
    private final CoordinateService coordinateService;
    private final MagicCityRepository magicCityRepository;
    private final RingRepository ringRepository;
    private final BookCreatureMapper mapper;
    private final ChangeBroadcaster broadcaster;

    public BookCreatureService(BookCreatureRepository creatureRepository,
                               CoordinateService coordinateService,
                               MagicCityRepository magicCityRepository,
                               RingRepository ringRepository,
                               BookCreatureMapper mapper,
                               ChangeBroadcaster broadcaster) {
        this.creatureRepository = creatureRepository;
        this.coordinateService = coordinateService;
        this.magicCityRepository = magicCityRepository;
        this.ringRepository = ringRepository;
        this.mapper = mapper;
        this.broadcaster = broadcaster;
    }

    /**
     * Отправляет всем подключённым WebSocket-клиентам уведомление об изменении существ.
     * Вызывается после каждого успешного create / update / delete, чтобы интерфейс
     * обновился без перезагрузки страницы.
     */
    private void broadcast() {
        broadcaster.broadcastCreatureChanged();
    }

    /**
     * Возвращает страницу существ с применённой пагинацией, сортировкой и фильтрами.
     * Параметры: page, size, sortColumn, sortDir, filterColumn, filterValue,
     * maxDistance, creationFrom, creationTo, creatureLocationId, ringId.
     */
    public List<BookCreatureListDto> findPaged(int page, int size,
                                               String sortColumn, String sortDir,
                                               String filterColumn, String filterValue,
                                               String maxDistance,
                                               String creationFrom, String creationTo,
                                               String creatureLocationId, String ringId) {
        List<BookCreature> entities = creatureRepository.findPaged(page, size, sortColumn, sortDir,
                filterColumn, filterValue, maxDistance, creationFrom, creationTo,
                creatureLocationId, ringId);
        return entities.stream().map(mapper::toListDto).toList();
    }

    /**
     * Возвращает общее количество существ, удовлетворяющих фильтрам (нужно для расчёта
     * количества страниц на интерфейсе).
     */
    public long count(String filterColumn, String filterValue, String maxDistance,
                      String creationFrom, String creationTo,
                      String creatureLocationId, String ringId) {
        return creatureRepository.count(filterColumn, filterValue, maxDistance, creationFrom, creationTo,
                creatureLocationId, ringId);
    }

    /**
     * Находит существо по ID и возвращает его в виде {@link BookCreatureListDto}.
     * Бросает {@link EntityNotFoundException}, если существо с таким ID не существует.
     */
    public BookCreatureListDto findById(Long id) {
        BookCreature c = creatureRepository.findById(id);
        if (c == null) {
            throw new EntityNotFoundException("Существо с id=" + id + " не найдено");
        }
        return mapper.toListDto(c);
    }

    /**
     * Создаёт нового существа из {@link BookCreatureDto}.
     * <ul>
     *   <li>Если передан {@code creatureLocationId} — ищет город в БД;</li>
     *   <li>Если передан {@code ringId} — ищет кольцо в БД;</li>
     *   <li>Координаты разрешаются через {@link CoordinateService#resolve};</li>
     *   <li>Дата создания берётся из DTO, или текущая дата, если не указана;</li>
     *   <li>После сохранения рассылается WebSocket-уведомление.</li>
     * </ul>
     */
    public BookCreatureListDto create(BookCreatureDto dto) {
        BookCreature creature = mapper.toEntity(dto);
        creature.setName(dto.getName());
        creature.setAge(dto.getAge());
        creature.setAttackLevel(dto.getAttackLevel());
        creature.setDefenseLevel(dto.getDefenseLevel());
        creature.setCreationDate(dto.getCreationDate() != null ? dto.getCreationDate() : LocalDate.now());

        creature.setCoordinates(coordinateService.resolve(dto.getCoordinates()));

        if (dto.getCreatureLocationId() != null) {
            MagicCity city = magicCityRepository.findById(dto.getCreatureLocationId());
            if (city == null) {
                throw new EntityNotFoundException("Город с id=" + dto.getCreatureLocationId() + " не найден");
            }
            creature.setCreatureLocation(city);
        }

        if (dto.getRingId() != null) {
            Ring ring = ringRepository.findById(dto.getRingId());
            if (ring == null) {
                throw new EntityNotFoundException("Кольцо с id=" + dto.getRingId() + " не найдено");
            }
            creature.setRing(ring);
        }

        creatureRepository.save(creature);
        broadcast();
        return mapper.toListDto(creature);
    }

    /**
     * Обновляет существо по ID данными из {@link BookCreatureDto}.
     * <ul>
     *   <li>Загружает существо со всеми связям одним запросом ({@code findByIdWithRelations});</li>
     *   <li>Обновляет поля: имя, возраст, уровни атаки/защиты, тип существа;</li>
     *   <li>Координаты обновляются, если переданы;</li>
     *   <li>Город и кольцо обновляются, если переданы их ID; иначе обнуляются;</li>
     *   <li>После сохранения рассылается WebSocket-уведомление.</li>
     * </ul>
     */
    public BookCreatureListDto update(Long id, BookCreatureDto dto) {
        BookCreature creature = creatureRepository.findByIdWithRelations(id);
        if (creature == null) {
            throw new EntityNotFoundException("Существо с id=" + id + " не найдено");
        }
        creature.setName(dto.getName());
        creature.setAge(dto.getAge());
        creature.setAttackLevel(dto.getAttackLevel());
        creature.setDefenseLevel(dto.getDefenseLevel());
        String typeCode = dto.getCreatureType() != null ? dto.getCreatureType().getCode() : null;
        creature.setCreatureType(typeCode != null && !typeCode.isBlank()
                ? BookCreatureType.valueOf(typeCode) : null);

        if (dto.getCoordinates() != null) {
            creature.setCoordinates(coordinateService.resolve(dto.getCoordinates()));
        }

        if (dto.getCreatureLocationId() != null) {
            MagicCity city = magicCityRepository.findById(dto.getCreatureLocationId());
            if (city == null) {
                throw new EntityNotFoundException("Город с id=" + dto.getCreatureLocationId() + " не найден");
            }
            creature.setCreatureLocation(city);
        } else {
            creature.setCreatureLocation(null);
        }

        if (dto.getRingId() != null) {
            Ring ring = ringRepository.findById(dto.getRingId());
            if (ring == null) {
                throw new EntityNotFoundException("Кольцо с id=" + dto.getRingId() + " не найдено");
            }
            creature.setRing(ring);
        } else {
            creature.setRing(null);
        }

        creatureRepository.save(creature);
        broadcast();
        return mapper.toListDto(creature);
    }

    /**
     * Удаляет существо по ID.
     * <ul>
     *   <li>Проверяет существование (COUNT без загрузки сущности);</li>
     *   <li>Проверяет, не связано ли существо с городом или кольцом;</li>
     *   <li>Выполняет bulk DELETE;</li>
     *   <li>После удаления рассылается WebSocket-уведомление.</li>
     * </ul>
     */
    public void delete(Long id) {
        if (!creatureRepository.existsById(id)) {
            throw new EntityNotFoundException("Существо с id=" + id + " не найдено");
        }
        if (creatureRepository.existsReferencedByOthers(id)) {
            throw new ReferencedEntityException(
                    "Невозможно удалить существо id=" + id
                            + ": оно связано с городом или кольцом. Отсоедините их перед удалением.");
        }
        creatureRepository.deleteById(id);
        broadcast();
    }
}
