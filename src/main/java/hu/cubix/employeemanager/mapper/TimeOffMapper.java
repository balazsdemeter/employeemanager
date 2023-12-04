package hu.cubix.employeemanager.mapper;

import hu.cubix.employeemanager.dto.TimeOffDto;
import hu.cubix.employeemanager.model.TimeOff;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TimeOffMapper {
    @Mapping(target = "createEmployeeName", source = "timeOff.createEmployee.name")
    @Mapping(target = "modifyEmployeeName", source = "timeOff.modifyEmployee.name")
    TimeOffDto entityToDto(TimeOff timeOff);

    List<TimeOffDto> entitiesToDtos(List<TimeOff> entities);
}