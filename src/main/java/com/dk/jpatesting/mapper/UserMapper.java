package com.dk.jpatesting.mapper;

import com.dk.jpatesting.dto.request.CreateUserRequest;
import com.dk.jpatesting.dto.request.UpdateUserRequest;
import com.dk.jpatesting.dto.response.UserResponse;
import com.dk.jpatesting.entity.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface UserMapper {

    // CreateUserRequest → User entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User toEntity(CreateUserRequest request);

    // User entity → UserResponse
    UserResponse toResponse(User user);

    // UpdateUserRequest → existing User entity (only update non-null fields)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(UpdateUserRequest request, @MappingTarget User user);
}
