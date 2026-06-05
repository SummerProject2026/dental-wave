package com.summerproject2026.DentalWave.controller;

import com.summerproject2026.DentalWave.dto.JwtAuthResponse;
import com.summerproject2026.DentalWave.dto.LoginDto;
import com.summerproject2026.DentalWave.dto.RegisterDto;
import com.summerproject2026.DentalWave.dto.UserDto;
import com.summerproject2026.DentalWave.service.AuthService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Handles all authentication related HTTP requests (login and register)
@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthController {

    // Service that contains the authentication business logic
    private AuthService authService;

    // POST / api/auth/login - acceptes credentials and retruns a JWT token
    @PsotMapping("/login")
    public ResponseEntity<JWTAuthResponse>login(@RequestBody LoginDTo loginDto){

        //Delegate login logic to the auth service
        JWTAuthResponse jwtAuthResponse = authService.login(loginDto);

        //Return 200 OK with the JWT token  in the response body
        return ResponseEntity.ok(jwtAuthResponse);

    }
    //POST/ api/auth/ register -  accepts user info and  creates a new account
    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@RequestBody RegisterDto registerDto){

        //Delegate registeration logic to the auth service
        UserDto savedUser = authService.register(registerDto);

        //Retrun 201 Created with the new user in the rsponse body
        retrun new ResponseEn
    }








}