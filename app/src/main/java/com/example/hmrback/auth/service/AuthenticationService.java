package com.example.hmrback.auth.service;

import com.example.hmrback.mapper.UserMapper;
import com.example.hmrback.model.request.AuthRequest;
import com.example.hmrback.model.request.RegisterRequest;
import com.example.hmrback.model.response.AuthResponse;
import com.example.hmrback.persistence.entity.RoleEntity;
import com.example.hmrback.persistence.entity.UserEntity;
import com.example.hmrback.persistence.enums.RoleEnum;
import com.example.hmrback.persistence.repository.RoleRepository;
import com.example.hmrback.persistence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static com.example.hmrback.exception.util.ExceptionMessageConstants.ROLE_NOT_FOUND_MESSAGE;
import static com.example.hmrback.exception.util.ExceptionMessageConstants.USER_ALREADY_EXISTS_MESSAGE;
import static com.example.hmrback.exception.util.ExceptionMessageConstants.USER_NOT_FOUND_EMAIL_MESSAGE;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private static final Logger LOG = LoggerFactory.getLogger(AuthenticationService.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    private final UserMapper userMapper;

    private final JwtService jwtService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        LOG.info("Register request for User: {}", request.user());

        if (userRepository.existsByEmail(request.user().email())) {
            throw new IllegalArgumentException(USER_ALREADY_EXISTS_MESSAGE.formatted(request.user().email()));
        }

        RoleEntity userRole = roleRepository.findByName(RoleEnum.ROLE_USER)
            .orElseThrow(() -> new IllegalStateException(ROLE_NOT_FOUND_MESSAGE.formatted("ROLE_USER")));

        UserEntity user = this.userMapper.toEntity(request.user());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setInscriptionDate(LocalDate.now());

        user.getRoles().add(userRole);
        userRepository.save(user);

        String token = jwtService.generateToken(user);
        return new AuthResponse(token);
    }

    public AuthResponse authenticate(AuthRequest request) {
        LOG.info("Authenticate user: {}", request.email());

        UserEntity user = userRepository.findByEmail(request.email())
            .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND_EMAIL_MESSAGE.formatted(request.email())));

        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        String token = jwtService.generateToken(user);
        return new AuthResponse(token);
    }
}
