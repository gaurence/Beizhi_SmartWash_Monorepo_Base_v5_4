package com.beizhi.smartwash.interfaces.rest.error;

public class ForbiddenException extends RuntimeException {
  public ForbiddenException(String message) {
    super(message);
  }
}
