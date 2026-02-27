package org.eventhub.eventhub.mapper;


import org.eventhub.eventhub.dto.users.UserRegisterRequestDto;
import org.eventhub.eventhub.entity.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "password", ignore = true) // encode edilecek, service'te
    User toEntity(UserRegisterRequestDto dto);

}
