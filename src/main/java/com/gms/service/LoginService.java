package com.gms.service;

import java.util.Map;

public interface LoginService {

    /**
     * Authenticate a user with their credentials and role
     * @param userId the user ID (student ID, teacher ID, or username)
     * @param password the user's password
     * @param role the user role: "STUDENT", "TEACHER", or "ADMIN"
     * @return result map with "success", "message", "userId", "userName", "role"
     */
    Map<String, Object> authenticate(String userId, String password, String role);
}
