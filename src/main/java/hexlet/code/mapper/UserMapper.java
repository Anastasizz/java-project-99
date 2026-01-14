package hexlet.code.mapper;

import hexlet.code.dto.UserCreateDTO;
import hexlet.code.dto.UserDTO;
import hexlet.code.dto.UserUpdateDTO;
import hexlet.code.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(
        uses = { JsonNullableMapper.class },
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public abstract class UserMapper {
    @Mapping(source = "createdAt", target = "createdAt", dateFormat = "yyyy-MM-dd")
    public abstract UserDTO map(User user);

    @Mapping(source = "createdAt", target = "createdAt", dateFormat = "yyyy-MM-dd")
    public abstract User map(UserDTO userDTO);

    @Mapping(source = "password",target = "passwordDigest")
    public abstract User map(UserCreateDTO userData);

    @Mapping(source = "password",target = "passwordDigest")
    public abstract void update(UserUpdateDTO userData, @MappingTarget User user);
}
