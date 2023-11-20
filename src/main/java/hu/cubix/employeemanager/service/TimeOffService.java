package hu.cubix.employeemanager.service;

import hu.cubix.employeemanager.dto.EmployeeDto;
import hu.cubix.employeemanager.dto.PageableTimeOffFilterDto;
import hu.cubix.employeemanager.dto.TimeOffDto;
import hu.cubix.employeemanager.dto.TimeOffFilterDto;
import hu.cubix.employeemanager.exception.EmployeeNotAllowedException;
import hu.cubix.employeemanager.exception.EmployeeNotFoundException;
import hu.cubix.employeemanager.exception.InvalidParameterException;
import hu.cubix.employeemanager.exception.InvalidStatusException;
import hu.cubix.employeemanager.exception.TimeOffNotFoundException;
import hu.cubix.employeemanager.mapper.TimeOffMapper;
import hu.cubix.employeemanager.model.Employee;
import hu.cubix.employeemanager.model.TimeOff;
import hu.cubix.employeemanager.model.enums.Status;
import hu.cubix.employeemanager.repository.TimeOffRepository;
import org.antlr.v4.runtime.misc.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TimeOffService {
    private final TimeOffRepository timeOffRepository;
    private final TimeOffMapper timeOffMapper;
    private final EmployeeService employeeService;

    @Autowired
    public TimeOffService(TimeOffRepository timeOffRepository, TimeOffMapper timeOffMapper, EmployeeService employeeService) {
        this.timeOffRepository = timeOffRepository;
        this.timeOffMapper = timeOffMapper;
        this.employeeService = employeeService;
    }

    @Transactional
    public List<TimeOffDto> findAll() {
        List<TimeOff> timeOffs = timeOffRepository.findAll();
        return timeOffMapper.entitiesToDtos(timeOffs);
    }

    @Transactional
    public TimeOffDto createTimeOff(TimeOffDto timeOffDto) {
        validateDates(timeOffDto.getStartDate(),timeOffDto.getEndDate());

        Employee employee = employeeService.findOrCreateEmployeeByName(timeOffDto.getCreateEmployeeName());
        TimeOff timeOff = new TimeOff(employee, null, timeOffDto.getStartDate(), timeOffDto.getEndDate(), Status.PENDING);
        timeOffRepository.save(timeOff);
        return timeOffMapper.entityToDto(timeOff);
    }

    @Transactional
    public TimeOffDto modifyTimeOff(long timeOffId, TimeOffDto timeOffDto) {
        validateDates(timeOffDto.getStartDate(), timeOffDto.getEndDate());

        Pair<Employee, TimeOff> pair = findTimeOffByIdAndEmployeeName(timeOffDto.getCreateEmployeeName(), timeOffId);

        TimeOff timeOff = pair.b;
        if (isPendig(timeOff)) {
            timeOff.setModifyEmployee(pair.a);
            timeOff.setStartDate(timeOffDto.getStartDate());
            timeOff.setEndDate(timeOffDto.getEndDate());
            TimeOff savedTimeOff = timeOffRepository.save(timeOff);
            return timeOffMapper.entityToDto(savedTimeOff);
        } else {
            throw new InvalidStatusException(String.format("%s status is invalid.", timeOff.getStatus().name()));
        }
    }

    @Transactional
    public TimeOffDto cancelTimeOff(long timeOffId, EmployeeDto employeeDto) {
        Pair<Employee, TimeOff> pair = findTimeOffByIdAndEmployeeName(employeeDto.getName(), timeOffId);

        TimeOff timeOff = pair.b;
        if (isPendig(timeOff)) {
            timeOff.setStatus(Status.CANCELED);
            timeOff.setModifyEmployee(pair.a);
            TimeOff savedTimeOff = timeOffRepository.save(timeOff);
            return timeOffMapper.entityToDto(savedTimeOff);
        } else {
            throw new InvalidStatusException(String.format("%s status is invalid.", timeOff.getStatus().name()));
        }
    }

    @Transactional
    public TimeOffDto approveOrDenyTimeOff(long timeOffId, EmployeeDto employeeDto, Boolean approveOrDeny) {
        Pair<Employee, TimeOff> pair = findEmployeeAndTimeOff(employeeDto.getName(), timeOffId);

        Employee employee = pair.a;
        TimeOff timeOff = pair.b;
        if (timeOff.getCreateEmployee().getId() == employee.getId()) {
            throw new EmployeeNotAllowedException(String.format("%s not allowed to use this function.", employee.getName()));
        }

        if (isPendig(timeOff)) {
            timeOff.setStatus(approveOrDeny ? Status.APPROVED : Status.DENIED);
            timeOff.setModifyEmployee(employee);
            TimeOff savedTimeOff = timeOffRepository.save(timeOff);
            return timeOffMapper.entityToDto(savedTimeOff);
        } else {
            throw new InvalidStatusException(String.format("%s status is invalid.", timeOff.getStatus().name()));
        }
    }

    private void validateDates(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate.isAfter(endDate)) {
            throw new InvalidParameterException("Start date must be before end date");
        }
    }

    private Pair<Employee, TimeOff> findEmployeeAndTimeOff(String name, long timeOffId) {
        Employee employee = employeeService.findEmployeeByName(name);
        if (employee == null) {
            throw new EmployeeNotFoundException("Employee not found.");
        }

        TimeOff timeOff = timeOffRepository.findById(timeOffId).orElse(null);
        if (timeOff == null) {
            throw new TimeOffNotFoundException("TimeOff not found.");
        }

        return new Pair<>(employee, timeOff);
    }

    private Pair<Employee, TimeOff> findTimeOffByIdAndEmployeeName(String name, long timeOffId) {
        Employee employee = employeeService.findEmployeeByName(name);
        if (employee == null) {
            throw new EmployeeNotFoundException("Employee not found.");
        }

        TimeOff timeOff = timeOffRepository.findByIdAndCreateEmployee(timeOffId, employee).orElse(null);
        if (timeOff == null) {
            throw new TimeOffNotFoundException("TimeOff not found.");
        }

        return new Pair<>(employee, timeOff);
    }

    public PageableTimeOffFilterDto findByFiltering(TimeOffFilterDto timeOffFilterDto) {
        Specification<TimeOff> specification = Specification.where(null);

        String status = timeOffFilterDto.status();
        if (StringUtils.hasLength(status)) {
            try {
                Status statusEnum = Status.valueOf(status);
                specification = specification.and(TimeOffSpecification.hasStatus(statusEnum));
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }

        String creator = timeOffFilterDto.creator();
        if (StringUtils.hasLength(creator)) {
            specification = specification.and(TimeOffSpecification.hasCreator(creator));
        }

        String modifier = timeOffFilterDto.modifier();
        if (StringUtils.hasLength(modifier)) {
            specification = specification.and(TimeOffSpecification.hasModifier(modifier));
        }

        LocalDateTime createDateFrom = timeOffFilterDto.createDateFrom();
        LocalDateTime createDateTo = timeOffFilterDto.createDateTo();
        if (createDateFrom != null && createDateTo != null) {
            specification = specification.and(TimeOffSpecification.hasCreateDateBetween(createDateFrom, createDateTo));
        }

        LocalDateTime startDate = timeOffFilterDto.startDate();
        LocalDateTime endDate = timeOffFilterDto.endDate();
        if (startDate != null && endDate != null) {
            specification = specification.and(TimeOffSpecification.hasDatesIntersect(startDate, endDate));
        }

        Page<TimeOff> page = timeOffRepository.findAll(specification, PageRequest.of(timeOffFilterDto.pageNumber(), timeOffFilterDto.pageSize()));
        return new PageableTimeOffFilterDto(timeOffMapper.entitiesToDtos(page.getContent()), page.getTotalPages(), page.getTotalElements());
    }

    private static boolean isPendig(TimeOff timeOff) {
        return Status.PENDING == timeOff.getStatus();
    }
}