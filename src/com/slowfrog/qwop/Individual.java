package com.slowfrog.qwop;

import java.util.ArrayList;
import java.util.List;

public class Individual {

  public String str;
  public List<RunInfo> runs;

  public Individual(String pstr, List<RunInfo> pruns) {
    this.str = pstr;
    if (pruns != null) {
      this.runs = new ArrayList<RunInfo>(pruns);
    } else {
      this.runs = new ArrayList<RunInfo>();
    }
  }
}
