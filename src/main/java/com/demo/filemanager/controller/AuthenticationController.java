package com.demo.filemanager.controller;

import com.demo.filemanager.dao.request.SignUpRequest;
import com.demo.filemanager.dao.request.SigninRequest;
import com.demo.filemanager.dao.response.JwtAuthenticationResponse;
import com.demo.filemanager.service.AuthenticationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authenticationService;
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationController.class);
    @PostMapping("/signup")
    public ResponseEntity<JwtAuthenticationResponse> signup(@RequestBody SignUpRequest request) {
        logger.info("Signed up");
        return ResponseEntity.ok(authenticationService.signup(request));
    }

    @PostMapping("/signing")
    public ResponseEntity<JwtAuthenticationResponse> signin(@RequestBody SigninRequest request) {
        logger.info("Signing");

        return ResponseEntity.ok(authenticationService.signin(request));
    }
}
