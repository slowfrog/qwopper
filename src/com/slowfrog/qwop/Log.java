package com.slowfrog.qwop;

public interface Log {
  public void log(String message);

  public void log(String message, Throwable e);

  public void logf(String format, Object... args);
}
