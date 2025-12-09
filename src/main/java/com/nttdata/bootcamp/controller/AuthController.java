package com.nttdata.bootcamp.controller;


import com.nttdata.bootcamp.entity.AuthRequest;
import com.nttdata.bootcamp.entity.AuthResponse;
import com.nttdata.bootcamp.util.JwtUtil;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserDetailsRepositoryReactiveAuthenticationManager authManager;
    private final ReactiveUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;

    public AuthController(UserDetailsRepositoryReactiveAuthenticationManager authManager,
                          ReactiveUserDetailsService userDetailsService,
                          JwtUtil jwtUtil) {
        this.authManager = authManager;
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<AuthResponse> login(@Validated @RequestBody AuthRequest request) {

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword());

        return authManager.authenticate(authToken)
                .flatMap(auth -> userDetailsService.findByUsername(request.getUsername()))
                .map(userDetails -> new AuthResponse(jwtUtil.generateToken(userDetails)))
                .switchIfEmpty(Mono.error(new RuntimeException("Credenciales inválidas")));
    }
}
