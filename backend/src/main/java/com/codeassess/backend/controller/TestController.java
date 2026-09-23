package com.codeassess.backend.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Test controller to demonstrate JWT authentication and role-based authorization.
 */
@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = "http://localhost:5173")
public class TestController {

    /**
     * Endpoint accessible to any authenticated user.
     * GET /api/test/user
     */
    @GetMapping("/user")
    public Map<String, Object> userEndpoint() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Hello authenticated user!");
        response.put("email", auth.getName());
        response.put("authorities", auth.getAuthorities());
        
        return response;
    }

    /**
     * Endpoint accessible only to CANDIDATE role.
     * GET /api/test/candidate
     */
    @GetMapping("/candidate")
    @PreAuthorize("hasRole('CANDIDATE')")
    public Map<String, Object> candidateEndpoint() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Hello candidate!");
        response.put("email", auth.getName());
        response.put("role", "CANDIDATE");
        
        return response;
    }

    /**
     * Endpoint accessible only to ADMIN role.
     * GET /api/test/admin
     */
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> adminEndpoint() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Hello admin!");
        response.put("email", auth.getName());
        response.put("role", "ADMIN");
        
        return response;
    }
}
