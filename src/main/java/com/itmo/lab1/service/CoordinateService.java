package com.itmo.lab1.service;

import com.itmo.lab1.dto.CoordinatesDto;
import com.itmo.lab1.entity.Coordinates;
import com.itmo.lab1.exception.EntityNotFoundException;
import com.itmo.lab1.repository.CoordinatesRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Бизнес-логика координат: разрешение CoordinatesDto в сущность Coordinates.
 * <p>
 * Выделено из {@link com.itmo.lab1.service.BookCreatureService} в соответствии с
 * recomendation по разделению перегруженного сервиса.
 */
@Service
@Transactional
public class CoordinateService {

    private final CoordinatesRepository coordinatesRepository;

    public CoordinateService(CoordinatesRepository coordinatesRepository) {
        this.coordinatesRepository = coordinatesRepository;
    }

    /**
     * Разрешает координаты из DTO:
     * <ul>
     *   <li>Если {@code coordsDto} null — бросает {@link EntityNotFoundException};</li>
     *   <li>Если в DTO есть ID — ищет существующие координаты, обновляет X/Y;</li>
     *   <li>Если ID нет — создаёт новые координаты;</li>
     * </ul>
     */
    public Coordinates resolve(CoordinatesDto coordsDto) {
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
}
