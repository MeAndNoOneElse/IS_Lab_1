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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Тесты {@link MagicCityService} с моком репозитория (БД не требуется).
 */
@ExtendWith(MockitoExtension.class)
class MagicCityServiceTest {

    @Mock
    private MagicCityRepository repository;
    @Mock
    private BookCreatureRepository creatureRepository;
    @Mock
    private ChangeBroadcaster broadcaster;

    private MagicCityService service;

    @BeforeEach
    void setUp() {
        service = new MagicCityService(repository, creatureRepository, broadcaster);
    }

    private MagicCityDto newDto() {
        MagicCityDto dto = new MagicCityDto();
        dto.setName("Rivendell");
        dto.setArea(100.5f);
        dto.setPopulation(5000L);
        dto.setEstablishmentDate(new Date());
        dto.setGovernor(new BookCreatureTypeDto("ELF", "Эльф"));
        dto.setCapital(Boolean.TRUE);
        dto.setPopulationDensity(49.75);
        return dto;
    }

    @Test
    void createShouldSaveCityAndReturnDto() {
        when(repository.save(any(MagicCity.class))).thenAnswer(invocation -> {
            MagicCity city = invocation.getArgument(0);
            city.setId(11L);
            return city;
        });

        MagicCityDto result = service.create(newDto());

        assertEquals(11L, result.getId());
        assertEquals("Rivendell", result.getName());
        verify(repository).save(any(MagicCity.class));
    }

    @Test
    void findByIdShouldThrowWhenMissing() {
        when(repository.findById(5L)).thenReturn(null);
        assertThrows(EntityNotFoundException.class, () -> service.findById(5L));
    }

    @Test
    void findAllShouldMapToDtos() {
        MagicCity city = new MagicCity();
        city.setId(1L);
        city.setName("Gondor");
        when(repository.findAll()).thenReturn(List.of(city));

        List<MagicCityDto> result = service.findAll();

        assertEquals(1, result.size());
        assertEquals("Gondor", result.get(0).getName());
    }

    @Test
    void deleteShouldCallExistsByIdAndDeleteById() {
        when(repository.existsById(1L)).thenReturn(true);
        when(creatureRepository.existsReferencedByCity(1L)).thenReturn(false);

        service.delete(1L);

        verify(repository).existsById(1L);
        verify(creatureRepository).existsReferencedByCity(1L);
        verify(repository).deleteById(1L);
        verify(broadcaster).broadcastCreatureChanged();
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
        when(creatureRepository.existsReferencedByCity(7L)).thenReturn(true);

        assertThrows(ReferencedEntityException.class, () -> service.delete(7L));
        verify(repository, never()).deleteById(7L);
        verify(broadcaster, never()).broadcastCreatureChanged();
    }

    @Test
    void findPagedShouldMapToDtos() {
        MagicCity city = new MagicCity();
        city.setId(1L);
        city.setName("Gondor");
        when(repository.findPaged(1, 10, "name", "asc", null, null)).thenReturn(List.of(city));

        List<MagicCityDto> result = service.findPaged(1, 10, "name", "asc", null, null);

        assertEquals(1, result.size());
        assertEquals("Gondor", result.get(0).getName());
    }

    @Test
    void countShouldReturnRepositoryCount() {
        when(repository.count("name", "Gondor")).thenReturn(7L);

        long result = service.count("name", "Gondor");

        assertEquals(7L, result);
    }
}
