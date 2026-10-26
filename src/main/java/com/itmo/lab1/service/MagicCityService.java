package com.itmo.lab1.service;

import com.itmo.lab1.dto.BookCreatureTypeDto;
import com.itmo.lab1.dto.MagicCityDto;
import com.itmo.lab1.entity.BookCreatureType;
import com.itmo.lab1.entity.MagicCity;
import com.itmo.lab1.exception.EntityNotFoundException;
import com.itmo.lab1.exception.ReferencedEntityException;
import com.itmo.lab1.repository.MagicCityRepository;
import com.itmo.lab1.websocket.ChangeBroadcaster;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MagicCityService {

    private final MagicCityRepository repository;
    private final ChangeBroadcaster broadcaster;

    public MagicCityService(MagicCityRepository repository,
                            ChangeBroadcaster broadcaster) {
        this.repository = repository;
        this.broadcaster = broadcaster;
    }

    @Transactional(readOnly = true)
    public List<MagicCityDto> findAll() {
        return repository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MagicCityDto> findPaged(int page, int size, String sortColumn, String sortDir,
                                        String filterColumn, String filterValue) {
        return repository.findPaged(page, size, sortColumn, sortDir, filterColumn, filterValue).stream()
                .map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long count(String filterColumn, String filterValue) {
        return repository.count(filterColumn, filterValue);
    }

    @Transactional(readOnly = true)
    public MagicCityDto findById(Long id) {
        MagicCity c = repository.findById(id);
        if (c == null) {
            throw new EntityNotFoundException("Город с id=" + id + " не найден");
        }
        return toDto(c);
    }

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

    private void broadcast() {
        broadcaster.broadcastCreatureChanged();
    }

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

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("Город с id=" + id + " не найден");
        }
        repository.deleteById(id);
        broadcast();
    }

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
