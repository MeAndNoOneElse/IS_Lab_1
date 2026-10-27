package com.itmo.lab1.mapper;

import com.itmo.lab1.dto.CoordinatesDto;
import com.itmo.lab1.entity.Coordinates;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * MapStruct-маппер для сущности {@link Coordinates} и её DTO.
 */
@Mapper(componentModel = "spring")
public interface CoordinatesMapper {

    CoordinatesMapper INSTANCE = Mappers.getMapper(CoordinatesMapper.class);

    CoordinatesDto toDto(Coordinates coordinates);

    Coordinates toEntity(CoordinatesDto dto);
}
