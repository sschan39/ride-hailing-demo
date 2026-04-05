// File: AuthService.java
package com.rideapp.auth;

import com.rideapp.models.User;
import org.mindrot.jbcrypt.BCrypt;
import java.util.HashMap;
import java.util.Map;

public class AuthService {
    private static AuthService instance;
    
    // Our in-memory database: maps username -> User object
    private Map<String, User> userDatabase;

    private AuthService() {
        userDatabase = new HashMap<>();
    }

    public static AuthService getInstance() {
        if (instance == null) {
            instance = new AuthService();
        }
        return instance;
    }

    // 1. Registration: Hashes the password BEFORE saving
    public void registerUser(String role, String username, String plainTextPassword) {
        if (userDatabase.containsKey(username)) {
            System.out.println("Error: Username '" + username + "' already exists.");
            return;
        }

        // Hash the password with a generated salt
        String hashed = BCrypt.hashpw(plainTextPassword, BCrypt.gensalt());
        
        // Use the Factory to create the user
        User newUser = UserFactory.createUser(role, username, hashed);
        userDatabase.put(username, newUser);
        
        System.out.println("Successfully registered " + role + ": " + username);
    }

    // 2. Login: Compares plain text input against the stored hash
    public User login(String username, String plainTextPassword) {
        User user = userDatabase.get(username);
        
        if (user == null) {
            System.out.println("Login failed: User not found.");
            return null;
        }

        // BCrypt handles extracting the salt from the hash and verifying
        if (BCrypt.checkpw(plainTextPassword, user.getHashedPassword())) {
            System.out.println("Login successful! Welcome, " + user.getUsername() + " (" + user.getRole() + ")");
            return user;
        } else {
            System.out.println("Login failed: Incorrect password.");
            return null;
        }
    }
}