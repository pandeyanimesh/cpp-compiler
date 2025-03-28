package com.example.cpp_compiler_backend.cppcompiler.controller;

import org.springframework.beans.factory.annotation.Autowired;
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
    //main api to execute c++ code
    @PostMapping("/compile")
    public ResponseEntity<CompileResponse> batchCompileCode(@RequestBody CompileRequest request) {
        CompileResponse response = new CompileResponse();
        
        if (request.getCode() == null || request.getCode().trim().isEmpty()) {
            response.setSuccess(false);
            response.setMessage("Code cannot be empty");
            return ResponseEntity.badRequest().body(response);
        }
        
        try {
            System.out.println("Received batch compile request");
            
            CompilerService.CompileResult result = compilerService.compileCppCode(request.getCode());
            response.setSuccess(result.isSuccess());
            response.setOutput(result.getOutput());
            
            if (!result.isSuccess()) {
                response.setMessage("Compilation or execution failed");
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error in batch compile: " + e.getMessage());
            e.printStackTrace();
            response.setSuccess(false);
            response.setMessage("Server error: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    @GetMapping("/batch-test")
    public ResponseEntity<CompileResponse> batchTest() {
        CompileResponse response = new CompileResponse();
        
        try {
            // Absolute minimal test
            String code = "int main() { return 0; }";
            
            CompilerService.CompileResult result = compilerService.compileCppCode(code);
            response.setSuccess(result.isSuccess());
            response.setOutput(result.getOutput());
            
            if (!result.isSuccess()) {
                response.setMessage("Batch test failed");
            } else {
                response.setMessage("Batch test succeeded");
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.setSuccess(false);
            response.setMessage("Server error: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }
    
    @GetMapping("/batch-hello")
    public ResponseEntity<CompileResponse> batchHello() {
        CompileResponse response = new CompileResponse();
        
        try {
            // Simple hello world
            String code = "#include <iostream>\n\nint main() {\n    std::cout << \"Hello from batch!\" << std::endl;\n    return 0;\n}";
            
            CompilerService.CompileResult result = compilerService.compileCppCode(code);
            response.setSuccess(result.isSuccess());
            response.setOutput(result.getOutput());
            
            if (!result.isSuccess()) {
                response.setMessage("Batch hello failed");
            } else {
                response.setMessage("Batch hello succeeded");
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.setSuccess(false);
            response.setMessage("Server error: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }
}