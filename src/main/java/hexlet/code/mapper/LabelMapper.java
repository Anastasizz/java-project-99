package hexlet.code.mapper;

import hexlet.code.dto.label.LabelCreateDTO;
import hexlet.code.dto.label.LabelDTO;
import hexlet.code.dto.label.LabelUpdateDTO;
import hexlet.code.model.Label;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public abstract class LabelMapper {
    @Mapping(source = "createdAt", target = "createdAt", dateFormat = "yyyy-MM-dd")
    public abstract LabelDTO map(Label label);

    @Mapping(source = "createdAt", target = "createdAt", dateFormat = "yyyy-MM-dd")
    public abstract Label map(LabelDTO labelDTO);

    public abstract Label map(LabelCreateDTO labelData);

    public abstract void update(LabelUpdateDTO labelData, @MappingTarget Label label);
}
