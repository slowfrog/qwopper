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
  
  public static RunInfo unmarshal(String line) {
    String[] parts = line.split("\\|");
    if (parts[0].equals("RunInfo#1")) {
      String string = parts[1];
      int delay = Integer.parseInt(parts[2]);
      float distance = Float.parseFloat(parts[3]);
      long duration = Long.parseLong(parts[4]);
      String resultCode = parts[5];
      boolean crashed = resultCode.equals("C");
      boolean stopped = resultCode.equals("S");
      return new RunInfo(string, delay, crashed, stopped, duration, distance);
      
    } else {
      throw new RuntimeException("Unknown format: " + parts[0]);
    }
  }

  public String toString() {
    return ("Ran " + distance + "m during " + duration + "ms") +
           (this.crashed ? " and crashed"
                        : this.stopped ? " and was stopped"
                                      : this.distance > 100 ? " and won" : "");
  }
}
