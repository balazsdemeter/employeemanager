package hu.cubix.employeemanager.repository;

import hu.cubix.employeemanager.model.Employee;
import hu.cubix.employeemanager.model.TimeOff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface TimeOffRepository extends JpaRepository<TimeOff, Long>, JpaSpecificationExecutor<TimeOff> {
    Optional<TimeOff> findByIdAndCreateEmployee(long id, Employee employee);
}