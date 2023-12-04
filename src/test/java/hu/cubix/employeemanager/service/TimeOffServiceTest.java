package hu.cubix.employeemanager.service;

import hu.cubix.employeemanager.dto.EmployeeDto;
import hu.cubix.employeemanager.dto.TimeOffDto;
import hu.cubix.employeemanager.exception.EmployeeNotAllowedException;
import hu.cubix.employeemanager.exception.EmployeeNotFoundException;
import hu.cubix.employeemanager.exception.InvalidParameterException;
import hu.cubix.employeemanager.exception.InvalidStatusException;
import hu.cubix.employeemanager.exception.TimeOffNotFoundException;
import hu.cubix.employeemanager.model.TimeOff;
import hu.cubix.employeemanager.model.enums.Status;
import hu.cubix.employeemanager.repository.EmployeeRepository;
import hu.cubix.employeemanager.repository.TimeOffRepository;
import hu.cubix.employeemanager.security.EmployeeUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@AutoConfigureTestDatabase
public class TimeOffServiceTest {

    private final EmployeeService employeeService;
    private final TimeOffService timeOffService;
    private final TimeOffRepository timeOffRepository;
    private final EmployeeRepository employeeRepository;

    @Autowired
    public TimeOffServiceTest(EmployeeService employeeService, TimeOffService timeOffService,
                              TimeOffRepository timeOffRepository,
                              EmployeeRepository employeeRepository) {
        this.employeeService = employeeService;
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
        String name = "create timeoff";

        EmployeeDto employee = employeeService.createEmployee(new EmployeeDto(name));
        setAuthentication(employee);

        TimeOffDto timeOffDto = createTimeOffDto();

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
        String name = "create timeoff";
        employeeService.createEmployee(new EmployeeDto(name));

        TimeOffDto timeOffDto = createTimeOffDto();
        timeOffDto.setStartDate(getLocalDateTime().plusHours(12));

        assertThatThrownBy(() -> timeOffService.createTimeOff(timeOffDto)).isInstanceOf(InvalidParameterException.class);
    }

    @Test
    public void testModifyTimeOff() {
        String name = "modify timeoff";
        EmployeeDto employee = employeeService.createEmployee(new EmployeeDto(name));
        setAuthentication(employee);

        TimeOffDto timeOffDto = createTimeOffDto();

        TimeOffDto savedTimeOffDto = timeOffService.createTimeOff(timeOffDto);

        LocalDateTime localDateTime = getLocalDateTime();
        timeOffDto.setStartDate(localDateTime.plusHours(2));
        timeOffDto.setEndDate(localDateTime.plusHours(3));
        timeOffService.modifyTimeOff(savedTimeOffDto.getId(), timeOffDto);

        TimeOff searchedTimeOff = timeOffRepository.findById(savedTimeOffDto.getId()).orElse(null);
        assertThat(searchedTimeOff).isNotNull();
        assertThat(searchedTimeOff.getCreateEmployee().getName()).isEqualTo(name);
        assertThat(searchedTimeOff.getCreateDate()).isNotNull();
        assertThat(searchedTimeOff.getModifyEmployee().getName()).isEqualTo(name);
        assertThat(searchedTimeOff.getModifyEmployee()).isNotNull();
        assertThat(searchedTimeOff.getStartDate()).isEqualTo(timeOffDto.getStartDate());
        assertThat(searchedTimeOff.getEndDate()).isEqualTo(timeOffDto.getEndDate());
        assertThat(searchedTimeOff.getStatus().name()).isEqualTo(Status.PENDING.name());
    }

    @Test
    public void testModifyTimeOffEmployeeNotFound() {
        String name = "modify timeoff";
        EmployeeDto employee = employeeService.createEmployee(new EmployeeDto(name));
        setAuthentication(employee);

        TimeOffDto timeOffDto = createTimeOffDto();

        TimeOffDto savedTimeOffDto = timeOffService.createTimeOff(timeOffDto);

        assertThatThrownBy(() -> {
            employee.setId(0L);
            setAuthentication(employee);
            timeOffService.modifyTimeOff(savedTimeOffDto.getId(), timeOffDto);
        }).isInstanceOf(EmployeeNotFoundException.class);
    }

    @Test
    public void testModifyTimeOffTimeOffNotFound_timeOff() {
        String name = "modify timeoff";
        EmployeeDto employee = employeeService.createEmployee(new EmployeeDto(name));
        setAuthentication(employee);

        TimeOffDto timeOffDto = createTimeOffDto();

        timeOffService.createTimeOff(timeOffDto);

        assertThatThrownBy(() -> timeOffService.modifyTimeOff(0, timeOffDto)).isInstanceOf(TimeOffNotFoundException.class);
    }

    @Test
    public void testModifyTimeOffTimeOffNotFound_employee() {
        EmployeeDto employee = employeeService.createEmployee(new EmployeeDto("modify timeoff"));
        setAuthentication(employee);

        TimeOffDto timeOffDto = createTimeOffDto();

        TimeOffDto timeOff = timeOffService.createTimeOff(timeOffDto);

        EmployeeDto employeeNotAllowed = employeeService.createEmployee(new EmployeeDto("sdf sdf"));
        setAuthentication(employeeNotAllowed);

        assertThatThrownBy(() -> timeOffService.modifyTimeOff(timeOff.getId(), timeOffDto)).isInstanceOf(TimeOffNotFoundException.class);
    }

    @Test
    public void testCancelTimeOff() {
        String name = "cancel timeoff";
        EmployeeDto employee = employeeService.createEmployee(new EmployeeDto(name));
        setAuthentication(employee);

        TimeOffDto timeOffDto = createTimeOffDto();

        TimeOffDto savedTimeOffDto = timeOffService.createTimeOff(timeOffDto);

        timeOffService.cancelTimeOff(savedTimeOffDto.getId());

        TimeOff searchedTimeOff = timeOffRepository.findById(savedTimeOffDto.getId()).orElse(null);
        assertThat(searchedTimeOff).isNotNull();
        assertThat(searchedTimeOff.getCreateEmployee().getName()).isEqualTo(name);
        assertThat(searchedTimeOff.getCreateDate()).isNotNull();
        assertThat(searchedTimeOff.getModifyEmployee().getName()).isEqualTo(name);
        assertThat(searchedTimeOff.getModifyEmployee()).isNotNull();
        assertThat(searchedTimeOff.getStartDate()).isEqualTo(timeOffDto.getStartDate());
        assertThat(searchedTimeOff.getEndDate()).isEqualTo(timeOffDto.getEndDate());
        assertThat(searchedTimeOff.getStatus().name()).isEqualTo(Status.CANCELED.name());
    }

    @Test
    public void testApproveOrDenyTimeOffApproved() {
        EmployeeDto manager = employeeService.createEmployee(new EmployeeDto("test manager"));

        String name = "aod timeoff";
        EmployeeDto employee = employeeService.createEmployee(new EmployeeDto(name), manager.getId());
        setAuthentication(employee);

        TimeOffDto timeOffDto = createTimeOffDto();

        TimeOffDto savedTimeOffDto = timeOffService.createTimeOff(timeOffDto);

        setAuthentication(manager);
        timeOffService.approveOrDenyTimeOff(savedTimeOffDto.getId(), true);

        TimeOff searchedTimeOff = timeOffRepository.findById(savedTimeOffDto.getId()).orElse(null);

        assertThat(searchedTimeOff).isNotNull();
        assertThat(searchedTimeOff.getCreateEmployee().getName()).isEqualTo(savedTimeOffDto.getCreateEmployeeName());
        assertThat(searchedTimeOff.getCreateDate()).isNotNull();
        assertThat(searchedTimeOff.getModifyEmployee().getName()).isEqualTo(manager.getName());
        assertThat(searchedTimeOff.getModifyEmployee()).isNotNull();
        assertThat(searchedTimeOff.getStartDate()).isEqualTo(timeOffDto.getStartDate());
        assertThat(searchedTimeOff.getEndDate()).isEqualTo(timeOffDto.getEndDate());
        assertThat(searchedTimeOff.getStatus().name()).isEqualTo(Status.APPROVED.name());
    }

    @Test
    public void testApproveOrDenyTimeOffDenied() {
        EmployeeDto manager = employeeService.createEmployee(new EmployeeDto("test manager"));

        String name = "aod timeoff";
        EmployeeDto employee = employeeService.createEmployee(new EmployeeDto(name), manager.getId());
        setAuthentication(employee);

        TimeOffDto timeOffDto = createTimeOffDto();

        TimeOffDto savedTimeOffDto = timeOffService.createTimeOff(timeOffDto);

        setAuthentication(manager);
        timeOffService.approveOrDenyTimeOff(savedTimeOffDto.getId(), false);

        TimeOff searchedTimeOff = timeOffRepository.findById(savedTimeOffDto.getId()).orElse(null);

        assertThat(searchedTimeOff).isNotNull();
        assertThat(searchedTimeOff.getCreateEmployee().getName()).isEqualTo(savedTimeOffDto.getCreateEmployeeName());
        assertThat(searchedTimeOff.getCreateDate()).isNotNull();
        assertThat(searchedTimeOff.getModifyEmployee().getName()).isEqualTo(manager.getName());
        assertThat(searchedTimeOff.getModifyEmployee()).isNotNull();
        assertThat(searchedTimeOff.getStartDate()).isEqualTo(timeOffDto.getStartDate());
        assertThat(searchedTimeOff.getEndDate()).isEqualTo(timeOffDto.getEndDate());
        assertThat(searchedTimeOff.getStatus().name()).isEqualTo(Status.DENIED.name());
    }

    @Test
    public void testApproveOrDenyTimeOffEmployeeNotAllowed() {
        EmployeeDto manager = employeeService.createEmployee(new EmployeeDto("test manager"));

        String name = "aod timeoff";
        EmployeeDto employee = employeeService.createEmployee(new EmployeeDto(name), manager.getId());
        setAuthentication(employee);

        TimeOffDto timeOffDto = createTimeOffDto();

        TimeOffDto savedTimeOffDto = timeOffService.createTimeOff(timeOffDto);

        assertThatThrownBy(() -> timeOffService.approveOrDenyTimeOff(savedTimeOffDto.getId(), true)).isInstanceOf(EmployeeNotAllowedException.class);
    }

    @Test
    public void testApproveOrDenyTimeOffInvalidStatusException() {
        EmployeeDto manager = employeeService.createEmployee(new EmployeeDto("test manager"));

        String name = "aod timeoff";
        EmployeeDto employee = employeeService.createEmployee(new EmployeeDto(name), manager.getId());

        TimeOffDto timeOffDto = createTimeOffDto();

        setAuthentication(employee);
        TimeOffDto savedTimeOffDto = timeOffService.createTimeOff(timeOffDto);
        Long id = savedTimeOffDto.getId();
        timeOffService.cancelTimeOff(id);

        setAuthentication(manager);

        assertThatThrownBy(() -> timeOffService.approveOrDenyTimeOff(id, true)).isInstanceOf(InvalidStatusException.class);
    }

    private TimeOffDto createTimeOffDto() {
        LocalDateTime localDateTime = getLocalDateTime();
        return new TimeOffDto(null, localDateTime.plusHours(1), localDateTime.plusHours(2));
    }

    private LocalDateTime getLocalDateTime() {
        return LocalDateTime.of(2023, 11, 16, 12, 0, 0);
    }

    private static void setAuthentication(EmployeeDto employee) {
        EmployeeUser employeeUser = new EmployeeUser("user", "password", Collections.emptyList(), employee.getId(), null, null, null, null);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(employeeUser, null, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}