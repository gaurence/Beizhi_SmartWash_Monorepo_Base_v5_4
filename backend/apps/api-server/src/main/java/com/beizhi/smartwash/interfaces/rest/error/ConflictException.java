package com.beizhi.smartwash.interfaces.rest.error;

public class ConflictException extends RuntimeException {
  public ConflictException(String message) {
    super(message);
  }
}
