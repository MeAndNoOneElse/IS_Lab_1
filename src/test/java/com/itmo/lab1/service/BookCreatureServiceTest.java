package com.itmo.lab1.service;

import com.itmo.lab1.dto.BookCreatureDto;
import com.itmo.lab1.dto.BookCreatureListDto;
import com.itmo.lab1.dto.CoordinatesDto;
import com.itmo.lab1.dto.BookCreatureTypeDto;
import com.itmo.lab1.entity.BookCreature;
import com.itmo.lab1.entity.BookCreatureType;
import com.itmo.lab1.entity.Coordinates;
import com.itmo.lab1.entity.MagicCity;
import com.itmo.lab1.entity.Ring;
import com.itmo.lab1.exception.EntityNotFoundException;
import com.itmo.lab1.exception.ReferencedEntityException;
import com.itmo.lab1.mapper.BookCreatureMapper;
import com.itmo.lab1.repository.BookCreatureRepository;
import com.itmo.lab1.repository.MagicCityRepository;
import com.itmo.lab1.repository.RingRepository;
import com.itmo.lab1.websocket.ChangeBroadcaster;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Тесты {@link BookCreatureService} с моками репозиториев (БД не требуется).
 */
@ExtendWith(MockitoExtension.class)
class BookCreatureServiceTest {

    @Mock
    private BookCreatureRepository creatureRepository;
    @Mock
    private CoordinateService coordinateService;
    @Mock
    private MagicCityRepository magicCityRepository;
    @Mock
    private RingRepository ringRepository;
    @Mock
    private BookCreatureMapper mapper;
    @Mock
    private ChangeBroadcaster broadcaster;

    private BookCreatureService service;

    @BeforeEach
    void setUp() {
        service = new BookCreatureService(creatureRepository, coordinateService,
                magicCityRepository, ringRepository, mapper, broadcaster);
    }

    private BookCreatureDto newDto() {
        BookCreatureDto dto = new BookCreatureDto();
        dto.setName("Frodo");
        CoordinatesDto coords = new CoordinatesDto();
        coords.setX(10L);
        coords.setY(20L);
        dto.setCoordinates(coords);
        dto.setAge(30);
        dto.setAttackLevel(10);
        dto.setDefenseLevel(5.5f);
        dto.setCreatureType(new BookCreatureTypeDto("HOBBIT", "Хоббит"));
        dto.setCreationDate(LocalDate.of(2024, 1, 1));
        return dto;
    }

    @Test
    void createShouldSaveCreatureCoordinatesAndBroadcast() {
        when(mapper.toEntity(any(BookCreatureDto.class))).thenReturn(new BookCreature());
        when(coordinateService.resolve(any(CoordinatesDto.class))).thenAnswer(invocation -> {
            CoordinatesDto cd = invocation.getArgument(0);
            Coordinates c = new Coordinates(cd.getX(), cd.getY());
            c.setId(5L);
            return c;
        });
        when(creatureRepository.save(any(BookCreature.class))).thenAnswer(invocation -> {
            BookCreature creature = invocation.getArgument(0);
            creature.setId(42L);
            return creature;
        });
        when(mapper.toListDto(any(BookCreature.class))).thenAnswer(invocation -> {
            BookCreature c = invocation.getArgument(0);
            BookCreatureListDto dto = new BookCreatureListDto();
            dto.setId(c.getId());
            dto.setName(c.getName());
            return dto;
        });

        BookCreatureListDto result = service.create(newDto());

        assertEquals(42L, result.getId());
        assertEquals("Frodo", result.getName());
        verify(creatureRepository).save(any(BookCreature.class));
        verify(coordinateService).resolve(any(CoordinatesDto.class));
        verify(broadcaster).broadcastCreatureChanged();
    }

    @Test
    void findByIdShouldThrowEntityNotFoundWhenMissing() {
        when(creatureRepository.findById(1L)).thenReturn(null);

        assertThrows(EntityNotFoundException.class, () -> service.findById(1L));
    }

    @Test
    void deleteShouldThrowReferencedEntityExceptionWhenCreatureHasLocationOrRing() {
        when(creatureRepository.existsById(1L)).thenReturn(true);
        when(creatureRepository.existsReferencedByOthers(1L)).thenReturn(true);

        assertThrows(ReferencedEntityException.class, () -> service.delete(1L));
        verify(creatureRepository, never()).deleteById(anyLong());
    }

    @Test
    void deleteShouldRemoveCreatureAndBroadcastWhenNotReferenced() {
        when(creatureRepository.existsById(1L)).thenReturn(true);
        when(creatureRepository.existsReferencedByOthers(1L)).thenReturn(false);

        service.delete(1L);

        verify(creatureRepository).deleteById(1L);
        verify(broadcaster).broadcastCreatureChanged();
    }

    @Test
    void updateShouldReplaceRingAndBroadcast() {
        BookCreature creature = new BookCreature();
        creature.setId(1L);
        Ring ring = new Ring();
        ring.setId(7L);
        when(creatureRepository.findByIdWithRelations(1L)).thenReturn(creature);
        when(ringRepository.findById(7L)).thenReturn(ring);
        when(mapper.toListDto(any(BookCreature.class))).thenAnswer(invocation -> {
            BookCreature c = invocation.getArgument(0);
            BookCreatureListDto dto = new BookCreatureListDto();
            dto.setId(c.getId());
            dto.setName(c.getName());
            return dto;
        });

        BookCreatureDto dto = newDto();
        dto.setRingId(7L);

        service.update(1L, dto);

        assertEquals(ring, creature.getRing());
        verify(creatureRepository).save(creature);
        verify(broadcaster).broadcastCreatureChanged();
    }

    @Test
    void findPagedShouldDelegateToRepository() {
        when(creatureRepository.findPaged(1, 10, "id", "asc", null, null, null, null, null,
                null, null))
                .thenReturn(List.of());

        List<BookCreatureListDto> result = service.findPaged(1, 10, "id", "asc", null, null,
                null, null, null, null, null);

        assertTrue(result.isEmpty());
        verify(creatureRepository).findPaged(1, 10, "id", "asc", null, null, null, null, null,
                null, null);
    }

    @Test
    void countShouldDelegateToRepository() {
        when(creatureRepository.count(null, null, null, null, null, null, null)).thenReturn(0L);

        long total = service.count(null, null, null, null, null, null, null);

        assertEquals(0L, total);
        verify(creatureRepository).count(null, null, null, null, null, null, null);
    }
}
