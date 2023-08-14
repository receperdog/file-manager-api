package com.demo.filemanager.service;

import com.demo.filemanager.dao.request.SignUpRequest;
import com.demo.filemanager.dao.request.SigninRequest;
import com.demo.filemanager.dao.response.JwtAuthenticationResponse;

public interface AuthenticationService {
    JwtAuthenticationResponse signup(SignUpRequest request);

    JwtAuthenticationResponse signin(SigninRequest request);
}