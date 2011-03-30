package com.slowfrog.qwop;

public class CrashedFilter implements IFilter<RunInfo> {

  @Override
  public boolean matches(RunInfo t) {
    return t.crashed;
  }

  
}
