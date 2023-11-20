package hu.cubix.employeemanager.dto;

import java.util.List;

public class PageableTimeOffFilterDto {
    private List<TimeOffDto> timeOffDtos;
    private int totalPages;
    private long totalElements;

    public PageableTimeOffFilterDto() {
    }

    public PageableTimeOffFilterDto(List<TimeOffDto> timeOffDtos, int totalPages, long totalElements) {
        this.timeOffDtos = timeOffDtos;
        this.totalPages = totalPages;
        this.totalElements = totalElements;
    }

    public List<TimeOffDto> getTimeOffDtos() {
        return timeOffDtos;
    }

    public void setTimeOffDtos(List<TimeOffDto> timeOffDtos) {
        this.timeOffDtos = timeOffDtos;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }
}