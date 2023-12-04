package hu.cubix.employeemanager.security;

import hu.cubix.employeemanager.dto.LoginDto;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class JwtLoginController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public JwtLoginController(AuthenticationManager authenticationManager,
                              JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping("/api/login")
    public String login(@Validated @RequestBody LoginDto loginDto) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginDto.username(), loginDto.password()));
        return jwtService.createJwt((EmployeeUser) authentication.getPrincipal());
    }
}