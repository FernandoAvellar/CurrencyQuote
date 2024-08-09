package com.avellar.currency_quote.controllers;

import com.avellar.currency_quote.dto.CreateUserDto;
import com.avellar.currency_quote.exception.UserAlreadyExistsException;
import com.avellar.currency_quote.services.UserService;
import com.avellar.currency_quote.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
public class UserControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private UserService userService;

        @MockBean
        private UserDetailsService userDetailsService;

        @Test
        public void testCreateNewUser_Success() throws Exception {
                Mockito.when(userService.createUser(any(CreateUserDto.class)))
                                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).build());

                mockMvc.perform(post("/users/register")
                                .contentType("application/json")
                                .content("{\"username\": \"testuser\", \"password\": \"password\"}"))
                                .andExpect(status().isCreated());
        }

        @Test
        public void testCreateNewUserThatAlreadyExists_Failure() throws Exception {
                Mockito.when(userService.createUser(any(CreateUserDto.class)))
                                .thenThrow(new UserAlreadyExistsException("user already exists"));

                mockMvc.perform(post("/users/register")
                                .contentType("application/json")
                                .content("{\"username\": \"\", \"password\": \"\"}"))
                                .andExpect(status().isUnprocessableEntity())
                                .andExpect(content().string("user already exists"));
        }
}
