package com.slowfrog.qwop.ui;

import java.awt.Robot;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

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
          args.length > 0 ? args[0] : Qwopper.makeRealisticRandomString(30), 10);

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
      saveRunInfo("runs.txt", info);
    }
  }

  private static void saveRunInfo(String filename, RunInfo info) {
    try {
      PrintStream out = new PrintStream(new FileOutputStream(filename, true));
      try {
        out.println(info.marshal());
      } finally {
        out.flush();
        out.close();
      }
    } catch (IOException ioe) {
      LOG.log("Error marshalling", ioe);
    }
  }

}
