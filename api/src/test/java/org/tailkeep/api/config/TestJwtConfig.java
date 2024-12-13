package org.tailkeep.api.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetails;
import org.tailkeep.api.service.JwtService;
import org.mockito.Mockito;

import java.util.UUID;

@TestConfiguration
public class TestJwtConfig {

    @Bean
    @Primary
    public JwtService jwtService() {
        JwtService mockJwtService = Mockito.mock(JwtService.class);
        
        Mockito.when(mockJwtService.generateToken(Mockito.any(UserDetails.class)))
            .thenAnswer(invocation -> "test.access." + UUID.randomUUID().toString());
        
        Mockito.when(mockJwtService.generateRefreshToken(Mockito.any(UserDetails.class)))
            .thenAnswer(invocation -> "test.refresh." + UUID.randomUUID().toString());
            
        Mockito.when(mockJwtService.isTokenValid(Mockito.anyString(), Mockito.any(UserDetails.class)))
            .thenReturn(true);
            
        return mockJwtService;
    }
} 
