package com.aidascorner.core.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;

import jakarta.annotation.PreDestroy;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

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
            // Use environment variable
            logger.info("Using Firebase credentials from environment variable");
            try (InputStream credentialsStream = new ByteArrayInputStream(firebaseCredentialsEnv.getBytes())) {
                credentials = GoogleCredentials.fromStream(credentialsStream);
            }
        } else {
            // Fallback to file-based credentials
            logger.info("Using Firebase credentials from file");
            try {
                credentials = GoogleCredentials.fromStream(
                    new ClassPathResource("firebase_key.json").getInputStream()
                );
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
        } else {
            firebaseApp = FirebaseApp.getInstance();
        }

        return FirestoreClient.getFirestore();
    }

    @PreDestroy
    public void onDestroy() {
        if (firebaseApp != null) {
            firebaseApp.delete();
        }
    }
}