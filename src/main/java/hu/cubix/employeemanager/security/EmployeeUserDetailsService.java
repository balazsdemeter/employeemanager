package hu.cubix.employeemanager.security;

import hu.cubix.employeemanager.model.Employee;
import hu.cubix.employeemanager.repository.EmployeeRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class EmployeeUserDetailsService implements UserDetailsService {

    private final EmployeeRepository employeeRepository;

    public EmployeeUserDetailsService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Employee employee = employeeRepository.findEmployeeByUsername(username).orElseThrow(() -> new UsernameNotFoundException(username));
        return new EmployeeUser(username, employee.getPassword(), employee.getRoles().stream().map(SimpleGrantedAuthority::new).toList(), employee);
    }
}