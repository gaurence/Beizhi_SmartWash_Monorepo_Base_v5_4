package com.beizhi.smartwash.interfaces.rest;

import com.beizhi.smartwash.application.service.AuthService;
import com.beizhi.smartwash.common.ApiResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class AuthController {
  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/auth/login")
  public ApiResponse login(@RequestBody(required = false) Map<String, Object> body) {
    return ApiResponse.ok(authService.login(body == null ? Map.of() : body));
  }

  @PostMapping("/auth/mfa/verify")
  public ApiResponse verifyMfa(@RequestBody(required = false) Map<String, Object> body) {
    return ApiResponse.ok(authService.verifyMfa(body == null ? Map.of() : body));
  }

  @PostMapping("/auth/logout")
  public ApiResponse logout() {
    return ApiResponse.ok(authService.logout());
  }
}
