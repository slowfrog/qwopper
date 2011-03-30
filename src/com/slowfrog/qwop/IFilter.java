package com.slowfrog.qwop;

public interface IFilter<T> {
  boolean matches(T t);
}
