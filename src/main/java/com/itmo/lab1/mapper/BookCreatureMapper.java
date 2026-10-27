package com.itmo.lab1.mapper;

import com.itmo.lab1.dto.BookCreatureDto;
import com.itmo.lab1.dto.BookCreatureListDto;
import com.itmo.lab1.dto.CoordinatesDto;
import com.itmo.lab1.entity.BookCreature;
import com.itmo.lab1.entity.Coordinates;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

/**
 * MapStruct-маппер для сущности {@link BookCreature} и её DTO.
 * Генерирует реализацию компонента типа Spring.
 */
@Mapper(componentModel = "spring")
public interface BookCreatureMapper {

    BookCreatureMapper INSTANCE = Mappers.getMapper(BookCreatureMapper.class);

    /**
     * Сущность → DTO для списка (BookCreatureListDto).
     * creatureType (Enum) → String, creatureLocation (MagicCity) → имя + id, ring → имя + id.
     */
    @Mapping(target = "creatureType", source = "creatureType", qualifiedByName = "typeToString")
    @Mapping(target = "creatureLocation", source = "creatureLocation.name")
    @Mapping(target = "creatureLocationId", source = "creatureLocation.id")
    @Mapping(target = "ring", source = "ring.name")
    @Mapping(target = "ringId", source = "ring.id")
    @Mapping(target = "coordinates", source = "coordinates")
    BookCreatureListDto toListDto(BookCreature creature);

    /**
     * Сущность → DTO для формы (BookCreatureDto).
     */
    @Mapping(target = "creatureType", source = "creatureType", qualifiedByName = "typeToDto")
    @Mapping(target = "creatureLocationId", source = "creatureLocation.id")
    @Mapping(target = "ringId", source = "ring.id")
    BookCreatureDto toDto(BookCreature creature);

    /**
     * DTO для формы → сущность (BookCreature).
     * creatureType (BookCreatureTypeDto) → BookCreatureType Enum.
     */
    @Mapping(target = "creatureType", source = "creatureType", qualifiedByName = "dtoToType")
    @Mapping(target = "creatureLocation", ignore = true) // заполняется отдельно в сервисе
    @Mapping(target = "ring", ignore = true) // заполняется отдельно в сервисе
    @Mapping(target = "id", ignore = true)
    BookCreature toEntity(BookCreatureDto dto);

    /**
     * Coordinates entity → CoordinatesDto.
     */
    @Named("coordinatesToDto")
    CoordinatesDto coordinatesToDto(Coordinates coordinates);

    /**
     * BookCreatureType Enum → String (имя константы).
     */
    @Named("typeToString")
    default String typeToString(com.itmo.lab1.entity.BookCreatureType type) {
        return type != null ? type.name() : null;
    }

    /**
     * BookCreatureType Enum → BookCreatureTypeDto.
     */
    @Named("typeToDto")
    default com.itmo.lab1.dto.BookCreatureTypeDto typeToDto(com.itmo.lab1.entity.BookCreatureType type) {
        return type != null
                ? new com.itmo.lab1.dto.BookCreatureTypeDto(type.name(), type.name())
                : null;
    }

    /**
     * BookCreatureTypeDto → BookCreatureType Enum.
     */
    @Named("dtoToType")
    default com.itmo.lab1.entity.BookCreatureType dtoToType(com.itmo.lab1.dto.BookCreatureTypeDto dto) {
        return dto != null
                ? com.itmo.lab1.entity.BookCreatureType.valueOf(dto.getCode())
                : null;
    }
}
