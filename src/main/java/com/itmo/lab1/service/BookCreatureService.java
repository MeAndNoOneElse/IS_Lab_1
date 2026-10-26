package com.itmo.lab1.service;

import com.itmo.lab1.dto.BookCreatureDto;
import com.itmo.lab1.dto.BookCreatureListDto;
import com.itmo.lab1.dto.CoordinatesDto;
import com.itmo.lab1.entity.BookCreature;
import com.itmo.lab1.entity.BookCreatureType;
import com.itmo.lab1.entity.Coordinates;
import com.itmo.lab1.entity.MagicCity;
import com.itmo.lab1.entity.Ring;
import com.itmo.lab1.exception.EntityNotFoundException;
import com.itmo.lab1.exception.ReferencedEntityException;
import com.itmo.lab1.repository.BookCreatureRepository;
import com.itmo.lab1.repository.CoordinatesRepository;
import com.itmo.lab1.repository.MagicCityRepository;
import com.itmo.lab1.repository.RingRepository;
import com.itmo.lab1.websocket.ChangeBroadcaster;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Бизнес-логика BookCreature: CRUD, пагинированный список с фильтром/сортировкой,
 * проверка связанности перед удалением. Связывание с существующими Coordinates / MagicCity / Ring.
 */
@Service
@Transactional
public class BookCreatureService {

    private final BookCreatureRepository creatureRepository;
    private final CoordinatesRepository coordinatesRepository;
    private final MagicCityRepository magicCityRepository;
    private final RingRepository ringRepository;
    private final ChangeBroadcaster broadcaster;

    public BookCreatureService(BookCreatureRepository creatureRepository,
                               CoordinatesRepository coordinatesRepository,
                               MagicCityRepository magicCityRepository,
                               RingRepository ringRepository,
                               ChangeBroadcaster broadcaster) {
        this.creatureRepository = creatureRepository;
        this.coordinatesRepository = coordinatesRepository;
        this.magicCityRepository = magicCityRepository;
        this.ringRepository = ringRepository;
        this.broadcaster = broadcaster;
    }

    private void broadcast() {
        broadcaster.broadcastCreatureChanged();
    }

    public List<BookCreatureListDto> findPaged(int page, int size,
                                               String sortColumn, String sortDir,
                                               String filterColumn, String filterValue,
                                               String maxDistance,
                                               String creationFrom, String creationTo,
                                               String creatureLocationId, String ringId) {
        List<BookCreature> entities = creatureRepository.findPaged(page, size, sortColumn, sortDir,
                filterColumn, filterValue, maxDistance, creationFrom, creationTo,
                creatureLocationId, ringId);
        return entities.stream().map(this::toListDto).collect(Collectors.toList());
    }

    public long count(String filterColumn, String filterValue, String maxDistance,
                      String creationFrom, String creationTo,
                      String creatureLocationId, String ringId) {
        return creatureRepository.count(filterColumn, filterValue, maxDistance, creationFrom, creationTo,
                creatureLocationId, ringId);
    }

    public BookCreatureListDto findById(Long id) {
        BookCreature c = creatureRepository.findById(id);
        if (c == null) {
            throw new EntityNotFoundException("Существо с id=" + id + " не найдено");
        }
        return toListDto(c);
    }

    public BookCreatureListDto create(BookCreatureDto dto) {
        BookCreature creature = new BookCreature();
        creature.setName(dto.getName());
        creature.setAge(dto.getAge());
        creature.setAttackLevel(dto.getAttackLevel());
        creature.setDefenseLevel(dto.getDefenseLevel());
        String typeCode = dto.getCreatureType() != null ? dto.getCreatureType().getCode() : null;
        creature.setCreatureType(typeCode != null && !typeCode.isBlank()
                ? BookCreatureType.valueOf(typeCode) : null);
        creature.setCreationDate(dto.getCreationDate() != null ? dto.getCreationDate() : LocalDate.now());

        creature.setCoordinates(resolveCoordinates(dto.getCoordinates()));

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
        return toListDto(creature);
    }

    public BookCreatureListDto update(Long id, BookCreatureDto dto) {
        // Загружаем существо со всеми связями одним запросом — избегаем N+1
        // при доступе к координатам, городу и кольцу.
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
            creature.setCoordinates(resolveCoordinates(dto.getCoordinates()));
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
        return toListDto(creature);
    }

    public void delete(Long id) {
        // Проверка существования — легковесный COUNT, без загрузки сущности.
        if (!creatureRepository.existsById(id)) {
            throw new EntityNotFoundException("Существо с id=" + id + " не найдено");
        }
        // Проверка связанности — легковесный COUNT с фильтром по FK, без загрузки сущности.
        if (creatureRepository.existsReferencedByOthers(id)) {
            throw new ReferencedEntityException(
                    "Невозможно удалить существо id=" + id
                            + ": оно связано с городом или кольцом. Отсоедините их перед удалением.");
        }
        // Bulk delete — один DELETE-запрос вместо SELECT + DELETE.
        creatureRepository.deleteById(id);
        broadcast();
    }

    /**
     * Разрешить координаты существа из DTO: существующие (по id) обновляются,
     * новые создаются и сохраняются в БД.
     */
    private Coordinates resolveCoordinates(CoordinatesDto coordsDto) {
        if (coordsDto == null) {
            throw new EntityNotFoundException("Координаты не указаны");
        }
        Coordinates coords;
        if (coordsDto.getId() != null) {
            coords = coordinatesRepository.findById(coordsDto.getId());
            if (coords == null) {
                throw new EntityNotFoundException("Координаты с id=" + coordsDto.getId() + " не найдены");
            }
            coords.setX(coordsDto.getX());
            coords.setY(coordsDto.getY());
        } else {
            coords = new Coordinates(coordsDto.getX(), coordsDto.getY());
            coords = coordinatesRepository.save(coords);
        }
        return coords;
    }

    private BookCreatureListDto toListDto(BookCreature c) {
        BookCreatureListDto dto = new BookCreatureListDto();
        dto.setId(c.getId());
        dto.setName(c.getName());
        if (c.getCoordinates() != null) {
            CoordinatesDto cd = new CoordinatesDto();
            cd.setId(c.getCoordinates().getId());
            cd.setX(c.getCoordinates().getX());
            cd.setY(c.getCoordinates().getY());
            dto.setCoordinates(cd);
        }
        dto.setCreationDate(c.getCreationDate());
        dto.setAge(c.getAge());
        dto.setCreatureType(c.getCreatureType() != null ? c.getCreatureType().name() : null);
        dto.setCreatureLocation(c.getCreatureLocation() != null ? c.getCreatureLocation().getName() : null);
        dto.setAttackLevel(c.getAttackLevel());
        dto.setDefenseLevel(c.getDefenseLevel());
        dto.setRing(c.getRing() != null ? c.getRing().getName() : null);
        return dto;
    }
}
