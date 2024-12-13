package org.tailkeep.api.integration;

import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;

import org.springframework.lang.NonNull;

public class TestRestTemplateResponseErrorHandler extends DefaultResponseErrorHandler {
    
    @Override
    public void handleError(@NonNull ClientHttpResponse response) throws IOException {
        if (response.getStatusCode() == HttpStatus.UNAUTHORIZED) {
            throw HttpClientErrorException.create(
                HttpStatus.UNAUTHORIZED,
                "Unauthorized",
                response.getHeaders(),
                new byte[0],
                null
            );
        }
        super.handleError(response);
    }
} 
