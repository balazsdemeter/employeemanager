package hu.cubix.employeemanager.service;

import hu.cubix.employeemanager.dto.EmployeeDto;
import hu.cubix.employeemanager.exception.EmployeeNotFoundException;
import hu.cubix.employeemanager.mapper.EmployeeMapper;
import hu.cubix.employeemanager.model.Employee;
import hu.cubix.employeemanager.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;

    public EmployeeService(EmployeeRepository employeeRepository,
                           EmployeeMapper employeeMapper) {
        this.employeeRepository = employeeRepository;
        this.employeeMapper = employeeMapper;
    }

    @Transactional
    public EmployeeDto createEmployee(EmployeeDto employeeDto) {
        Employee employee = employeeMapper.dtoToEmployee(employeeDto);
        return employeeMapper.employeeToDto(employeeRepository.save(employee));
    }

    @Transactional
    public EmployeeDto createEmployee(EmployeeDto employeeDto, Long managerId) {
        Employee employee = employeeMapper.dtoToEmployee(employeeDto);

        if (managerId != null) {
            employeeRepository.findById(managerId).ifPresent(employee::setManager);
        }
        return employeeMapper.employeeToDto(employeeRepository.save(employee));
    }

    public Employee findById(Long id) {
        return employeeRepository.findById(id).orElseThrow(EmployeeNotFoundException::new);
    }
}