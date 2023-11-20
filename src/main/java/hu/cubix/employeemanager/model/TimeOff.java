package hu.cubix.employeemanager.model;

import hu.cubix.employeemanager.model.enums.Status;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@EntityListeners(AuditingEntityListener.class)
public class TimeOff {
    @Id
    @GeneratedValue
    private long id;
    @CreatedDate
    private LocalDateTime createDate;
    @ManyToOne
    private Employee createEmployee;
    @LastModifiedDate
    private LocalDateTime modifyDate;
    @ManyToOne
    private Employee modifyEmployee;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    @Enumerated(EnumType.STRING)
    private Status status;

    public TimeOff() {
    }

    public TimeOff(Employee createEmployee, Employee modifyEmployee, LocalDateTime startDate, LocalDateTime endDate, Status status) {
        this.createEmployee = createEmployee;
        this.modifyEmployee = modifyEmployee;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public LocalDateTime getCreateDate() {
        return createDate;
    }

    public void setCreateDate(LocalDateTime createDate) {
        this.createDate = createDate;
    }

    public Employee getCreateEmployee() {
        return createEmployee;
    }

    public void setCreateEmployee(Employee createEmployee) {
        this.createEmployee = createEmployee;
    }

    public LocalDateTime getModifyDate() {
        return modifyDate;
    }

    public void setModifyDate(LocalDateTime modifyDate) {
        this.modifyDate = modifyDate;
    }

    public Employee getModifyEmployee() {
        return modifyEmployee;
    }

    public void setModifyEmployee(Employee modifyEmployee) {
        this.modifyEmployee = modifyEmployee;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimeOff timeOffs = (TimeOff) o;
        return id == timeOffs.id
                && Objects.equals(createDate, timeOffs.createDate)
                && Objects.equals(createEmployee, timeOffs.createEmployee)
                && Objects.equals(modifyDate, timeOffs.modifyDate)
                && Objects.equals(modifyEmployee, timeOffs.modifyEmployee)
                && Objects.equals(startDate, timeOffs.startDate)
                && Objects.equals(endDate, timeOffs.endDate)
                && status == timeOffs.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, createDate, createEmployee, modifyDate, modifyEmployee, startDate, endDate, status);
    }
}