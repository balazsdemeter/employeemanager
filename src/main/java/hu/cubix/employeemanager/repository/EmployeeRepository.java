package hu.cubix.employeemanager.repository;

import hu.cubix.employeemanager.model.Employee;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findEmployeeByName(String name);

    @EntityGraph(attributePaths = {"inferiors", "roles", "manager"})
    @Query("SELECT e FROM Employee e WHERE e.username=:username")
    Optional<Employee> findEmployeeByUsername(String username);
}