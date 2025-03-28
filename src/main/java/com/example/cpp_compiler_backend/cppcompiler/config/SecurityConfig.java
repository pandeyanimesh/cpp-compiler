package com.example.cpp_compiler_backend.cppcompiler.config;
// package com.example.cppcompiler.config;

// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.security.config.annotation.web.builders.HttpSecurity;
// import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
// import org.springframework.security.web.SecurityFilterChain;

// @Configuration
// @EnableWebSecurity
// public class SecurityConfig {

//     @Bean
//     public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//         http
//             // Disable CSRF protection
//             .csrf().disable()
//             // Configure authorization
//             .authorizeRequests()
//                 // Allow all requests
//                 .anyRequest().permitAll()
//             .and()
//             // Explicitly disable HTTP Basic
//             .httpBasic().disable()
//             // Disable form login
//             .formLogin().disable();
        
//         return http.build();
//     }
// }