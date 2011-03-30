package com.slowfrog.qwop;

public class MinRatioFilter implements IFilter<Individual> {

  private float minRatio;

  private IFilter<RunInfo> filter;

  public MinRatioFilter(IFilter<RunInfo> filter) {
    this(filter, 0.5f);
  }
  
  public MinRatioFilter(IFilter<RunInfo> filter, float minRatio) {
    this.minRatio = minRatio;
    this.filter = filter;
  }

  @Override
  public boolean matches(Individual individual) {
    if ((individual.runs.size() == 0) && (minRatio > 0)) {
      return false;
    }
    int matchingRuns = 0;
    for (RunInfo run : individual.runs) {
      if (this.filter.matches(run)) {
        ++matchingRuns;
      }
    }
    return (((float) matchingRuns) / individual.runs.size()) >= this.minRatio;
  }

}
