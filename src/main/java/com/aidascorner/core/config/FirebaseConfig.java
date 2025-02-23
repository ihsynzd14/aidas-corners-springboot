package com.aidascorner.core.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;

import jakarta.annotation.PreDestroy;

import java.io.IOException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class FirebaseConfig {
    private FirebaseApp firebaseApp;

    @Bean
    public Firestore firestore() throws IOException {
        GoogleCredentials credentials = GoogleCredentials.fromStream(
            new ClassPathResource("firebase_key.json").getInputStream()
        );

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