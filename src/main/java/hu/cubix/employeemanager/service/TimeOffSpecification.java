package hu.cubix.employeemanager.service;

import hu.cubix.employeemanager.model.Employee_;
import hu.cubix.employeemanager.model.TimeOff;
import hu.cubix.employeemanager.model.TimeOff_;
import hu.cubix.employeemanager.model.enums.Status;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class TimeOffSpecification {

    public static Specification<TimeOff> hasStatus(Status status) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get(TimeOff_.status), status);
    }

    public static Specification<TimeOff> hasCreator(String prefix) {
        return (root, query, criteriaBuilder)
                -> criteriaBuilder.like(criteriaBuilder.lower(root.get(TimeOff_.createEmployee).get(Employee_.name)), prefix.toLowerCase() + "%");
    }

    public static Specification<TimeOff> hasModifier(String prefix) {
        return (root, query, criteriaBuilder)
                -> criteriaBuilder.like(criteriaBuilder.lower(root.get(TimeOff_.modifyEmployee).get(Employee_.name)), prefix.toLowerCase() + "%");
    }

    public static Specification<TimeOff> hasCreateDateBetween(LocalDateTime from, LocalDateTime to) {
        return (root, query, criteriaBuilder)
                -> criteriaBuilder.between(root.get(TimeOff_.createDate), from, to);
    }

    public static Specification<TimeOff> hasDatesIntersect(LocalDateTime startDate, LocalDateTime endDate) {
        return (root, query, criteriaBuilder)
                -> criteriaBuilder.or(
                criteriaBuilder.lessThanOrEqualTo(root.get(TimeOff_.startDate), endDate),
                criteriaBuilder.greaterThanOrEqualTo(root.get(TimeOff_.endDate), startDate)
        );
    }
}