package com.beizhi.smartwash;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthController {
  @GetMapping("/healthz")
  public Map<String, Object> healthz() {
    return Map.of("ok", true);
  }
}
