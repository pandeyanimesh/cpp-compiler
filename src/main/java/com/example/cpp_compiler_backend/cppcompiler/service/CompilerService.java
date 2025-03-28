package com.example.cpp_compiler_backend.cppcompiler.service;

import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class CompilerService {

    // Use Documents folder for better permissions
    private final String COMPILER_DIR = System.getProperty("user.home") + File.separator + "Documents" + File.separator + "cpp-compiler";
    
    // Try all available compilers
    private final String[] GCC_PATHS = {
        "C:\\MinGW\\bin\\g++.exe",
        "C:\\msys64\\ucrt64\\bin\\g++.exe",
        "C:\\Program Files (x86)\\cpeditor\\mingw64\\bin\\g++.exe",
        "C:\\Program Files\\mingw-w64\\x86_64-8.1.0-posix-seh-rt_v6-rev0\\mingw64\\bin\\g++.exe"
    };

    public CompilerService() {
        // Create the compiler directory if it doesn't exist
        try {
            File dir = new File(COMPILER_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            System.out.println("Using compiler directory: " + COMPILER_DIR);
            
            // Find a working compiler
            for (String gccPath : GCC_PATHS) {
                File gccFile = new File(gccPath);
                if (gccFile.exists()) {
                    System.out.println("Found working compiler: " + gccPath);
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to create compiler directory: " + e.getMessage());
        }
    }

    // Overloaded method for backward compatibility
    public CompileResult compileCppCode(String code) {
        return compileCppCode(code, "");
    }

    public CompileResult compileCppCode(String code, String input) {
        CompileResult result = new CompileResult();
        String uniqueId = UUID.randomUUID().toString();
        String sourceFilePath = COMPILER_DIR + File.separator + uniqueId + ".cpp";
        String executableFilePath = COMPILER_DIR + File.separator + uniqueId + ".exe";
        String batchFilePath = COMPILER_DIR + File.separator + "compile_" + uniqueId + ".bat";
        String outputFilePath = COMPILER_DIR + File.separator + uniqueId + "_output.txt";
        String inputFilePath = COMPILER_DIR + File.separator + uniqueId + "_input.txt";
        
        try {
            // Write the code to a file
            Files.write(Path.of(sourceFilePath), code.getBytes());
            
            // Write input to a file if provided
            boolean hasInput = input != null && !input.trim().isEmpty();
            if (hasInput) {
                Files.write(Path.of(inputFilePath), input.getBytes());
                System.out.println("Created input file: " + inputFilePath);
            }
            
            // Find a working compiler
            String gccPath = null;
            for (String path : GCC_PATHS) {
                if (new File(path).exists()) {
                    gccPath = path;
                    break;
                }
            }
            
            if (gccPath == null) {
                result.setSuccess(false);
                result.setOutput("No working C++ compiler found. Please install MinGW or MSYS2.");
                return result;
            }
            
            // Create a batch file for compilation and execution
            StringBuilder batchContent = new StringBuilder();
            batchContent.append("@echo off\r\n");
            batchContent.append("cd \"").append(COMPILER_DIR).append("\"\r\n");
            
            // Set PATH based on the compiler we're using
            if (gccPath.contains("msys64")) {
                batchContent.append("set PATH=C:\\msys64\\ucrt64\\bin;%PATH%\r\n");
            } else if (gccPath.contains("MinGW")) {
                batchContent.append("set PATH=C:\\MinGW\\bin;%PATH%\r\n");
            } else if (gccPath.contains("cpeditor")) {
                batchContent.append("set PATH=C:\\Program Files (x86)\\cpeditor\\mingw64\\bin;%PATH%\r\n");
            } else if (gccPath.contains("mingw-w64")) {
                batchContent.append("set PATH=C:\\Program Files\\mingw-w64\\x86_64-8.1.0-posix-seh-rt_v6-rev0\\mingw64\\bin;%PATH%\r\n");
            }
            
            // Compile step
            batchContent.append("echo Compiling...\r\n");
            batchContent.append("\"").append(gccPath).append("\" \"")
                      .append(sourceFilePath).append("\" -o \"")
                      .append(executableFilePath).append("\"\r\n");
            
            // Check if compilation succeeded
            batchContent.append("if %ERRORLEVEL% NEQ 0 (\r\n");
            batchContent.append("  echo Compilation failed with exit code %ERRORLEVEL%\r\n");
            batchContent.append("  exit /b %ERRORLEVEL%\r\n");
            batchContent.append(")\r\n");
            
            // Run step - with or without input redirection
            batchContent.append("echo Running program...\r\n");
            
            if (hasInput) {
                // With input redirection
                batchContent.append("\"").append(executableFilePath).append("\" < \"")
                          .append(inputFilePath).append("\" > \"")
                          .append(outputFilePath).append("\" 2>&1\r\n");
            } else {
                // Without input redirection
                batchContent.append("\"").append(executableFilePath).append("\" > \"")
                          .append(outputFilePath).append("\" 2>&1\r\n");
            }
            
            // Save execution exit code
            batchContent.append("set EXIT_CODE=%ERRORLEVEL%\r\n");
            batchContent.append("echo Program exited with code %EXIT_CODE%\r\n");
            
            // Write the exit code to a file
            String exitCodeFilePath = COMPILER_DIR + File.separator + uniqueId + "_exit.txt";
            batchContent.append("echo %EXIT_CODE% > \"").append(exitCodeFilePath).append("\"\r\n");
            batchContent.append("exit /b %EXIT_CODE%\r\n");
            
            // Write batch file
            Files.write(Path.of(batchFilePath), batchContent.toString().getBytes());
            
            // Execute the batch file
            ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", batchFilePath);
            pb.directory(new File(COMPILER_DIR));
            
            Process process = pb.start();
            
            // Capture the batch file's output
            StringBuilder processOutput = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    processOutput.append(line).append("\n");
                }
            }
            
            // Capture the batch file's error output
            StringBuilder processError = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    processError.append(line).append("\n");
                }
            }
            
            // Wait for batch file to complete
            boolean completed = process.waitFor(30, TimeUnit.SECONDS);
            if (!completed) {
                process.destroyForcibly();
                result.setSuccess(false);
                result.setOutput("Process timed out");
                return result;
            }
            
            int exitCode = process.exitValue();
            
            // Check for user program output
            String programOutput = "";
            if (Files.exists(Path.of(outputFilePath))) {
                programOutput = Files.readString(Path.of(outputFilePath));
            }
            
            // Check for user program exit code
            int programExitCode = exitCode;
            if (Files.exists(Path.of(exitCodeFilePath))) {
                String exitCodeStr = Files.readString(Path.of(exitCodeFilePath)).trim();
                try {
                    programExitCode = Integer.parseInt(exitCodeStr);
                } catch (NumberFormatException e) {
                    // Fall back to batch exit code
                }
            }
            
            if (exitCode != 0) {
                // Compilation or execution failed
                result.setSuccess(false);
                
                if (!Files.exists(Path.of(executableFilePath))) {
                    // Compilation failed
                    result.setOutput("Compilation failed:\n" + processOutput.toString() + processError.toString());
                } else {
                    // Execution failed
                    if (programExitCode == -1073741819 || programExitCode == -1073741571) {
                        // Access violation or stack overflow
                        result.setOutput("Program crashed during execution (Access Violation or Stack Overflow).\n" +
                                        "Program output before crash: " + programOutput);
                    } else {
                        result.setOutput("Program exited with an error (exit code: " + programExitCode + ").\n" +
                                        "Output: " + programOutput);
                    }
                }
            } else {
                // Success!
                result.setSuccess(true);
                result.setOutput(programOutput);
            }
            
        } catch (Exception e) {
            result.setSuccess(false);
            result.setOutput("Error: " + e.getMessage() + "\n" + e.toString());
            e.printStackTrace();
        } finally {
            // Clean up files
            try {
                Files.deleteIfExists(Path.of(sourceFilePath));
                Files.deleteIfExists(Path.of(executableFilePath));
                Files.deleteIfExists(Path.of(batchFilePath));
                Files.deleteIfExists(Path.of(outputFilePath));
                if (input != null && !input.isEmpty()) {
                    Files.deleteIfExists(Path.of(inputFilePath));
                }
            } catch (Exception e) {
                System.err.println("Failed to clean up files: " + e.getMessage());
            }
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