package hu.cubix.employeemanager.mapper;

import hu.cubix.employeemanager.dto.EmployeeDto;
import hu.cubix.employeemanager.model.Employee;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EmployeeMapper {

    EmployeeDto employeeToDto(Employee employee);

    Employee dtoToEmployee(EmployeeDto employeeDto);
}
