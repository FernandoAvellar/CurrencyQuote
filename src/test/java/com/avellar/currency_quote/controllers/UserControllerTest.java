package com.avellar.currency_quote.controllers;

import com.avellar.currency_quote.dto.RegisterUserDto;
import com.avellar.currency_quote.entities.User;
import com.avellar.currency_quote.services.UserService;
import com.avellar.currency_quote.config.SecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private JwtEncoder jwtEncoder;

    private String jwtToken;

    private RegisterUserDto registerUserDto;
    private User user;

    @BeforeEach
    public void setup() {
        registerUserDto = new RegisterUserDto("testuser", "userpassword");
        user = new User(null, "testuser", "userpassword");

        // Mock the JwtEncoder to return a valid JWT token
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("currency_quote_backend")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(1200L))
                .subject("testuser")
                .build();

        Jwt jwt = new Jwt("token", now, now.plusSeconds(1200L), Map.of("alg", "RS256"), claims.getClaims());
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(jwt);

        // Generate a valid JWT token
        jwtToken = jwt.getTokenValue();
    }

    @Test
    public void testNewUser_Success() throws Exception {
        Mockito.when(userService.newUser(any(RegisterUserDto.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).build());

        mockMvc.perform(post("/users/register")
                .contentType("application/json")
                .content("{\"username\": \"testuser\", \"password\": \"password\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    public void testNewUser_Failure() throws Exception {
        Mockito.when(userService.newUser(any(RegisterUserDto.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.BAD_REQUEST).build());

        mockMvc.perform(post("/users/register")
                .contentType("application/json")
                .content("{\"username\": \"\", \"password\": \"\"}"))
                .andExpect(status().isBadRequest());
    }
}
