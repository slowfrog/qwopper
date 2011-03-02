package com.slowfrog.qwop.ui;

import java.awt.Robot;

import com.slowfrog.qwop.ConsoleLog;
import com.slowfrog.qwop.Log;
import com.slowfrog.qwop.Qwopper;
import com.slowfrog.qwop.RunInfo;

public class Main {

  private static final Log LOG = new ConsoleLog();

  public static void main(String[] args) {
    try {
      Robot rob = new Robot();
      Qwopper qwop = new Qwopper(rob, LOG);
      qwop.findRealOrigin();
      testString(qwop,
          args.length > 0 ? args[0] : Qwopper.makeRealisticRandomString(50), 10);

    } catch (Throwable t) {
      LOG.log("Error", t);
    }
  }

  private static void testString(Qwopper qwop, String str, int count) {
    for (int i = 0; i < 10; ++i) {
      LOG.logf("Run #%d\n", i);
      qwop.startGame();
      RunInfo info = qwop.playOneGame(str, 60000);
      LOG.log(info.toString());
      LOG.log(info.marshal());
    }
  }

}
