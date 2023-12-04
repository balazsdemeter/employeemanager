package hu.cubix.employeemanager.service;

import hu.cubix.employeemanager.model.Employee;
import hu.cubix.employeemanager.repository.EmployeeRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

@Service
public class InitDbService {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    public InitDbService(EmployeeRepository employeeRepository, PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void createEmployeeIfNeeded() {

        Employee manager;
        Optional<Employee> optional = employeeRepository.findEmployeeByName("manager");
        if (optional.isEmpty()) {
            manager = new Employee();
            manager.setName("manager");
            manager.setUsername("admin");
            manager.setPassword(passwordEncoder.encode("pass"));
            manager.setRoles(Set.of("admin", "user"));
            employeeRepository.save(manager);
        } else {
            manager = optional.get();
        }

        if (employeeRepository.findEmployeeByName("employee1").isEmpty()) {
            manager.getInferiors().add(createEmployee("employee1", "user1", manager));
        }

        if (employeeRepository.findEmployeeByName("employee2").isEmpty()) {
            manager.getInferiors().add(createEmployee("employee2", "user2", manager));
        }
    }

    private Employee createEmployee(String name, String username, Employee manager) {
        Employee employee = new Employee();
        employee.setName(name);
        employee.setManager(manager);
        employee.setUsername(username);
        employee.setRoles(Set.of("user"));
        employee.setPassword(passwordEncoder.encode("pass"));
        employeeRepository.save(employee);
        return employee;
    }
}