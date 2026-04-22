// File: src/test/java/com/rideapp/auth/UserFactoryTest.java
package com.rideapp.auth;

import com.rideapp.models.Driver;
import com.rideapp.models.Passenger;
import com.rideapp.models.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TEST RECORD & METHODOLOGY:
 * --------------------------
 * WHAT IS TESTED:
 * 1. Object Creation: Verification that the factory returns the correct concrete subclass (Driver/Passenger).
 * 2. Case Sensitivity: Ensures the "role" string is handled case-insensitively (e.g., "driver" vs "DRIVER").
 * 3. Exception Handling: Validation that invalid or "wrong" roles trigger the expected IllegalArgumentException.
 * 4. Data Integrity: Checking that fields (username/password) are passed correctly to the created objects.
 *
 * HOW IT IS TESTED:
 * - Framework: JUnit 5 (Jupiter).
 * - Strategy: Logical path testing. No mocks/stubs are used because the Factory creates simple 
 * domain objects, and we want to verify the actual type instantiation (instanceof).
 * - Assertions: Used assertThrows for error paths and assertInstanceOf for success paths.
 */
class UserFactoryTest {

    @Test
    @DisplayName("Should create a Passenger instance when role is 'PASSENGER'")
    void testCreatePassenger_Success() {
        // Arrange
        String role = "PASSENGER";
        String username = "alice_p";
        String pass = "hash123";

        // Act
        User user = UserFactory.createUser(role, username, pass);

        // Assert
        assertNotNull(user);
        assertTrue(user instanceof Passenger, "Object should be an instance of Passenger");
        assertEquals(username, user.getUsername());
        assertEquals(pass, user.getHashedPassword());
    }

    @Test
    @DisplayName("Should create a Driver instance when role is 'driver' (Lowercase check)")
    void testCreateDriver_CaseInsensitive() {
        // Arrange
        String role = "driver"; // Testing case insensitivity
        String username = "bob_d";
        String pass = "hash456";

        // Act
        User user = UserFactory.createUser(role, username, pass);

        // Assert
        assertNotNull(user);
        assertInstanceOf(Driver.class, user, "Should handle lowercase role strings correctly");
        assertEquals(username, user.getUsername());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for an unknown role")
    void testCreateUser_InvalidRole_ThrowsException() {
        // Arrange
        String invalidRole = "ADMIN";

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            UserFactory.createUser(invalidRole, "user", "pass");
        });

        assertTrue(exception.getMessage().contains("Unknown user role"), "Error message should mention the unknown role");
    }

    @Test
    @DisplayName("Should throw NullPointerException when role is null")
    void testCreateUser_NullRole_ThrowsException() {
        // Act & Assert
        // This tests an 'edge case' that would likely cause a crash in the .equalsIgnoreCase call
        assertThrows(NullPointerException.class, () -> {
            UserFactory.createUser(null, "user", "pass");
        }, "The factory should fail if the role provided is null");
    }

    @Test
    @DisplayName("Should handle empty string as an invalid role")
    void testCreateUser_EmptyRole_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            UserFactory.createUser("", "user", "pass");
        });
    }

    @Test
    @DisplayName("Verify object separation: Driver is not a Passenger")
    void testTypeSeparation() {
        // This is a "Clearly Wrong" check to ensure the logic isn't crossing wires
        User driver = UserFactory.createUser("DRIVER", "test", "pass");
        
        assertFalse(driver instanceof Passenger, "A Driver object should not be identifiable as a Passenger");
    }
}