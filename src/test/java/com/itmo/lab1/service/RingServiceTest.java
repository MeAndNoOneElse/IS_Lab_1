package com.itmo.lab1.service;

import com.itmo.lab1.dto.RingDto;
import com.itmo.lab1.entity.Ring;
import com.itmo.lab1.exception.EntityNotFoundException;
import com.itmo.lab1.exception.ReferencedEntityException;
import com.itmo.lab1.repository.BookCreatureRepository;
import com.itmo.lab1.repository.RingRepository;
import com.itmo.lab1.websocket.ChangeBroadcaster;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Тесты {@link RingService} с моком репозитория (БД не требуется).
 */
@ExtendWith(MockitoExtension.class)
class RingServiceTest {

    @Mock
    private RingRepository repository;
    @Mock
    private BookCreatureRepository creatureRepository;
    @Mock
    private ChangeBroadcaster broadcaster;

    private RingService service;

    @BeforeEach
    void setUp() {
        service = new RingService(repository, creatureRepository, broadcaster);
    }

    private RingDto newDto() {
        RingDto dto = new RingDto();
        dto.setName("The One Ring");
        dto.setPower(100L);
        return dto;
    }

    @Test
    void createShouldSaveRingAndReturnDto() {
        when(repository.save(any(Ring.class))).thenAnswer(invocation -> {
            Ring ring = invocation.getArgument(0);
            ring.setId(3L);
            return ring;
        });

        RingDto result = service.create(newDto());

        assertEquals(3L, result.getId());
        assertEquals("The One Ring", result.getName());
        verify(repository).save(any(Ring.class));
    }

    @Test
    void findByIdShouldThrowWhenMissing() {
        when(repository.findById(9L)).thenReturn(null);
        assertThrows(EntityNotFoundException.class, () -> service.findById(9L));
    }

    @Test
    void findAllShouldMapToDtos() {
        Ring ring = new Ring();
        ring.setId(1L);
        ring.setName("Narya");
        when(repository.findAll()).thenReturn(List.of(ring));

        List<RingDto> result = service.findAll();

        assertEquals(1, result.size());
        assertEquals("Narya", result.get(0).getName());
    }

    @Test
    void updateShouldModifyExistingRing() {
        Ring ring = new Ring();
        ring.setId(1L);
        when(repository.findById(1L)).thenReturn(ring);

        RingDto dto = newDto();
        service.update(1L, dto);

        assertEquals("The One Ring", ring.getName());
        assertEquals(100L, ring.getPower());
        verify(repository).save(ring);
    }

    @Test
    void deleteShouldThrowWhenMissing() {
        when(repository.existsById(5L)).thenReturn(false);
        assertThrows(EntityNotFoundException.class, () -> service.delete(5L));
        verify(repository, never()).deleteById(5L);
        verify(broadcaster, never()).broadcastCreatureChanged();
    }

    @Test
    void deleteShouldThrowWhenReferencedByCreature() {
        when(repository.existsById(7L)).thenReturn(true);
        when(creatureRepository.existsReferencedByRing(7L)).thenReturn(true);

        assertThrows(ReferencedEntityException.class, () -> service.delete(7L));
        verify(repository, never()).deleteById(7L);
        verify(broadcaster, never()).broadcastCreatureChanged();
    }

    @Test
    void deleteShouldSucceedWhenNotReferenced() {
        when(repository.existsById(9L)).thenReturn(true);
        when(creatureRepository.existsReferencedByRing(9L)).thenReturn(false);

        service.delete(9L);

        verify(creatureRepository).existsReferencedByRing(9L);
        verify(repository).deleteById(9L);
        verify(broadcaster).broadcastCreatureChanged();
    }
}
