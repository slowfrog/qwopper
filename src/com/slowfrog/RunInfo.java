package com.slowfrog;

public class RunInfo {

  public String string;
  public boolean crashed;
  public boolean stopped;
  public long duration;
  public float distance;

  
  
  public RunInfo(String pstring, boolean pcrashed, boolean pstopped,
      long pduration, float pdistance) {
    this.string = pstring;
    this.crashed = pcrashed;
    this.stopped = pstopped;
    this.duration = pduration;
    this.distance = pdistance;
  }

  public String toString() {
    return ("Ran " + distance + "m during " + duration + "ms") +
           (this.crashed ? " and crashed"
                        : this.stopped ? " and was stopped"
                                      : this.distance > 100 ? " and won" : "");
  }
}
