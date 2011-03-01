package com.slowfrog.qwop.ui;

import java.awt.Robot;

import com.slowfrog.qwop.ConsoleLog;
import com.slowfrog.qwop.Qwopper;
import com.slowfrog.qwop.RunInfo;

public class Main {

  public static void main(String[] args) {
    ConsoleLog log = new ConsoleLog();
    try {
      Robot rob = new Robot();
      Qwopper qwop = new Qwopper(rob, log);
      qwop.findRealOrigin();
      String str = args.length > 0 ? args[0] : Qwopper
          .makeRealisticRandomString(50);
      qwop.startGame();
      RunInfo info = qwop.playOneGame(str, 60000);

    } catch (Throwable t) {
      log.log("Error", t);
    }
  }

}
