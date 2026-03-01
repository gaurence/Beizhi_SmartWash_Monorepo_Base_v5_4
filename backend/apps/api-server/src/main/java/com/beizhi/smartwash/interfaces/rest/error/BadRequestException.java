package com.beizhi.smartwash.interfaces.rest.error;

public class BadRequestException extends RuntimeException {
  public BadRequestException(String message) {
    super(message);
  }
}
