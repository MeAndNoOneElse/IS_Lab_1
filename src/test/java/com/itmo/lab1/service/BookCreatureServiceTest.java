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
import com.itmo.lab1.repository.BookCreatureRepository;
import com.itmo.lab1.repository.CoordinatesRepository;
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
    private CoordinatesRepository coordinatesRepository;
    @Mock
    private MagicCityRepository magicCityRepository;
    @Mock
    private RingRepository ringRepository;
    @Mock
    private ChangeBroadcaster broadcaster;

    private BookCreatureService service;

    @BeforeEach
    void setUp() {
        service = new BookCreatureService(creatureRepository, coordinatesRepository,
                magicCityRepository, ringRepository, broadcaster);
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
        when(creatureRepository.save(any(BookCreature.class))).thenAnswer(invocation -> {
            BookCreature creature = invocation.getArgument(0);
            creature.setId(42L);
            return creature;
        });
        when(coordinatesRepository.save(any(Coordinates.class))).thenAnswer(invocation -> {
            Coordinates coords = invocation.getArgument(0);
            coords.setId(5L);
            return coords;
        });

        BookCreatureListDto result = service.create(newDto());

        assertEquals(42L, result.getId());
        assertEquals("Frodo", result.getName());
        assertEquals(30, result.getAge());
        assertNotNull(result.getCoordinates());
        assertEquals(10L, result.getCoordinates().getX());
        verify(creatureRepository).save(any(BookCreature.class));
        verify(coordinatesRepository).save(any(Coordinates.class));
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
