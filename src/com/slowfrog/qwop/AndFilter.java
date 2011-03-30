package com.slowfrog.qwop;

public class AndFilter<T> implements IFilter<T> {

  private IFilter<T> filter1;
  private IFilter<T> filter2;

  public AndFilter(IFilter<T> filter1, IFilter<T> filter2) {
    this.filter1 = filter1;
    this.filter2 = filter2;
  }

  @Override
  public boolean matches(T t) {
    return filter1.matches(t) && filter2.matches(t);
  }

}
