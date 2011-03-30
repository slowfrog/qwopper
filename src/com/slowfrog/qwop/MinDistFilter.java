package com.slowfrog.qwop;

public class MinDistFilter implements IFilter<RunInfo> {

  private int minDist;

  public MinDistFilter(int minDist) {
    this.minDist = minDist;
  }

  @Override
  public boolean matches(RunInfo run) {
    return (run.distance >= this.minDist);
  }

}
