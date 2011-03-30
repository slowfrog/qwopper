package com.slowfrog.qwop;

public class MinDistFilter implements IFilter {

  private int minDist;
  
  private float runRatio;
  
  public MinDistFilter(int minDist, float runRatio) {
    this.minDist = minDist;
    this.runRatio = runRatio;
  }
  
  public MinDistFilter(int minDist) {
    this(minDist, 0.5f);
  }
  
  @Override
  public boolean matches(Individual individual) {
    if (individual.runs.size() == 0) {
      return false;
    }
    int matchingRuns = 0;
    for (RunInfo run : individual.runs) {
      if (run.distance >= this.minDist) {
        ++matchingRuns;
      }
    }
    return (((float) matchingRuns) / individual.runs.size()) >= this.runRatio;
  }

}
