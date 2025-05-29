package de.bas.bodo.woodle.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

@Configuration
@Profile("!test")
public class S3Config {

        @Value("${app.s3.endpoint}")
        private String endpoint;

        @Value("${app.s3.access-key}")
        private String accessKey;

        @Value("${app.s3.secret-key}")
        private String secretKey;

        @Bean
        public S3Client s3Client() {
                return S3Client.builder()
                                .endpointOverride(URI.create(endpoint))
                                .credentialsProvider(StaticCredentialsProvider.create(
                                                AwsBasicCredentials.create(accessKey, secretKey)))
                                .region(Region.US_EAST_1) // Required but not used with MinIO
                                .serviceConfiguration(S3Configuration.builder()
                                                .pathStyleAccessEnabled(true)
                                                .build())
                                .build();
        }
}