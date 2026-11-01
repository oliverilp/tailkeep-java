package org.tailkeep.adapter.web.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tailkeep.domain.auth.Authenticate;
import org.tailkeep.domain.auth.Logout;
import org.tailkeep.domain.auth.RefreshToken;
import org.tailkeep.domain.auth.Register;
import org.tailkeep.domain.auth.Registration;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {
    private static final String BEARER_PREFIX = "Bearer ";

    private final Register register;
    private final Authenticate authenticate;
    private final RefreshToken refreshToken;
    private final Logout logout;

    public AuthenticationController(Register register, Authenticate authenticate, RefreshToken refreshToken,
            Logout logout) {
        this.register = register;
        this.authenticate = authenticate;
        this.refreshToken = refreshToken;
        this.logout = logout;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody RegisterRequest request) {
        Registration registration = new Registration(request.nickname(), request.username(), request.password());
        return ResponseEntity.ok(AuthenticationResponse.from(register.execute(registration)));
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        return ResponseEntity.ok(
                AuthenticationResponse.from(authenticate.execute(request.username(), request.password())));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthenticationResponse> refreshToken(@RequestBody Map<String, String> request) {
        return ResponseEntity.ok(AuthenticationResponse.from(refreshToken.execute(request.get("refreshToken"))));
    }

    @PostMapping("/logout")
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        response.setStatus(revokeBearerToken(request) ? HttpServletResponse.SC_OK : HttpServletResponse.SC_UNAUTHORIZED);
    }

    private boolean revokeBearerToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            return false;
        }
        try {
            return logout.execute(authHeader.substring(BEARER_PREFIX.length()));
        } catch (RuntimeException e) {
            return false;
        }
    }
}
