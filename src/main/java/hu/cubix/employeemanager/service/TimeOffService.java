package hu.cubix.employeemanager.service;

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
import hu.cubix.employeemanager.security.EmployeeUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
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

        Employee employee = findEmployee(getEmployeeUser().getId());
        TimeOff timeOff = new TimeOff(employee, null, timeOffDto.getStartDate(), timeOffDto.getEndDate(), Status.PENDING);
        timeOffRepository.save(timeOff);
        return timeOffMapper.entityToDto(timeOff);
    }

    @Transactional
    public TimeOffDto modifyTimeOff(long timeOffId, TimeOffDto timeOffDto) {
        validateDates(timeOffDto.getStartDate(), timeOffDto.getEndDate());

        Employee employee = findEmployee(getEmployeeUser().getId());
        TimeOff timeOff = findTimeOff(timeOffId, employee);

        if (isPendig(timeOff)) {
            timeOff.setModifyEmployee(employee);
            timeOff.setStartDate(timeOffDto.getStartDate());
            timeOff.setEndDate(timeOffDto.getEndDate());
            TimeOff savedTimeOff = timeOffRepository.save(timeOff);
            return timeOffMapper.entityToDto(savedTimeOff);
        } else {
            throw new InvalidStatusException(String.format("%s status is invalid.", timeOff.getStatus().name()));
        }
    }

    @Transactional
    public TimeOffDto cancelTimeOff(long timeOffId) {
        Employee employee = findEmployee(getEmployeeUser().getId());
        TimeOff timeOff = findTimeOff(timeOffId, employee);

        if (isPendig(timeOff)) {
            timeOff.setStatus(Status.CANCELED);
            timeOff.setModifyEmployee(employee);
            TimeOff savedTimeOff = timeOffRepository.save(timeOff);
            return timeOffMapper.entityToDto(savedTimeOff);
        } else {
            throw new InvalidStatusException(String.format("%s status is invalid.", timeOff.getStatus().name()));
        }
    }

    @Transactional
    public TimeOffDto approveOrDenyTimeOff(long timeOffId, Boolean approveOrDeny) {
        EmployeeUser employeeUser = getEmployeeUser();
        TimeOff timeOff = findTimeOff(timeOffId);

        if (timeOff.getCreateEmployee().getManager().getId() != employeeUser.getId()) {
            throw new EmployeeNotAllowedException(String.format("%s not allowed to use this function.", employeeUser.getName()));
        }

        if (isPendig(timeOff)) {
            timeOff.setStatus(approveOrDeny ? Status.APPROVED : Status.DENIED);
            timeOff.setModifyEmployee(findEmployee(employeeUser.getId()));
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

    private Employee findEmployee(Long id) {
        Employee employee = employeeService.findById(id);
        if (employee == null) {
            throw new EmployeeNotFoundException("Employee not found.");
        }
        return employee;
    }

    private TimeOff findTimeOff(Long id) {
        return timeOffRepository.findById(id)
                .orElseThrow(() -> new TimeOffNotFoundException("TimeOff not found."));
    }

    private TimeOff findTimeOff(Long id, Employee employee) {
        return timeOffRepository.findByIdAndCreateEmployee(id, employee)
                .orElseThrow(() -> new TimeOffNotFoundException("TimeOff not found."));
    }

    public Page<TimeOff> findByFiltering(TimeOffFilterDto timeOffFilterDto) {
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

        return timeOffRepository.findAll(specification, PageRequest.of(timeOffFilterDto.pageNumber(), timeOffFilterDto.pageSize()));
    }

    private static boolean isPendig(TimeOff timeOff) {
        return Status.PENDING == timeOff.getStatus();
    }

    private EmployeeUser getEmployeeUser() {
        return ((EmployeeUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }
}