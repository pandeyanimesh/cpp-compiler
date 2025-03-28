package com.example.cpp_compiler_backend.cppcompiler.model;

public class CompileRequest {
    private String code;
    private String input;

    // Default constructor required for Jackson
    public CompileRequest() {
    }

    public CompileRequest(String code, String input) {
        this.code = code;
        this.input = input;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
    
    public String getInput() {
        return input;
    }
    
    public void setInput(String input) {
        this.input = input;
    }

    @Override
    public String toString() {
        return "CompileRequest{" +
                "code='" + code + '\'' +
                ", input='" + (input != null ? input : "") + '\'' +
                '}';
    }
}