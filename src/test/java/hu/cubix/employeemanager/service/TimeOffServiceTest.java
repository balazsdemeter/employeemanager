package hu.cubix.employeemanager.service;

import hu.cubix.employeemanager.dto.EmployeeDto;
import hu.cubix.employeemanager.dto.TimeOffDto;
import hu.cubix.employeemanager.exception.EmployeeNotAllowedException;
import hu.cubix.employeemanager.exception.EmployeeNotFoundException;
import hu.cubix.employeemanager.exception.InvalidParameterException;
import hu.cubix.employeemanager.exception.InvalidStatusException;
import hu.cubix.employeemanager.exception.TimeOffNotFoundException;
import hu.cubix.employeemanager.model.Employee;
import hu.cubix.employeemanager.model.TimeOff;
import hu.cubix.employeemanager.model.enums.Status;
import hu.cubix.employeemanager.repository.EmployeeRepository;
import hu.cubix.employeemanager.repository.TimeOffRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@AutoConfigureTestDatabase
public class TimeOffServiceTest {

    private final TimeOffService timeOffService;
    private final TimeOffRepository timeOffRepository;
    private final EmployeeRepository employeeRepository;

    @Autowired
    public TimeOffServiceTest(TimeOffService timeOffService,
                              TimeOffRepository timeOffRepository,
                              EmployeeRepository employeeRepository) {
        this.timeOffService = timeOffService;
        this.timeOffRepository = timeOffRepository;
        this.employeeRepository = employeeRepository;
    }

    @BeforeEach
    public void init() {
        timeOffRepository.deleteAllInBatch();
        employeeRepository.deleteAllInBatch();
    }

    @Test
    public void testCreateTimeOffWithNewEmployee() {
        TimeOffDto timeOffDto = createTimeOffDto("create timeoff");

        TimeOffDto savedTimeOffDto = timeOffService.createTimeOff(timeOffDto);

        TimeOff searchedTimeOff = timeOffRepository.findById(savedTimeOffDto.getId()).orElse(null);

        assertThat(searchedTimeOff).isNotNull();
        assertThat(searchedTimeOff.getCreateEmployee().getName()).isEqualTo(savedTimeOffDto.getCreateEmployeeName());
        assertThat(searchedTimeOff.getCreateDate()).isNotNull();
        assertThat(searchedTimeOff.getModifyEmployee()).isNull();
        assertThat(searchedTimeOff.getStartDate()).isEqualTo(savedTimeOffDto.getStartDate());
        assertThat(searchedTimeOff.getEndDate()).isEqualTo(savedTimeOffDto.getEndDate());
        assertThat(searchedTimeOff.getStatus().name()).isEqualTo(Status.PENDING.name());
    }

    @Test
    public void testCreateTimeOffInvalidParameter() {
        TimeOffDto timeOffDto = createTimeOffDto("create timeoff");
        timeOffDto.setStartDate(getLocalDateTime().plusHours(12));

        assertThatThrownBy(() -> {
            timeOffService.createTimeOff(timeOffDto);
        }).isInstanceOf(InvalidParameterException.class);
    }

    @Test
    public void testModifyTimeOff() {
        TimeOffDto timeOffDto = createTimeOffDto("modify timeoff");

        TimeOffDto savedTimeOffDto = timeOffService.createTimeOff(timeOffDto);

        LocalDateTime localDateTime = getLocalDateTime();
        timeOffDto.setStartDate(localDateTime.plusHours(2));
        timeOffDto.setEndDate(localDateTime.plusHours(3));
        timeOffService.modifyTimeOff(savedTimeOffDto.getId(), timeOffDto);

        TimeOff searchedTimeOff = timeOffRepository.findById(savedTimeOffDto.getId()).orElse(null);
        assertThat(searchedTimeOff).isNotNull();
        assertThat(searchedTimeOff.getCreateEmployee().getName()).isEqualTo(timeOffDto.getCreateEmployeeName());
        assertThat(searchedTimeOff.getCreateDate()).isNotNull();
        assertThat(searchedTimeOff.getModifyEmployee().getName()).isEqualTo(timeOffDto.getCreateEmployeeName());
        assertThat(searchedTimeOff.getModifyEmployee()).isNotNull();
        assertThat(searchedTimeOff.getStartDate()).isEqualTo(timeOffDto.getStartDate());
        assertThat(searchedTimeOff.getEndDate()).isEqualTo(timeOffDto.getEndDate());
        assertThat(searchedTimeOff.getStatus().name()).isEqualTo(Status.PENDING.name());
    }

    @Test
    public void testModifyTimeOffEmployeeNotFound() {
        TimeOffDto timeOffDto = createTimeOffDto("modify timeoff");

        TimeOffDto savedTimeOffDto = timeOffService.createTimeOff(timeOffDto);
        timeOffDto.setCreateEmployeeName("sserf sdfsdf");

        assertThatThrownBy(() -> {
            timeOffService.modifyTimeOff(savedTimeOffDto.getId(), timeOffDto);
        }).isInstanceOf(EmployeeNotFoundException.class);
    }

    @Test
    public void testModifyTimeOffTimeOffNotFound() {
        TimeOffDto timeOffDto = createTimeOffDto("modify timeoff");

        timeOffService.createTimeOff(timeOffDto);

        assertThatThrownBy(() -> {
            timeOffService.modifyTimeOff(0, timeOffDto);
        }).isInstanceOf(TimeOffNotFoundException.class);
    }

    @Test
    public void testCancelTimeOff() {
        TimeOffDto timeOffDto = createTimeOffDto("cancel timeoff");

        TimeOffDto savedTimeOffDto = timeOffService.createTimeOff(timeOffDto);

        EmployeeDto employeeDto = new EmployeeDto(timeOffDto.getCreateEmployeeName());
        timeOffService.cancelTimeOff(savedTimeOffDto.getId(), employeeDto);

        TimeOff searchedTimeOff = timeOffRepository.findById(savedTimeOffDto.getId()).orElse(null);
        assertThat(searchedTimeOff).isNotNull();
        assertThat(searchedTimeOff.getCreateEmployee().getName()).isEqualTo(employeeDto.getName());
        assertThat(searchedTimeOff.getCreateDate()).isNotNull();
        assertThat(searchedTimeOff.getModifyEmployee().getName()).isEqualTo(employeeDto.getName());
        assertThat(searchedTimeOff.getModifyEmployee()).isNotNull();
        assertThat(searchedTimeOff.getStartDate()).isEqualTo(timeOffDto.getStartDate());
        assertThat(searchedTimeOff.getEndDate()).isEqualTo(timeOffDto.getEndDate());
        assertThat(searchedTimeOff.getStatus().name()).isEqualTo(Status.CANCELED.name());
    }

    @Test
    public void testApproveOrDenyTimeOffApproved() {
        TimeOffDto timeOffDto = createTimeOffDto("aod timeoff");

        TimeOffDto savedTimeOffDto = timeOffService.createTimeOff(timeOffDto);

        Employee managerEmployee = new Employee("test manager");
        employeeRepository.save(managerEmployee);

        timeOffService.approveOrDenyTimeOff(savedTimeOffDto.getId(), new EmployeeDto(managerEmployee.getName()), true);

        TimeOff searchedTimeOff = timeOffRepository.findById(savedTimeOffDto.getId()).orElse(null);

        assertThat(searchedTimeOff).isNotNull();
        assertThat(searchedTimeOff.getCreateEmployee().getName()).isEqualTo(savedTimeOffDto.getCreateEmployeeName());
        assertThat(searchedTimeOff.getCreateDate()).isNotNull();
        assertThat(searchedTimeOff.getModifyEmployee().getName()).isEqualTo(managerEmployee.getName());
        assertThat(searchedTimeOff.getModifyEmployee()).isNotNull();
        assertThat(searchedTimeOff.getStartDate()).isEqualTo(timeOffDto.getStartDate());
        assertThat(searchedTimeOff.getEndDate()).isEqualTo(timeOffDto.getEndDate());
        assertThat(searchedTimeOff.getStatus().name()).isEqualTo(Status.APPROVED.name());
    }

    @Test
    public void testApproveOrDenyTimeOffDenied() {
        TimeOffDto timeOffDto = createTimeOffDto("aod timeoff");

        TimeOffDto savedTimeOffDto = timeOffService.createTimeOff(timeOffDto);

        Employee managerEmployee = new Employee("test manager");
        employeeRepository.save(managerEmployee);

        timeOffService.approveOrDenyTimeOff(savedTimeOffDto.getId(), new EmployeeDto(managerEmployee.getName()), false);

        TimeOff searchedTimeOff = timeOffRepository.findById(savedTimeOffDto.getId()).orElse(null);

        assertThat(searchedTimeOff).isNotNull();
        assertThat(searchedTimeOff.getCreateEmployee().getName()).isEqualTo(savedTimeOffDto.getCreateEmployeeName());
        assertThat(searchedTimeOff.getCreateDate()).isNotNull();
        assertThat(searchedTimeOff.getModifyEmployee().getName()).isEqualTo(managerEmployee.getName());
        assertThat(searchedTimeOff.getModifyEmployee()).isNotNull();
        assertThat(searchedTimeOff.getStartDate()).isEqualTo(timeOffDto.getStartDate());
        assertThat(searchedTimeOff.getEndDate()).isEqualTo(timeOffDto.getEndDate());
        assertThat(searchedTimeOff.getStatus().name()).isEqualTo(Status.DENIED.name());
    }

    @Test
    public void testApproveOrDenyTimeOffEmployeeNotAllowed() {
        TimeOffDto timeOffDto = createTimeOffDto("aod timeoff");

        TimeOffDto savedTimeOffDto = timeOffService.createTimeOff(timeOffDto);

        assertThatThrownBy(() -> {
            timeOffService.approveOrDenyTimeOff(savedTimeOffDto.getId(), new EmployeeDto(timeOffDto.getCreateEmployeeName()), true);
        }).isInstanceOf(EmployeeNotAllowedException.class);
    }

    @Test
    public void testApproveOrDenyTimeOffInvalidStatusException() {
        TimeOffDto timeOffDto = createTimeOffDto("aod timeoff");

        TimeOffDto savedTimeOffDto = timeOffService.createTimeOff(timeOffDto);
        Long id = savedTimeOffDto.getId();
        String createEmployeeName = timeOffDto.getCreateEmployeeName();
        timeOffService.cancelTimeOff(id, new EmployeeDto(createEmployeeName));

        Employee managerEmployee = new Employee("test manager");
        employeeRepository.save(managerEmployee);

        assertThatThrownBy(() -> {
            timeOffService.approveOrDenyTimeOff(id, new EmployeeDto(managerEmployee.getName()), true);
        }).isInstanceOf(InvalidStatusException.class);
    }

    private TimeOffDto createTimeOffDto(String employeeName) {
        LocalDateTime localDateTime = getLocalDateTime();
        return new TimeOffDto(employeeName, localDateTime.plusHours(1), localDateTime.plusHours(2));
    }

    private LocalDateTime getLocalDateTime() {
        return LocalDateTime.of(2023, 11, 16, 12, 0, 0);
    }
}