package hu.cubix.employeemanager.exception;

public class EmployeeNotAllowedException extends RuntimeException {
    public EmployeeNotAllowedException(String message) {
        super(message);
    }
}