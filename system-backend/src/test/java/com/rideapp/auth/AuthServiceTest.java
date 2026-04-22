// File: src/test/java/com/rideapp/auth/AuthServiceTest.java
package com.rideapp.auth;

import com.rideapp.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TEST RECORD & METHODOLOGY:
 * --------------------------
 * WHAT IS TESTED:
 * 1. User Registration: Validation of uniqueness and password hashing integrity.
 * 2. User Login: Validation of credential matching and BCrypt verification.
 * 3. State Management: Ensuring the Singleton instance doesn't leak data between tests.
 * 4. Edge Cases: Handling of null inputs, non-existent users, and duplicate usernames.
 *
 * HOW IT IS TESTED:
 * - Framework: JUnit 5 (Jupiter).
 * - State Control: Since AuthService is a Singleton with a private map, Java Reflection 
 * is used in @BeforeEach to clear 'userDatabase' before every test to ensure isolation.
 * - Assertions: Used to verify object existence, property correctness, and return values.
 */
class AuthServiceTest {

    private AuthService authService;

    @BeforeEach
    void setUp() throws Exception {
        authService = AuthService.getInstance();

        // RESET SINGLETON STATE
        // Because the instance persists across tests in the JVM, we manually clear 
        // the internal map to prevent "Alice" from one test appearing in another.
        Field field = AuthService.class.getDeclaredField("userDatabase");
        field.setAccessible(true);
        Map<?, ?> map = (Map<?, ?>) field.get(authService);
        map.clear();
    }

    @Test
    @DisplayName("Should successfully register and then login a valid user")
    void testRegistrationAndLoginFlow() {
        String user = "testUser";
        String pass = "plainText123";
        
        authService.registerUser("DRIVER", user, pass);
        User result = authService.login(user, pass);

        assertNotNull(result, "Login should return a User object on success.");
        assertEquals(user, result.getUsername());
        assertEquals("DRIVER", result.getRole());
    }

    @Test
    @DisplayName("Should fail login if the password does not match")
    void testLoginWithWrongPassword() {
        authService.registerUser("PASSENGER", "bob", "correct_pass");
        
        User result = authService.login("bob", "wrong_pass");
        
        assertNull(result, "Login should return null for incorrect passwords.");
    }

    @Test
    @DisplayName("Should fail login if the username does not exist")
    void testLoginWithNonExistentUser() {
        User result = authService.login("ghost", "any_pass");
        
        assertNull(result, "Login should return null for unknown users.");
    }

    @Test
    @DisplayName("Should prevent registering a duplicate username")
    void testDuplicateRegistration() {
        authService.registerUser("DRIVER", "uniqueUser", "pass1");
        
        // Attempt to register again with same name but different role/pass
        authService.registerUser("PASSENGER", "uniqueUser", "pass2");
        
        User result = authService.login("uniqueUser", "pass1");
        assertNotNull(result);
        assertEquals("DRIVER", result.getRole(), "The first registration should remain unchanged.");
        
        User failedResult = authService.login("uniqueUser", "pass2");
        assertNull(failedResult, "The duplicate registration should not have updated the password.");
    }

    @Test
    @DisplayName("Verify password security: Password should be hashed, not plain text")
    void testPasswordHashingSecurity() {
        String rawPass = "secret_password";
        authService.registerUser("PASSENGER", "secureUser", rawPass);
        
        User user = authService.login("secureUser", rawPass);
        
        assertNotNull(user);
        assertNotEquals(rawPass, user.getHashedPassword(), "The stored password must be hashed.");
        assertTrue(user.getHashedPassword().startsWith("$2a$"), "Hashed password should be in BCrypt format.");
    }

    @Test
    @DisplayName("Edge Case: Handle null or empty credentials")
    void testNullInputs() {
        // Test registration with nulls
        assertDoesNotThrow(() -> authService.registerUser(null, null, null));
        
        // Test login with nulls
        User loginResult = authService.login(null, null);
        assertNull(loginResult, "Login with nulls should return null, not throw Exception.");
    }
}