// File: src/test/java/com/rideapp/auth/AuthServiceTest.java
package com.rideapp.auth;

import com.rideapp.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest {
    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = AuthService.getInstance();
        // Since it's a Singleton, it persists across tests. 
        // In a real app, we'd add a method to clear the map for test isolation.
        authService.registerUser("PASSENGER", "unit_tester", "Secret123!");
    }

    @Test
    void testSingletonInstance() {
        AuthService instance1 = AuthService.getInstance();
        AuthService instance2 = AuthService.getInstance();
        
        assertSame(instance1, instance2, "AuthService must return the exact same instance in memory");
    }

    @Test
    void testSuccessfulLogin() {
        User loggedIn = authService.login("unit_tester", "Secret123!");
        
        assertNotNull(loggedIn, "Login should succeed with correct credentials");
        assertEquals("unit_tester", loggedIn.getUsername());
        assertEquals("PASSENGER", loggedIn.getRole());
    }

    @Test
    void testFailedLoginWithWrongPassword() {
        User loggedIn = authService.login("unit_tester", "WrongPassword");
        
        assertNull(loggedIn, "Login should fail and return null with incorrect credentials");
    }

    @Test
    void testFailedLoginWithUnknownUser() {
        User loggedIn = authService.login("ghost_user", "Secret123!");
        
        assertNull(loggedIn, "Login should fail for unregistered usernames");
    }
}