package com.beizhi.smartwash.common;

import java.util.List;

public record PageResponse(boolean success, List<?> data, int page, int size, long total) {
  public static PageResponse of(List<?> data, int page, int size, long total) {
    return new PageResponse(true, data, page, size, total);
  }
}
