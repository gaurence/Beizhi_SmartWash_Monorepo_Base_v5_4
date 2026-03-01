package com.beizhi.smartwash.common;

public record ApiResponse(boolean success, Object data, ApiError error) {
  public static ApiResponse ok(Object data) {
    return new ApiResponse(true, data, null);
  }

  public static ApiResponse fail(String code, String message) {
    return new ApiResponse(false, null, new ApiError(code, message, null));
  }

  public static ApiResponse fail(String code, String message, Object details) {
    return new ApiResponse(false, null, new ApiError(code, message, details));
  }

  public record ApiError(String code, String message, Object details) {}
}
