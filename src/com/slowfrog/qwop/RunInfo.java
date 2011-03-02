package com.slowfrog.qwop;

public class RunInfo {

  private static final long serialVersionUID = 1L;

  public String string;
  public int delay;
  public boolean crashed;
  public boolean stopped;
  public long duration;
  public float distance;

  public RunInfo(String pstring, int pdelay, boolean pcrashed,
      boolean pstopped, long pduration, float pdistance) {
    this.string = pstring;
    this.delay = pdelay;
    this.crashed = pcrashed;
    this.stopped = pstopped;
    this.duration = pduration;
    this.distance = pdistance;
  }

  public String getResultCode() {
    return (this.crashed ? "C" : this.stopped ? "S" : this.distance > 100 ? "W"
                                                                         : "?");
  }

  public String marshal() {
    return "RunInfo#" + serialVersionUID + "|" + this.string + "|" +
           this.delay + "|" + this.distance + "|" + this.duration + "|" +
           this.getResultCode();
  }

  public String toString() {
    return ("Ran " + distance + "m during " + duration + "ms") +
           (this.crashed ? " and crashed"
                        : this.stopped ? " and was stopped"
                                      : this.distance > 100 ? " and won" : "");
  }
}
