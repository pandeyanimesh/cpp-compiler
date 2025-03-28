package com.example.cpp_compiler_backend.cppcompiler.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.cpp_compiler_backend.cppcompiler.model.CompileRequest;
import com.example.cpp_compiler_backend.cppcompiler.model.CompileResponse;
import com.example.cpp_compiler_backend.cppcompiler.service.CompilerService;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS})
public class CompilerController {

    private final CompilerService compilerService;

    @Autowired
    public CompilerController(CompilerService compilerService) {
        this.compilerService = compilerService;
    }

    @PostMapping("/compile")
    public ResponseEntity<CompileResponse> compileCode(@RequestBody CompileRequest request) {
        CompileResponse response = new CompileResponse();
        
        if (request.getCode() == null || request.getCode().trim().isEmpty()) {
            response.setSuccess(false);
            response.setMessage("Code cannot be empty");
            return ResponseEntity.badRequest().body(response);
        }
        
        try {
            CompilerService.CompileResult result = compilerService.compileCppCode(request.getCode());
            response.setSuccess(result.isSuccess());
            response.setOutput(result.getOutput());
            
            if (!result.isSuccess()) {
                response.setMessage("Compilation or execution failed");
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.setSuccess(false);
            response.setMessage("Server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Add a simple test endpoint to check if the controller is accessible
    @GetMapping("/test")
    public ResponseEntity<String> testEndpoint() {
        return ResponseEntity.ok("API is working!");
    }
}