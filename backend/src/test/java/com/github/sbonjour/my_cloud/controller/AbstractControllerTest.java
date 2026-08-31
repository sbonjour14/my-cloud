package com.github.sbonjour.my_cloud.controller;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.github.sbonjour.my_cloud.config.SecurityConfig;
import com.github.sbonjour.my_cloud.repository.UserRepository;
import com.github.sbonjour.my_cloud.security.JwtService;

@Import(SecurityConfig.class)
public abstract class AbstractControllerTest {

    @Autowired
    protected WebApplicationContext context;

    protected MockMvc mockMvc;

    @MockitoBean
    protected UserRepository userRepository;

    @MockitoBean
    protected JwtService jwtService;

    protected static final String COOKIE_NAME = "my-cloud-token";

    @BeforeEach
    void setUpMockMvc() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }
}
