package hu.cubix.employeemanager;

import hu.cubix.employeemanager.service.InitDbService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class EmployeeManagerApplication implements CommandLineRunner {

    private final InitDbService initDbService;

    @Autowired
    public EmployeeManagerApplication(InitDbService initDbService) {
        this.initDbService = initDbService;
    }

    public static void main(String[] args) {
        SpringApplication.run(EmployeeManagerApplication.class, args);
    }

    @Override
    public void run(String... args) {
        initDbService.createEmployeeIfNeeded();
    }
}