package com.avellar.currency_quote.controllers;

import com.avellar.currency_quote.config.SecurityConfig;
import com.avellar.currency_quote.dto.LoginRequestDto;
import com.avellar.currency_quote.dto.LoginResponseDto;
import com.avellar.currency_quote.services.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@WebMvcTest(TokenController.class)
@Import(SecurityConfig.class)
public class TokenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TokenService tokenService;

    private LoginRequestDto loginRequestDto;
    private LoginResponseDto loginResponseDto;

    @BeforeEach
    public void setup() {
        loginRequestDto = new LoginRequestDto("testuser", "password");
        loginResponseDto = new LoginResponseDto("testtoken", 1200L);
    }

    @Test
    public void testLogin_Success() throws Exception {
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
    public void testLogin_Failure() throws Exception {
        Mockito.when(tokenService.login(any(LoginRequestDto.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());

        mockMvc.perform(post("/auth/login")
                .contentType("application/json")
                .content("{\"username\": \"testuser\", \"password\": \"wrongpassword\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testGetMe_Unauthorized() throws Exception {
        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isUnauthorized());
    }
}
