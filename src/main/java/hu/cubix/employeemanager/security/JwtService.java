package hu.cubix.employeemanager.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import hu.cubix.employeemanager.config.EMConfigurationProperties;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class JwtService {

    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String INFERIORS = "inferiors";
    public static final String MANAGER_ID = "managerId";
    public static final String MANAGER_USER_NAME = "managerUserName";
    public static final String AUTH = "auth";
    private final EMConfigurationProperties properties;

    public JwtService(EMConfigurationProperties properties) {
        this.properties = properties;
    }

    public String createJwt(EmployeeUser userDetails) {

        Map<String, String> map = new HashMap<>();
        userDetails.getInferiors().forEach((key, value) -> map.put(String.valueOf(key), value));

        return JWT.create()
                .withSubject(userDetails.getUsername())
                .withClaim(ID, userDetails.getId())
                .withClaim(NAME, userDetails.getName())
                .withClaim(INFERIORS, map)
                .withClaim(MANAGER_ID, userDetails.getManagerId())
                .withClaim(MANAGER_USER_NAME, userDetails.getManagerUserName())
                .withArrayClaim(AUTH, userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).toArray(String[]::new))
                .withExpiresAt(new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(properties.getExpiryInterval())))
                .withIssuer(properties.getIssuer())
                .sign(Algorithm.HMAC256(properties.getSecret()));
    }

    public EmployeeUser parseJwt(String jwtToken) {

        DecodedJWT decodedJwt = JWT.require(Algorithm.HMAC256(properties.getSecret()))
                .withIssuer(properties.getIssuer())
                .build()
                .verify(jwtToken);

        Map<Long, String> map = new HashMap<>();
        decodedJwt.getClaim(INFERIORS).asMap().forEach((key, value) -> map.put(Long.valueOf(key), (String) value));

        return new EmployeeUser(
                decodedJwt.getSubject(),
                "dummy",
                decodedJwt.getClaim(AUTH).asList(String.class).stream().map(SimpleGrantedAuthority::new).toList(),
                decodedJwt.getClaim(ID).asLong(),
                decodedJwt.getClaim(NAME).asString(),
                map,
                decodedJwt.getClaim(MANAGER_ID).asLong(),
                decodedJwt.getClaim(MANAGER_USER_NAME).asString());
    }
}