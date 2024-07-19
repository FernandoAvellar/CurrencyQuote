package com.avellar.currency_quote.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.web.servlet.MockMvc;

import com.avellar.currency_quote.config.SecurityConfig;
import com.avellar.currency_quote.dto.LoginRequestDto;
import com.avellar.currency_quote.dto.LoginResponseDto;
import com.avellar.currency_quote.entities.User;
import com.avellar.currency_quote.repositories.UserRepository;
import com.avellar.currency_quote.services.TokenService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(TokenController.class)
@Import(SecurityConfig.class)
public class TokenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TokenService tokenService;

    @MockBean
    private JwtEncoder jwtEncoder;

    @MockBean
    private JwtDecoder jwtDecoder;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private BCryptPasswordEncoder passwordEncoder;

    private User user;
    private LoginResponseDto loginResponseDto;

    @BeforeEach
    public void setup() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword(passwordEncoder.encode("password"));

        Instant now = Instant.now();

        Map<String, Object> headers = new HashMap<>();
        headers.put("alg", "none");

        Map<String, Object> claimsMap = new HashMap<>();
        claimsMap.put("iss", "currency_quote_backend");
        claimsMap.put("sub", user.getId().toString());
        claimsMap.put("iat", now);
        claimsMap.put("exp", now.plusSeconds(1200L));

        var jwt = new Jwt("testtoken", now, now.plusSeconds(1200L), headers, claimsMap);

        Mockito.when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(jwt);
        Mockito.when(jwtDecoder.decode(anyString())).thenReturn(jwt);
        Mockito.when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        Mockito.when(userRepository.findById(any(Long.class))).thenReturn(Optional.of(user));

        loginResponseDto = new LoginResponseDto(jwt.getTokenValue(), 1200L);
    }

    @Test
    public void testLoginWithValidToken_Success() throws Exception {
        Mockito.when(tokenService.login(any(LoginRequestDto.class)))
                .thenReturn(ResponseEntity.ok(loginResponseDto));

        mockMvc.perform(post("/auth/login")
                .contentType("application/json")
                .content("{\"username\": \"testuser\", \"password\": \"password\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("testtoken"))
                .andExpect(jsonPath("$.expiresIn").value("1200"));
    }

    @Test
    public void testLogin_BadCredentials() throws Exception {
        LoginRequestDto loginRequestDto = new LoginRequestDto("testuser", "wrongpassword");

        Mockito.when(tokenService.login(any(LoginRequestDto.class)))
                .thenThrow(new BadCredentialsException("user or password is invalid!"));

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(loginRequestDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testGetMe_Success() throws Exception {
        mockMvc.perform(get("/auth/me")
                .header("Authorization", "Bearer testtoken"))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetMe_Unauthorized() throws Exception {
        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isUnauthorized());
    }
}
