package com.slowfrog.qwop;

public class NotFilter<T> implements IFilter<T> {

  private IFilter<T> filter;

  public NotFilter(IFilter<T> filter) {
    this.filter = filter;
  }

  @Override
  public boolean matches(T t) {
    return !filter.matches(t);
  }

}
