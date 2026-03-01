package com.beizhi.smartwash.interfaces.rest.error;

public class NotFoundException extends RuntimeException {
  public NotFoundException(String message) {
    super(message);
  }
}
