package com.example.hmrback.auth.service;

import com.example.hmrback.mapper.UserMapper;
import com.example.hmrback.model.User;
import com.example.hmrback.model.request.AuthRequest;
import com.example.hmrback.model.request.RegisterRequest;
import com.example.hmrback.model.response.AuthResponse;
import com.example.hmrback.persistence.entity.RoleEntity;
import com.example.hmrback.persistence.entity.UserEntity;
import com.example.hmrback.persistence.enums.RoleEnum;
import com.example.hmrback.persistence.repository.RoleRepository;
import com.example.hmrback.persistence.repository.UserRepository;
import com.example.hmrback.utils.test.CommonTestUtils;
import com.example.hmrback.utils.test.EntityTestUtils;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.util.Optional;

import static com.example.hmrback.exception.util.ExceptionMessageConstants.ROLE_NOT_FOUND_MESSAGE;
import static com.example.hmrback.exception.util.ExceptionMessageConstants.USER_ALREADY_EXISTS_MESSAGE;
import static com.example.hmrback.exception.util.ExceptionMessageConstants.USER_NOT_FOUND_EMAIL_MESSAGE;
import static com.example.hmrback.utils.test.TestConstants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = AuthenticationService.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthenticationServiceTest {

    @Autowired
    private AuthenticationService service;

    @MockitoBean
    private UserRepository userRepository;
    @MockitoBean
    private RoleRepository roleRepository;

    @MockitoBean
    private PasswordEncoder passwordEncoder;
    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private UserMapper userMapper;

    @MockitoBean
    private JwtService jwtService;

    private static RoleEntity userRole;
    private static UserEntity user;
    private static String token;

    private static RegisterRequest registerRequest;
    private static AuthRequest authRequest;

    private static final LocalDate now = LocalDate.now();

    @BeforeAll
    static void setup() {
        // Role
        userRole = EntityTestUtils.buildRoleEntity();

        // User
        user = EntityTestUtils.buildUserEntity(NUMBER_1, false);

        // Token
        token = "test-token-666";

        // Requests
        registerRequest = CommonTestUtils.buildRegisterRequest();
        authRequest = CommonTestUtils.buildAuthRequest();
    }

    @Test
    @Order(1)
    void register() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(roleRepository.findByName(any(RoleEnum.class))).thenReturn(Optional.ofNullable(userRole));
        when(userMapper.toEntity(any(User.class))).thenReturn(user);
        when(passwordEncoder.encode(anyString())).thenReturn(ENCODED_PASSWORD);
        when(userRepository.save(any(UserEntity.class))).thenReturn(user);
        when(jwtService.generateToken(any(UserEntity.class))).thenReturn(token);

        AuthResponse result = service.register(registerRequest);

        assertNotNull(result, NOT_NULL_MESSAGE.formatted("AuthResponse"));
        assertNotNull(result.token(), NOT_NULL_MESSAGE.formatted("token"));

        assertEquals(now, user.getInscriptionDate(), SHOULD_BE_EQUALS_MESSAGE.formatted("Inscription date", now));

        verify(userRepository, times(1)).existsByEmail(EMAIL.formatted(NUMBER_1));
        verify(roleRepository, times(1)).findByName(RoleEnum.ROLE_USER);
        verify(userMapper, times(1)).toEntity(any(User.class));
        verify(passwordEncoder, times(1)).encode(PASSWORD);
        verify(userRepository, times(1)).save(any(UserEntity.class));
        verify(jwtService, times(1)).generateToken(any(UserEntity.class));
    }

    @Test
    @Order(2)
    void register_whenUserAlreadyExists() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.register(registerRequest));

        assertNotNull(ex, NOT_NULL_MESSAGE.formatted("Exception"));
        assertEquals(USER_ALREADY_EXISTS_MESSAGE.formatted(EMAIL.formatted(NUMBER_1)), ex.getMessage(), EXCEPTION_MESSAGE_SHOULD_MATCH);

        verify(userRepository, times(1)).existsByEmail(EMAIL.formatted(NUMBER_1));
        verify(roleRepository, times(0)).findByName(RoleEnum.ROLE_USER);
        verify(userMapper, times(0)).toEntity(any(User.class));
        verify(passwordEncoder, times(0)).encode(PASSWORD);
        verify(userRepository, times(0)).save(any(UserEntity.class));
        verify(jwtService, times(0)).generateToken(any(UserEntity.class));
    }

    @Test
    @Order(3)
    void register_whenRoleNotFound() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(roleRepository.findByName(any(RoleEnum.class))).thenReturn(Optional.empty());

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.register(registerRequest));

        assertNotNull(ex, NOT_NULL_MESSAGE.formatted("Exception"));
        assertEquals(ROLE_NOT_FOUND_MESSAGE.formatted("ROLE_USER"), ex.getMessage(), EXCEPTION_MESSAGE_SHOULD_MATCH);

        verify(userRepository, times(1)).existsByEmail(EMAIL.formatted(NUMBER_1));
        verify(roleRepository, times(1)).findByName(RoleEnum.ROLE_USER);
        verify(userMapper, times(0)).toEntity(any(User.class));
        verify(passwordEncoder, times(0)).encode(PASSWORD);
        verify(userRepository, times(0)).save(any(UserEntity.class));
        verify(jwtService, times(0)).generateToken(any(UserEntity.class));
    }

    @Test
    @Order(4)
    void authenticate() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.ofNullable(user));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null);
        when(jwtService.generateToken(any(UserEntity.class))).thenReturn(token);

        AuthResponse result = service.authenticate(authRequest);

        assertNotNull(result, NOT_NULL_MESSAGE.formatted("AuthResponse"));
        assertNotNull(result.token(), NOT_NULL_MESSAGE.formatted("token"));

        verify(userRepository, times(1)).findByEmail(anyString());
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService, times(1)).generateToken(any(UserEntity.class));
    }

    @Test
    @Order(5)
    void authenticate_whenUserIsNotFound() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.authenticate(authRequest));

        assertNotNull(ex, NOT_NULL_MESSAGE.formatted("AuthResponse"));
        assertEquals(USER_NOT_FOUND_EMAIL_MESSAGE.formatted(EMAIL.formatted(NUMBER_1)), ex.getMessage(), EXCEPTION_MESSAGE_SHOULD_MATCH);

        verify(userRepository, times(1)).findByEmail(anyString());
        verify(authenticationManager, times(0)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService, times(0)).generateToken(any(UserEntity.class));
    }

}
