package hu.cubix.employeemanager.mapper;

import hu.cubix.employeemanager.dto.TimeOffDto;
import hu.cubix.employeemanager.model.TimeOff;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TimeOffMapper {
    @Mapping(target = "id", source = "timeOff.id")
    @Mapping(target = "createEmployeeName", source = "timeOff.createEmployee.name")
    @Mapping(target = "createDate", source = "timeOff.createDate")
    @Mapping(target = "modifyEmployeeName", source = "timeOff.modifyEmployee.name")
    @Mapping(target = "modifyDate", source = "timeOff.modifyDate")
    @Mapping(target = "startDate", source = "timeOff.startDate")
    @Mapping(target = "endDate", source = "timeOff.endDate")
    @Mapping(target = "status", source = "timeOff.status")
    TimeOffDto entityToDto(TimeOff timeOff);

    List<TimeOffDto> entitiesToDtos(List<TimeOff> entities);
}