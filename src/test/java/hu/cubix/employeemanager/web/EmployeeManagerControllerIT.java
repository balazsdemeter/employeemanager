package hu.cubix.employeemanager.web;

import hu.cubix.employeemanager.dto.EmployeeDto;
import hu.cubix.employeemanager.dto.LoginDto;
import hu.cubix.employeemanager.dto.TimeOffDto;
import hu.cubix.employeemanager.model.enums.Status;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient.ResponseSpec;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase
public class EmployeeManagerControllerIT {
    private static final String BASE_URI = "/api/timeOff";
    private final WebTestClient webTestClient;
    private String employeeToken;
    private String managerToken;

    @Autowired
    public EmployeeManagerControllerIT(WebTestClient webTestClient) {
        this.webTestClient = webTestClient;
    }

    @Test
    public void test_create_timeoff_ok() {
        employeeToken = login("user1");

        List<TimeOffDto> timeOffsBefore = getAllTimeOffs();

        LocalDateTime localDateTime = getLocalDateTime();
        TimeOffDto timeOffDto = new TimeOffDto("employee1", localDateTime.plusHours(1), localDateTime.plusHours(2));
        saveTimeOff(timeOffDto);

        List<TimeOffDto> timeOffsAfter = getAllTimeOffs();

        assertThat(timeOffsAfter.size()).isEqualTo(timeOffsBefore.size() + 1);
        assertThat(timeOffsAfter.get(timeOffsAfter.size() - 1))
                .usingRecursiveComparison()
                .ignoringFields("id", "createDate", "modifyDate", "status")
                .isEqualTo(timeOffDto);
    }

    @Test
    public void test_modify_timeoff_ok() {
        employeeToken = login("user1");

        LocalDateTime localDateTime = getLocalDateTime();
        TimeOffDto timeOffDto = new TimeOffDto("employee1", localDateTime.plusHours(1), localDateTime.plusHours(2));
        TimeOffDto savedTimeOffDto = saveTimeOff(timeOffDto)
                .expectStatus()
                .isOk()
                .expectBody(TimeOffDto.class)
                .returnResult()
                .getResponseBody();

        List<TimeOffDto> timeOffsBefore = getAllTimeOffs();
        savedTimeOffDto.setEndDate(localDateTime.plusHours(6));
        modifyTimeOff(savedTimeOffDto.getId(), savedTimeOffDto).expectStatus().isOk();
        savedTimeOffDto.setModifyEmployeeName(savedTimeOffDto.getCreateEmployeeName());

        List<TimeOffDto> timeOffsAfter = getAllTimeOffs();

        assertThat(timeOffsAfter).hasSameSizeAs(timeOffsBefore);
        assertThat(timeOffsAfter.get(timeOffsAfter.size() - 1))
                .usingRecursiveComparison()
                .ignoringFields("id", "createDate", "modifyDate", "status")
                .isEqualTo(savedTimeOffDto);
    }

    @Test
    public void test_modify_timeoff_unauthorized() {
        employeeToken = login("user1");
        LocalDateTime localDateTime = getLocalDateTime();
        TimeOffDto timeOffDto = new TimeOffDto("employee1", localDateTime.plusHours(1), localDateTime.plusHours(2));
        TimeOffDto savedTimeOffDto = saveTimeOff(timeOffDto)
                .expectStatus()
                .isOk()
                .expectBody(TimeOffDto.class)
                .returnResult()
                .getResponseBody();

        employeeToken = login("user2");
        modifyTimeOff(savedTimeOffDto.getId(), new TimeOffDto("employee2", localDateTime.plusHours(1), localDateTime.plusHours(2)))
                .expectStatus()
                .isForbidden();
    }

    @Test
    public void test_cancel_timeoff_ok() {
        employeeToken = login("user1");

        LocalDateTime localDateTime = getLocalDateTime();
        String employeeName = "employee1";
        TimeOffDto timeOffDto = new TimeOffDto(employeeName, localDateTime.plusHours(1), localDateTime.plusHours(2));
        TimeOffDto savedTimeOffDto = saveTimeOff(timeOffDto)
                .expectStatus()
                .isOk()
                .expectBody(TimeOffDto.class)
                .returnResult()
                .getResponseBody();

        List<TimeOffDto> timeOffsBefore = getAllTimeOffs();
        cancelTimeOff(savedTimeOffDto.getId(), new EmployeeDto(employeeName)).expectStatus().isOk();
        savedTimeOffDto.setModifyEmployeeName(employeeName);

        List<TimeOffDto> timeOffsAfter = getAllTimeOffs();

        assertThat(timeOffsAfter).hasSameSizeAs(timeOffsBefore);
        TimeOffDto actual = timeOffsAfter.get(timeOffsAfter.size() - 1);
        assertThat(actual)
                .usingRecursiveComparison()
                .ignoringFields("id", "createDate", "modifyDate", "status")
                .isEqualTo(savedTimeOffDto);
        assertThat(actual.getStatus()).isEqualTo(Status.CANCELED.name());
    }

    @Test
    public void test_approveOrDenyTimeOff_ok() {
        employeeToken = login("user1");

        LocalDateTime localDateTime = getLocalDateTime();
        String employeeName = "employee1";
        TimeOffDto timeOffDto = new TimeOffDto(employeeName, localDateTime.plusHours(1), localDateTime.plusHours(2));
        TimeOffDto savedTimeOffDto = saveTimeOff(timeOffDto)
                .expectStatus()
                .isOk()
                .expectBody(TimeOffDto.class)
                .returnResult()
                .getResponseBody();

        List<TimeOffDto> timeOffsBefore = getAllTimeOffs();

        managerToken = login("admin");
        approveOrDeny(savedTimeOffDto.getId(), new EmployeeDto("manager"))
                .expectStatus()
                .isOk();
        List<TimeOffDto> timeOffsAfter = getAllTimeOffs();

        assertThat(timeOffsAfter).hasSameSizeAs(timeOffsBefore);
        TimeOffDto actual = timeOffsAfter.get(timeOffsAfter.size() - 1);
        assertThat(actual)
                .usingRecursiveComparison()
                .ignoringFields("id", "createDate", "modifyDate", "status", "modifyEmployeeName")
                .isEqualTo(savedTimeOffDto);
        assertThat(actual.getModifyEmployeeName()).isEqualTo("manager");
        assertThat(actual.getStatus()).isEqualTo(Status.APPROVED.name());
    }

    @Test
    public void test_approveOrDenyTimeOff_nok() {
        employeeToken = login("user1");

        LocalDateTime localDateTime = getLocalDateTime();
        TimeOffDto timeOffDto = new TimeOffDto("employee1", localDateTime.plusHours(1), localDateTime.plusHours(2));
        TimeOffDto savedTimeOffDto = saveTimeOff(timeOffDto)
                .expectStatus()
                .isOk()
                .expectBody(TimeOffDto.class)
                .returnResult()
                .getResponseBody();

        employeeToken = login("user2");
        approveOrDeny(savedTimeOffDto.getId(), new EmployeeDto("employee2"))
                .expectStatus()
                .reasonEquals(HttpStatus.FORBIDDEN.getReasonPhrase());
    }

    private ResponseSpec approveOrDeny(long id, EmployeeDto employeeDto) {
        String path = BASE_URI + "/" + id + "/approveOrDeny?approveOrDeny=true";
        return webTestClient
                .put()
                .uri(path)
                .headers(http -> http.setBearerAuth(managerToken))
                .bodyValue(employeeDto)
                .exchange();
    }


    private ResponseSpec cancelTimeOff(long id, EmployeeDto employeeDto) {
        String path = BASE_URI + "/" + id + "/cancel";
        return webTestClient
                .put()
                .uri(path)
                .headers(http -> http.setBearerAuth(employeeToken))
                .bodyValue(employeeDto)
                .exchange();
    }

    private ResponseSpec modifyTimeOff(long id, TimeOffDto timeOffDto) {
        String path = BASE_URI + "/" + id + "/modify";
        return webTestClient
                .put()
                .uri(path)
                .headers(http -> http.setBearerAuth(employeeToken))
                .bodyValue(timeOffDto)
                .exchange();
    }

    private ResponseSpec saveTimeOff(TimeOffDto timeOffDto) {
        return webTestClient
                .post()
                .uri(BASE_URI)
                .headers(http -> http.setBearerAuth(employeeToken))
                .bodyValue(timeOffDto)
                .exchange();
    }

    private String login(String username) {
        return webTestClient
                .post()
                .uri("/api/login")
                .bodyValue(new LoginDto(username, "pass"))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();
    }

    private List<TimeOffDto> getAllTimeOffs() {
        List<TimeOffDto> timeOffDtos = webTestClient
                .get()
                .uri(BASE_URI)
                .headers(http -> http.setBearerAuth(employeeToken))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(TimeOffDto.class)
                .returnResult()
                .getResponseBody();

        if (timeOffDtos == null) {
            return Collections.emptyList();
        }

        timeOffDtos.sort(Comparator.comparing(TimeOffDto::getId));
        return timeOffDtos;
    }

    private static LocalDateTime getLocalDateTime() {
        return LocalDateTime.of(2023, 11, 16, 12, 0, 0);
    }
}