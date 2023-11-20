package hu.cubix.employeemanager.controller;

import hu.cubix.employeemanager.dto.EmployeeDto;
import hu.cubix.employeemanager.dto.PageableTimeOffFilterDto;
import hu.cubix.employeemanager.dto.TimeOffDto;
import hu.cubix.employeemanager.dto.TimeOffFilterDto;
import hu.cubix.employeemanager.exception.EmployeeNotAllowedException;
import hu.cubix.employeemanager.exception.EmployeeNotFoundException;
import hu.cubix.employeemanager.exception.InvalidParameterException;
import hu.cubix.employeemanager.exception.InvalidStatusException;
import hu.cubix.employeemanager.exception.TimeOffNotFoundException;
import hu.cubix.employeemanager.service.TimeOffService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/timeOff")
public class EmployeeManagerController {
    private final TimeOffService timeOffService;

    @Autowired
    public EmployeeManagerController(TimeOffService timeOffService) {
        this.timeOffService = timeOffService;
    }

    @GetMapping
    public ResponseEntity<List<TimeOffDto>> getAll() {
        List<TimeOffDto> timeOffDtos = timeOffService.findAll();
        return ResponseEntity.ok(timeOffDtos);
    }

    @GetMapping("/findByFiltering")
    public ResponseEntity<PageableTimeOffFilterDto> findByFiltering(@RequestParam(name = "status", required = false) String status,
                                                                    @RequestParam(name = "creator", required = false) String creator,
                                                                    @RequestParam(name = "modifier", required = false) String modifier,
                                                                    @RequestParam(name = "createDateFrom", required = false) LocalDateTime createDateFrom,
                                                                    @RequestParam(name = "createDateTo", required = false) LocalDateTime createDateTo,
                                                                    @RequestParam(name = "startDate", required = false) LocalDateTime startDate,
                                                                    @RequestParam(name = "endDate", required = false) LocalDateTime endDate,
                                                                    @RequestParam(name = "pageNumber") int pageNumber,
                                                                    @RequestParam(name = "pageSize") int pageSize) {
        TimeOffFilterDto timeOffFilterDto =
                new TimeOffFilterDto(
                        status,
                        creator,
                        modifier,
                        createDateFrom,
                        createDateTo,
                        startDate,
                        endDate,
                        pageNumber,
                        pageSize
                );
        PageableTimeOffFilterDto pageableTimeOffFilterDto = timeOffService.findByFiltering(timeOffFilterDto);
        return ResponseEntity.ok(pageableTimeOffFilterDto);
    }

    @PostMapping
    public ResponseEntity<TimeOffDto> createTimeOff(@Valid @RequestBody TimeOffDto timeOffDto) {
        TimeOffDto savedTimeOffDto;
        try {
            savedTimeOffDto = timeOffService.createTimeOff(timeOffDto);
            return ResponseEntity.ok(savedTimeOffDto);
        } catch (InvalidParameterException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("{id}/modify")
    public ResponseEntity<TimeOffDto> modifyTimeOff(@PathVariable long id, @Valid @RequestBody TimeOffDto timeOffDto) {
        try {
            TimeOffDto modifiedTimeOffDto = timeOffService.modifyTimeOff(id, timeOffDto);
            return ResponseEntity.ok(modifiedTimeOffDto);
        } catch (InvalidParameterException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        } catch (EmployeeNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        } catch (TimeOffNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        } catch (EmployeeNotAllowedException | InvalidStatusException e) {
            throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED);
        }
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<TimeOffDto> cancelTimeOff(@PathVariable long id, @Valid @RequestBody EmployeeDto employeeDto) {
        try {
            TimeOffDto modifiedTimeOffDto = timeOffService.cancelTimeOff(id, employeeDto);
            return ResponseEntity.ok(modifiedTimeOffDto);
        } catch (EmployeeNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        } catch (TimeOffNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        } catch (EmployeeNotAllowedException | InvalidStatusException e) {
            throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED);
        }
    }

    @PutMapping("/{id}/approveOrDeny")
    public ResponseEntity<TimeOffDto> approveOrDenyTimeOff(@PathVariable long id, @Valid @RequestBody EmployeeDto employeeDto,
                                                           @RequestParam(name = "approveOrDeny") Boolean approveOrDeny) {
        try {
            TimeOffDto timeOffDto = timeOffService.approveOrDenyTimeOff(id, employeeDto, approveOrDeny);
            return ResponseEntity.ok(timeOffDto);
        } catch (EmployeeNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        } catch (TimeOffNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        } catch (EmployeeNotAllowedException | InvalidStatusException e) {
            throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED);
        }
    }
}