package de.bas.bodo.woodle.config;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import software.amazon.awssdk.services.s3.S3Client;

@TestConfiguration
public class WebMvcTestConfig {

    @Bean
    @Primary
    public S3Client s3Client() {
        return Mockito.mock(S3Client.class);
    }
}