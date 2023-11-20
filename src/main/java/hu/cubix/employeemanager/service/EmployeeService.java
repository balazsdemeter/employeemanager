package hu.cubix.employeemanager.service;

import hu.cubix.employeemanager.model.Employee;
import hu.cubix.employeemanager.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmployeeService {
    private final EmployeeRepository employeeRepository;

    @Autowired
    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Transactional
    public Employee findOrCreateEmployeeByName(String name) {
        Employee employee = findEmployeeByName(name);
        if (employee != null) {
            return employee;
        }

        employee = new Employee(name);
        return employeeRepository.save(employee);
    }

    @Transactional
    public Employee findEmployeeByName(String name) {
        return employeeRepository.findEmployeeByName(name).orElse(null);
    }
}