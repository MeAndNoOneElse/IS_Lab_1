package com.itmo.lab1.service;

import com.itmo.lab1.dto.RingDto;
import com.itmo.lab1.entity.Ring;
import com.itmo.lab1.exception.EntityNotFoundException;
import com.itmo.lab1.exception.ReferencedEntityException;
import com.itmo.lab1.repository.RingRepository;
import com.itmo.lab1.websocket.ChangeBroadcaster;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RingService {

    private final RingRepository repository;
    private final ChangeBroadcaster broadcaster;

    public RingService(RingRepository repository,
                       ChangeBroadcaster broadcaster) {
        this.repository = repository;
        this.broadcaster = broadcaster;
    }

    @Transactional(readOnly = true)
    public List<RingDto> findAll() {
        return repository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RingDto> findPaged(int page, int size, String sortColumn, String sortDir,
                                   String filterColumn, String filterValue) {
        return repository.findPaged(page, size, sortColumn, sortDir, filterColumn, filterValue).stream()
                .map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long count(String filterColumn, String filterValue) {
        return repository.count(filterColumn, filterValue);
    }

    @Transactional(readOnly = true)
    public RingDto findById(Long id) {
        Ring r = repository.findById(id);
        if (r == null) {
            throw new EntityNotFoundException("Кольцо с id=" + id + " не найдено");
        }
        return toDto(r);
    }

    public RingDto create(RingDto dto) {
        Ring ring = new Ring();
        ring.setName(dto.getName());
        ring.setPower(dto.getPower());
        repository.save(ring);
        broadcast();
        return toDto(ring);
    }

    private void broadcast() {
        broadcaster.broadcastCreatureChanged();
    }

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

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("Кольцо с id=" + id + " не найдено");
        }
        repository.deleteById(id);
        broadcast();
    }

    private RingDto toDto(Ring r) {
        RingDto dto = new RingDto();
        dto.setId(r.getId());
        dto.setName(r.getName());
        dto.setPower(r.getPower());
        return dto;
    }
}
