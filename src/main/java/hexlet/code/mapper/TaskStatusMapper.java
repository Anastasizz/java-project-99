package hexlet.code.mapper;

import hexlet.code.dto.TaskStatusCreateDTO;
import hexlet.code.dto.TaskStatusDTO;
import hexlet.code.dto.TaskStatusUpdateDTO;
import hexlet.code.model.TaskStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(
        uses = { JsonNullableMapper.class },
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public abstract class TaskStatusMapper {
    @Mapping(source = "createdAt", target = "createdAt", dateFormat = "yyyy-MM-dd")
    public abstract TaskStatus map(TaskStatusDTO taskData);

    @Mapping(source = "createdAt", target = "createdAt", dateFormat = "yyyy-MM-dd")
    public abstract TaskStatusDTO map(TaskStatus taskStatus);

    public abstract TaskStatus map(TaskStatusCreateDTO data);

    public abstract void update(TaskStatusUpdateDTO taskStatusData, @MappingTarget TaskStatus taskStatus);
}
