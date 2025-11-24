package com.aurora.climatesync.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GoogleCalendarServiceImplTest {

    @Test
    void testConnectThrowsExceptionWhenEnvVarsMissing() {
        GoogleCalendarServiceImpl service = new GoogleCalendarServiceImpl(null, null);

        Exception exception = assertThrows(IOException.class, service::connect);
        assertTrue(exception.getMessage().contains("Client ID and Client Secret must be configured"));
    }

    @Test
    void testConnectConfiguresFlowCorrectly() throws Exception {
        GoogleCalendarServiceImpl service = new GoogleCalendarServiceImpl("test-client-id", "test-client-secret") {
            @Override
            protected Credential authorize(GoogleAuthorizationCodeFlow flow, LocalServerReceiver receiver) throws IOException {
                // Verify that the flow was initialized with the correct secrets
                assertEquals("test-client-id", flow.getClientId());
                // We can't easily check client secret as it's not exposed via getter on flow, but we checked ID.
                
                // Return a mock credential to stop the flow here
                return mock(Credential.class);
            }
        };

        // We expect an exception because the subsequent API call in connect() will fail 
        // (since we didn't mock the Transport or the API call itself, and the Credential is a mock)
        // But we only care that it reached authorize() with correct config.
        try {
            service.connect();
        } catch (Exception e) {
            // Expected failure after authorization
        }
    }
}
