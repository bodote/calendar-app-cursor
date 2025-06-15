package de.bas.bodo.woodle.domain.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.bas.bodo.woodle.domain.model.PollData;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Slf4j
@Service
public class PollStorageService {

    @Autowired
    private S3Client s3Client;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${app.s3.bucket-name}")
    private String bucketName;

    @Value("${app.base-url}")
    private String baseUrl;

    public String storePoll(PollData pollData) throws Exception {
        String pollId = UUID.randomUUID().toString();
        String key = "polls/" + pollId + ".json";

        // Convert poll data to JSON
        String jsonData = objectMapper.writeValueAsString(pollData);

        // Store in S3
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType("application/json")
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromString(jsonData));
        log.info("object successfully stored in s3!");

        // Generate and return the poll URL
        return baseUrl + "/event/" + pollId;
    }

    public void storePollWithUuid(String pollId, PollData pollData) throws Exception {
        String key = "polls/" + pollId + ".json";

        // Convert poll data to JSON
        String jsonData = objectMapper.writeValueAsString(pollData);

        // Store in S3
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType("application/json")
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromString(jsonData));
        log.info("Updated poll data successfully stored in S3 with UUID: {}", pollId);
    }
}