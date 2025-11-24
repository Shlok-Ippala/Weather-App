package com.aurora.climatesync;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
    "spring.security.oauth2.client.registration.google.client-id=mock-client-id",
    "spring.security.oauth2.client.registration.google.client-secret=mock-client-secret"
})
class ClimatesyncApplicationTests {

    @Test
    void contextLoads() {
    }

}
