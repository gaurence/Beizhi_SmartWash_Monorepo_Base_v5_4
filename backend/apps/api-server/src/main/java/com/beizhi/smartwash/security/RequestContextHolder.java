package com.beizhi.smartwash.security;

public final class RequestContextHolder {
  private static final ThreadLocal<RequestContext> HOLDER = new ThreadLocal<>();

  private RequestContextHolder() {}

  public static void set(RequestContext context) {
    HOLDER.set(context);
  }

  public static RequestContext get() {
    RequestContext context = HOLDER.get();
    if (context == null) {
      throw new IllegalStateException("RequestContext is not set");
    }
    return context;
  }

  public static void clear() {
    HOLDER.remove();
  }
}
