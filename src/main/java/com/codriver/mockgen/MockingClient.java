package com.codriver.mockgen;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class MockingClient {
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Generate mock responses from an OpenAPI spec file.
     *
     * @param specFile The OpenAPI spec file (YAML or JSON).
     * @return Mock responses as a JSON string.
     * @throws IOException If file reading fails.
     */
    public String generateMockFromFile(File specFile) throws IOException {
        String specContent = Files.readString(specFile.toPath());
        return generateMockFromString(specContent);
    }

    /**
     * Generate mock responses from an OpenAPI spec string.
     *
     * @param specContent The OpenAPI spec as a string.
     * @return Mock responses as a JSON string.
     */
    public String generateMockFromString(String specContent) {

        JsonNode rootNode;
        try {
            rootNode = objectMapper.readTree(specContent);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return generateMockFromString(rootNode);
    }

    /**
     * Generate mock responses from an OpenAPI spec string.
     *
     * @param rootNode The OpenAPI spec as a JSON Root Node.
     * @return Mock responses as a JSON string.
     */
    public String generateMockFromString(JsonNode rootNode) {
        try {

            // Generate mock responses
            MockResponseGenerator generator = new MockResponseGenerator(rootNode);
            // Parse the OpenAPI spec string
            OpenApiParser parser = new OpenApiParser();
            Map<String, Map<String, JsonNode>> operations = parser.extractOperations(rootNode);
            Map<String, Object> mockResponses = new HashMap<>();

            operations.forEach((operationId, responses) -> {
                Map<String, Object> responseVariants = new HashMap<>();
                responses.forEach((statusCode, responseSchema) -> responseVariants.put(statusCode, generator.generateMockData(responseSchema)));
                mockResponses.put(operationId, responseVariants);
            });

            // Convert to JSON string and return
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(mockResponses);
        } catch (Exception e) {
            return "{\"error\": \"Failed to generate mock responses\"}";
        }
    }
}
