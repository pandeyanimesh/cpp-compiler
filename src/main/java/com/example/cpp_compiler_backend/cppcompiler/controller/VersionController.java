package com.example.cpp_compiler_backend.cppcompiler.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootVersion;
import org.springframework.core.SpringVersion;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class VersionController {

    @GetMapping("/version")
    public ResponseEntity<Map<String, String>> getVersionInfo() {
        Map<String, String> versions = new HashMap<>();
        versions.put("spring-core", SpringVersion.getVersion());
        versions.put("spring-boot", SpringBootVersion.getVersion());
        
        return ResponseEntity.ok(versions);
    }
}