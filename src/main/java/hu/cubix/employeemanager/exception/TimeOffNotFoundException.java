package hu.cubix.employeemanager.exception;

public class TimeOffNotFoundException extends RuntimeException {
    public TimeOffNotFoundException(String message) {
        super(message);
    }
}