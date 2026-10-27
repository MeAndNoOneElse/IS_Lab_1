package com.itmo.lab1.mapper;

import com.itmo.lab1.dto.RingDto;
import com.itmo.lab1.entity.Ring;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * MapStruct-маппер для сущности {@link Ring} и её DTO.
 */
@Mapper(componentModel = "spring")
public interface RingMapper {

    RingMapper INSTANCE = Mappers.getMapper(RingMapper.class);

    RingDto toDto(Ring ring);

    Ring toEntity(RingDto dto);
}
