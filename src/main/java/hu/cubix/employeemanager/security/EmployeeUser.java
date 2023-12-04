package hu.cubix.employeemanager.security;

import hu.cubix.employeemanager.model.Employee;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class EmployeeUser extends User {

    private Long id;
    private String name;
    private Map<Long, String> inferiors = new HashMap<>();
    private Long managerId;
    private String managerUserName;

    public EmployeeUser(String username, String password, Collection<? extends GrantedAuthority> authorities,Long id, String name,
                        Map<Long, String> inferiors, Long managerId, String managerUserName) {
        super(username, password, authorities);
        this.id = id;
        this.name = name;
        this.inferiors = inferiors;
        this.managerId = managerId;
        this.managerUserName = managerUserName;
    }

    public EmployeeUser(String username, String password, Collection<? extends GrantedAuthority> authorities, Employee employee) {
        super(username, password, authorities);
        this.id = employee.getId();
        this.name = employee.getName();

        Set<Employee> employeeInferiors = employee.getInferiors();
        if (employeeInferiors != null && !employeeInferiors.isEmpty()) {
            employeeInferiors.forEach(e -> inferiors.put(e.getId(), e.getUsername()));
        }

        Employee manager = employee.getManager();
        if (manager != null) {
            this.managerId = manager.getId();
            this.managerUserName = manager.getUsername();
        }
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Map<Long, String> getInferiors() {
        return inferiors;
    }

    public Long getManagerId() {
        return managerId;
    }

    public String getManagerUserName() {
        return managerUserName;
    }
}