package com.example.cpp_compiler_backend.cppcompiler.service;

import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
public class CompilerService {

    private final String TEMP_DIR = System.getProperty("java.io.tmpdir");

    public CompileResult compileCppCode(String code) {
        CompileResult result = new CompileResult();
        String uniqueId = UUID.randomUUID().toString();
        String sourceFilePath = TEMP_DIR + File.separator + uniqueId + ".cpp";
        String executableFilePath = TEMP_DIR + File.separator + uniqueId + ".exe";
        File sourceFile = new File(sourceFilePath);

        try {
            // Write the code to a temporary file
            Files.write(Path.of(sourceFilePath), code.getBytes());

            // Compile the code
            ProcessBuilder compileProcessBuilder = new ProcessBuilder();
            
            // For Windows
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                compileProcessBuilder.command("g++", sourceFilePath, "-o", executableFilePath);
            } else {
                // For Linux/Mac
                compileProcessBuilder.command("g++", sourceFilePath, "-o", executableFilePath);
            }
            
            Process compileProcess = compileProcessBuilder.start();
            StringBuilder compileErrors = new StringBuilder();
            
            try (BufferedReader errorReader = new BufferedReader(
                    new InputStreamReader(compileProcess.getErrorStream()))) {
                String line;
                while ((line = errorReader.readLine()) != null) {
                    compileErrors.append(line).append("\n");
                }
            }
            
            int compileExitCode = compileProcess.waitFor();
            
            if (compileExitCode != 0) {
                result.setSuccess(false);
                result.setOutput(compileErrors.toString());
                return result;
            }
            
            // Execute the compiled program
            ProcessBuilder executeProcessBuilder = new ProcessBuilder();
            
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                executeProcessBuilder.command(executableFilePath);
            } else {
                executeProcessBuilder.command("./" + executableFilePath);
            }
            
            Process executeProcess = executeProcessBuilder.start();
            
            StringBuilder output = new StringBuilder();
            StringBuilder errors = new StringBuilder();
            
            try (BufferedReader stdoutReader = new BufferedReader(
                    new InputStreamReader(executeProcess.getInputStream()))) {
                String line;
                while ((line = stdoutReader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }
            
            try (BufferedReader stderrReader = new BufferedReader(
                    new InputStreamReader(executeProcess.getErrorStream()))) {
                String line;
                while ((line = stderrReader.readLine()) != null) {
                    errors.append(line).append("\n");
                }
            }
            
            int executeExitCode = executeProcess.waitFor();
            
            result.setSuccess(executeExitCode == 0);
            result.setOutput(output.toString());
            
            if (executeExitCode != 0) {
                result.setOutput(result.getOutput() + "\nErrors: " + errors.toString());
            }
            
        } catch (Exception e) {
            result.setSuccess(false);
            result.setOutput("Error: " + e.getMessage());
        } finally {
            // Clean up temporary files
            new File(sourceFilePath).delete();
            new File(executableFilePath).delete();
        }
        
        return result;
    }
    
    public static class CompileResult {
        private boolean success;
        private String output;
        
        public boolean isSuccess() {
            return success;
        }
        
        public void setSuccess(boolean success) {
            this.success = success;
        }
        
        public String getOutput() {
            return output;
        }
        
        public void setOutput(String output) {
            this.output = output;
        }
    }
}