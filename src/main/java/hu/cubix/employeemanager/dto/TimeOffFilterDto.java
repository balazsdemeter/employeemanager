package hu.cubix.employeemanager.dto;

import java.time.LocalDateTime;

public record TimeOffFilterDto(
        String status,
        String creator,
        String modifier,
        LocalDateTime createDateFrom,
        LocalDateTime createDateTo,
        LocalDateTime startDate,
        LocalDateTime endDate,
        int pageNumber,
        int pageSize) {

    public TimeOffFilterDto(String status, String creator, String modifier, LocalDateTime createDateFrom, LocalDateTime createDateTo, LocalDateTime startDate, LocalDateTime endDate, int pageNumber, int pageSize) {
        this.status = status;
        this.creator = creator;
        this.modifier = modifier;
        this.createDateFrom = createDateFrom;
        this.createDateTo = createDateTo;
        this.startDate = startDate;
        this.endDate = endDate;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
    }
}