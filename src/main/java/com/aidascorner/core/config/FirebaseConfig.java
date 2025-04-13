package com.aidascorner.core.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import jakarta.annotation.PreDestroy;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.env.Environment;

@Configuration
public class FirebaseConfig {
    private FirebaseApp firebaseApp;
    private static final Logger logger = LoggerFactory.getLogger(FirebaseConfig.class);
    private final Environment environment;

    public FirebaseConfig(Environment environment) {
        this.environment = environment;
    }

    @Bean
    public Firestore firestore() throws IOException {
        GoogleCredentials credentials;
        
        // Try to get credentials from environment variable first
        String firebaseCredentialsEnv = environment.getProperty("FIREBASE_CREDENTIALS");
        
        if (firebaseCredentialsEnv != null && !firebaseCredentialsEnv.isEmpty()) {
            logger.info("Using Firebase credentials from environment variable");
            
            try {
                // Try to clean and validate the JSON before using it
                String cleanedJson = cleanAndValidateJson(firebaseCredentialsEnv);
                logger.info("JSON validated successfully");
                
                try (InputStream credentialsStream = new ByteArrayInputStream(cleanedJson.getBytes(StandardCharsets.UTF_8))) {
                    credentials = GoogleCredentials.fromStream(credentialsStream);
                    logger.info("Credentials loaded successfully from environment variable");
                }
            } catch (Exception e) {
                logger.error("Failed to parse Firebase credentials from environment variable: {}", e.getMessage());
                logger.info("Falling back to file-based credentials");
                
                // Fall back to file-based credentials
                try {
                    credentials = GoogleCredentials.fromStream(
                        new ClassPathResource("firebase_key.json").getInputStream()
                    );
                    logger.info("Credentials loaded successfully from file");
                } catch (IOException ioException) {
                    logger.error("Failed to load Firebase credentials from file", ioException);
                    throw ioException;
                }
            }
        } else {
            // Fallback to file-based credentials
            logger.info("No environment variable found, using Firebase credentials from file");
            try {
                credentials = GoogleCredentials.fromStream(
                    new ClassPathResource("firebase_key.json").getInputStream()
                );
                logger.info("Credentials loaded successfully from file");
            } catch (IOException e) {
                logger.error("Failed to load Firebase credentials from file", e);
                throw e;
            }
        }

        FirebaseOptions options = FirebaseOptions.builder()
            .setCredentials(credentials)
            .build();

        if (FirebaseApp.getApps().isEmpty()) {
            firebaseApp = FirebaseApp.initializeApp(options);
            logger.info("Firebase application initialized successfully");
        } else {
            firebaseApp = FirebaseApp.getInstance();
            logger.info("Retrieved existing Firebase application instance");
        }

        return FirestoreClient.getFirestore();
    }
    
    /**
     * Clean and validate the JSON string to ensure it's properly formatted
     */
    private String cleanAndValidateJson(String jsonString) {
        try {
            // First try to parse as-is
            Gson gson = new GsonBuilder().create();
            JsonObject jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();
            return gson.toJson(jsonObject);
        } catch (Exception e) {
            logger.warn("First JSON parse attempt failed: {}", e.getMessage());
            
            try {
                // Try to decode if it's Base64 encoded
                byte[] decodedBytes = Base64.getDecoder().decode(jsonString);
                String decodedString = new String(decodedBytes, StandardCharsets.UTF_8);
                
                // Parse and re-serialize to ensure proper JSON format
                Gson gson = new GsonBuilder().create();
                JsonObject jsonObject = JsonParser.parseString(decodedString).getAsJsonObject();
                return gson.toJson(jsonObject);
            } catch (Exception e2) {
                logger.warn("Base64 decode attempt failed: {}", e2.getMessage());
                
                // Try to clean the string: remove any leading/trailing whitespace and quotes
                String cleaned = jsonString.trim();
                if (cleaned.startsWith("\"") && cleaned.endsWith("\"")) {
                    // Remove surrounding quotes and try to parse again
                    cleaned = cleaned.substring(1, cleaned.length() - 1);
                }
                
                // Replace escaped quotes with actual quotes
                cleaned = cleaned.replace("\\\"", "\"");
                
                // Parse and re-serialize to ensure proper JSON format
                Gson gson = new GsonBuilder().create();
                JsonObject jsonObject = JsonParser.parseString(cleaned).getAsJsonObject();
                return gson.toJson(jsonObject);
            }
        }
    }

    @PreDestroy
    public void onDestroy() {
        if (firebaseApp != null) {
            firebaseApp.delete();
        }
    }
}