package hu.cubix.employeemanager.dto;

import jakarta.validation.constraints.NotBlank;

public class EmployeeDto {
    private Long id;
    @NotBlank
    private String name;

    public EmployeeDto() {
    }

    public EmployeeDto(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}