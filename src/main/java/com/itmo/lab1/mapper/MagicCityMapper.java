package com.itmo.lab1.mapper;

import com.itmo.lab1.dto.MagicCityDto;
import com.itmo.lab1.entity.MagicCity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * MapStruct-маппер для сущности {@link MagicCity} и её DTO.
 */
@Mapper(componentModel = "spring")
public interface MagicCityMapper {

    MagicCityMapper INSTANCE = Mappers.getMapper(MagicCityMapper.class);

    /**
     * Сущность → DTO. governor (BookCreatureType Enum) → BookCreatureTypeDto.
     */
    @Mapping(target = "governor", source = "governor", qualifiedByName = "typeToDto")
    MagicCityDto toDto(MagicCity city);

    /**
     * DTO → сущность. governor (BookCreatureTypeDto) → BookCreatureType Enum.
     */
    @Mapping(target = "governor", source = "governor", qualifiedByName = "dtoToType")
    MagicCity toEntity(MagicCityDto dto);

    /**
     * BookCreatureType Enum → BookCreatureTypeDto.
     */
    @org.mapstruct.Named("typeToDto")
    default com.itmo.lab1.dto.BookCreatureTypeDto typeToDto(com.itmo.lab1.entity.BookCreatureType type) {
        return type != null
                ? new com.itmo.lab1.dto.BookCreatureTypeDto(type.name(), type.name())
                : null;
    }

    /**
     * BookCreatureTypeDto → BookCreatureType Enum.
     */
    @org.mapstruct.Named("dtoToType")
    default com.itmo.lab1.entity.BookCreatureType dtoToType(com.itmo.lab1.dto.BookCreatureTypeDto dto) {
        return dto != null
                ? com.itmo.lab1.entity.BookCreatureType.valueOf(dto.getCode())
                : null;
    }
}
