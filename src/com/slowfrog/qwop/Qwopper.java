/*
 * Copyright SlowFrog 2011
 *
 * License granted to anyone for any kind of purpose as long as you don't sue me.
 */
package com.slowfrog.qwop;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Random;

/**
 * This class will try to play QWOP and evolve some way to play well...
 * hopefully. Game at {@link http://foddy.net/Athletics.html}
 * 
 * @author SlowFrog
 */
public class Qwopper {

  /** Tolerance for color comparison. */
  private static final int RGB_TOLERANCE = 3;

  /** Unit delay in milliseconds when playing a 'string' */
  private static final int DELAY = 150;

  /** Interval between two speed checks. */
  private static final int CHECK_INTERVAL = 1000;

  /** All possible 'notes' */
  private static final String NOTES = "QWOPqwop++";

  /**
   * Number of consecutive runs before we trigger a reload of the browser to
   * keep CPU and memory usage reasonable.
   */
  private static final int MAX_RUNS_BETWEEN_RELOAD = 10;

  /** Distance between two colors. */
  private static int colorDistance(int rgb1, int rgb2) {
    int dr = Math.abs(((rgb1 & 0xff0000) >> 16) - ((rgb2 & 0xff0000) >> 16));
    int dg = Math.abs(((rgb1 & 0xff00) >> 8) - ((rgb2 & 0xff00) >> 8));
    int db = Math.abs((rgb1 & 0xff) - (rgb2 & 0xff));
    return dr + dg + db;
  }

  /** Checks if a color matches another within a given tolerance. */
  private static boolean colorMatches(int ref, int other) {
    return colorDistance(ref, other) < RGB_TOLERANCE;
  }

  /**
   * Checks if from a given x,y position we can find the pattern that identifies
   * the blue border of the message box.
   */
  private static boolean matchesBlueBorder(BufferedImage img, int x, int y) {
    int refColor = 0x9dbcd0;
    return ((y > 4) && (y < img.getHeight() - 4) && (x < img.getWidth() - 12) &&
            colorMatches(img.getRGB(x, y), refColor) &&
            colorMatches(img.getRGB(x + 4, y), refColor) &&
            colorMatches(img.getRGB(x + 8, y), refColor) &&
            colorMatches(img.getRGB(x + 12, y), refColor) &&
            colorMatches(img.getRGB(x, y + 4), refColor) &&
            !colorMatches(img.getRGB(x, y - 4), refColor) && !colorMatches(
        img.getRGB(x + 4, y + 4), refColor));
  }

  /**
   * From a position that matches the blue border, slide left and top until the
   * corner is found.
   */
  private static int[] slideTopLeft(BufferedImage img, int x, int y) {
    int ax = x;
    int ay = y;

    OUTER_LOOP:

    while (ax >= 0) {
      --ax;
      if (matchesBlueBorder(img, ax, ay)) {
        continue;
      } else {
        ++ax;
        while (ay >= 0) {
          --ay;
          if (matchesBlueBorder(img, ax, ay)) {
            continue;
          } else {
            ++ay;
            break OUTER_LOOP;
          }
        }
      }
    }
    return new int[] { ax, ay };
  }

  /**
   * Move the mouse cursor to a given screen position and click with the left
   * mouse button.
   */
  private static void clickAt(Robot rob, int x, int y) {
    rob.mouseMove(x, y);
    rob.mousePress(InputEvent.BUTTON1_MASK);
    rob.mouseRelease(InputEvent.BUTTON1_MASK);
  }

  /**
   * Simulates a key 'click' by sending a key press followed by a key release
   * event.
   */
  private static void clickKey(Robot rob, int keycode) {
    rob.keyPress(keycode);
    rob.keyRelease(keycode);
  }

  /** Wait for a few milliseconds, without fear of an InterruptedException. */
  private static void doWait(int millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      // Don't mind
    }
  }

  /**
   * Play a string. Interpret a string of QWOPqwop+ as a music sheet.
   * <ul>
   * <li>QWOP means press the key Q, W, O or P</li>
   * <li>qwop means release the key</li>
   * <li>+ means wait for a small delay</li>
   * </ul>
   */
  private void playString(String str) {
    this.string = str;
    long lastTick = System.currentTimeMillis();
    for (int i = 0; i < str.length(); ++i) {
      if (stop) {
        return;
      }
      char c = str.charAt(i);
      switch (c) {
      case 'Q':
        rob.keyPress(KeyEvent.VK_Q);
        break;

      case 'W':
        rob.keyPress(KeyEvent.VK_W);
        break;

      case 'O':
        rob.keyPress(KeyEvent.VK_O);
        break;

      case 'P':
        rob.keyPress(KeyEvent.VK_P);
        break;

      case 'q':
        rob.keyRelease(KeyEvent.VK_Q);
        break;

      case 'w':
        rob.keyRelease(KeyEvent.VK_W);
        break;

      case 'o':
        rob.keyRelease(KeyEvent.VK_O);
        break;

      case 'p':
        rob.keyRelease(KeyEvent.VK_P);
        break;

      case '+':
        if (System.currentTimeMillis() > this.nextCheck) {
          checkSpeed();
        }

        int waitTime = (int) ((lastTick + delay) - System.currentTimeMillis());
        if (waitTime > 0) {
          doWait(waitTime);
        }
        long newTick = System.currentTimeMillis();
        // log.logf("w=%03d d=%03d\n", waitTime, newTick - lastTick);
        lastTick = newTick;
        if ((this.timeLimit != 0) && (newTick > this.timeLimit)) {
          this.stop = true;
          return;
        }
        // After each delay, check the screen to see if it's finished
        if (isFinished()) {
          return;
        }
        break;

      default:
        System.out.println("Unkown 'note': " + c);
      }
    }
  }

  private void checkSpeed() {
    this.nextCheck += CHECK_INTERVAL;
    String distStr = captureDistance();
    float dist = Float.parseFloat(distStr);
    long dur = System.currentTimeMillis() - this.start;
    if (dur == 0) {
      dur = 1;
    }
    float speed = (dist * 1000) / dur;
    log.logf("%.1fm in %ds: speed=%.3f\n", dist, (dur / 1000), speed);
  }

  private static int keyIndex(char key) {
    switch (Character.toLowerCase(key)) {
    case 'q':
      return 0;
    case 'w':
      return 1;
    case 'o':
      return 2;
    case 'p':
      return 3;
    default:
      throw new IllegalArgumentException("Invalid key: " + key);
    }
  }

  private static String indexKey(int index) {
    return "qwop".substring(index, index + 1);
  }

  /**
   * A realistic random string is one where:
   * <ul>
   * <li>a key press is always followed by a key release for the same key</li>
   * <li>there is some time between a press and a release of a key</li>
   * <li>there is also some time between a release and a press of a key.</li>
   * </ul>
   * 
   * @param duration
   *          duration of the sequence in 'ticks'
   */
  public static String makeRealisticRandomString(int duration) {
    Random random = new Random(System.currentTimeMillis());
    String str = "";
    boolean[] down = { false, false, false, false };
    boolean[] justDown = { false, false, false, false };
    boolean[] justUp = { false, false, false, false };
    int cur = 0;
    while (cur < duration) {
      int rnd = random.nextInt(NOTES.length());
      String k = NOTES.substring(rnd, rnd + 1);
      char kc = k.charAt(0);
      if (kc == '+') { // delay
        ++cur;
        for (int i = 0; i < 4; ++i) {
          justDown[i] = false;
          justUp[i] = false;
        }
      } else if (Character.isUpperCase(kc)) { // key press
        int ki = keyIndex(kc);
        if (!(down[ki] || justUp[ki])) {
          down[ki] = true;
          justDown[ki] = true;
        } else {
          continue;
        }

      } else { // Lower case: key release
        int ki = keyIndex(kc);
        if (down[ki] && !justDown[ki]) {
          down[ki] = false;
          justUp[ki] = true;
        } else {
          continue;
        }
      }

      str += kc;
    }

    // Make sure all keys are released at the end (maybe without a delay)
    for (int i = 0; i < down.length; ++i) {
      if (down[i]) {
        str += indexKey(i);
      }
    }
    return str;
  }

  private Robot rob;

  private int[] origin;

  private boolean finished;

  private Log log;

  private long start;

  private long nextCheck;

  private long timeLimit;

  private boolean stop;

  private String string;

  private int delay = DELAY;

  private int nbRuns;

  private BufferedImage capture;

  private BufferedImage transformed;

  public Qwopper(Robot rob, Log log) {
    this.rob = rob;
    this.log = log;
  }

  public int[] getOrigin() {
    return this.origin;
  }

  public String getString() {
    return this.string;
  }

  public BufferedImage getLastCapture() {
    return this.capture;
  }

  public BufferedImage getLastTransformed() {
    return this.transformed;
  }

  /** Look for the origin of the game area on screen. */
  private static int[] findOrigin(Robot rob) {
    Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
    BufferedImage shot = rob.createScreenCapture(new Rectangle(dim));
    for (int x = 0; x < dim.width; x += 4) {
      for (int y = 0; y < dim.height; y += 4) {
        if (matchesBlueBorder(shot, x, y)) {
          int[] corner = slideTopLeft(shot, x, y);
          return new int[] { corner[0] - 124, corner[1] - 103 };
        }
      }
    }
    throw new RuntimeException(
        "Origin not found. Make sure the game is open and fully visible.");
  }

  /** Checks if the game is finished by looking at the two yellow medals. */
  public boolean isFinished() {
    Color col1 = rob.getPixelColor(origin[0] + 157, origin[1] + 126);
    if (colorMatches(
        (col1.getRed() << 16) | (col1.getGreen() << 8) | col1.getBlue(),
        0xffff00)) {
      Color col2 = rob.getPixelColor(origin[0] + 482, origin[1] + 126);
      if (colorMatches(
          (col2.getRed() << 16) | (col2.getGreen() << 8) | col2.getBlue(),
          0xffff00)) {
        finished = true;
        return true;
      }

    }
    finished = false;
    return false;
  }

  public boolean isRunning() {
    return !(this.stop || this.finished);
  }

  /** Find the real origin of the game. */
  public int[] findRealOrigin() {
    origin = findOrigin(rob);
    if (isFinished()) {
      origin = new int[] { origin[0] - 5, origin[1] + 4 };
    }

    return origin;
  }

  /**
   * Start a game, either by clicking on it (at first load) or pressing space
   * for next games.
   */
  public void startGame() {
    stop = false;
    clickAt(rob, origin[0], origin[1]);
    if (isFinished()) {
      clickKey(rob, KeyEvent.VK_SPACE);
    } else {
      // Press 'R' for restart
      rob.keyPress(KeyEvent.VK_R);
      rob.keyRelease(KeyEvent.VK_R);
    }
  }

  public void stop() {
    this.stop = true;
  }

  private void stopRunning() {
    Point before = MouseInfo.getPointerInfo().getLocation();

    // Restore focus to QWOP (after a button click on QwopControl)
    clickAt(rob, origin[0], origin[1]);
    // Make sure all possible keys are released
    rob.keyPress(KeyEvent.VK_Q);
    rob.keyPress(KeyEvent.VK_W);
    rob.keyPress(KeyEvent.VK_O);
    rob.keyPress(KeyEvent.VK_P);
    doWait(20);
    rob.keyRelease(KeyEvent.VK_Q);
    rob.keyRelease(KeyEvent.VK_W);
    rob.keyRelease(KeyEvent.VK_O);
    rob.keyRelease(KeyEvent.VK_P);

    // Return the mouse cursor to its initial position...
    rob.mouseMove(before.x, before.y);
  }

  public void refreshBrowser() {
    // Click out of the flash rectangle to give focus to the browser
    clickAt(rob, origin[0], origin[1]);
    
    // Reload (F5)
    rob.keyPress(KeyEvent.VK_F5);
    doWait(20);
    rob.keyRelease(KeyEvent.VK_F5);

    // Wait some time and try to find the window again
    for (int i = 0; i < 10; ++i) {
      doWait(2000);
      try {
        this.findRealOrigin();
        return;
      } catch (RuntimeException e) {
        // Probably not available yet
      }
    }
    throw new RuntimeException("Could not find origin after browser reload");
  }

  public String captureDistance() {
    Rectangle distRect = new Rectangle();
    distRect.x = origin[0] + 200;
    distRect.y = origin[1] + 20;
    distRect.width = 200;
    distRect.height = 30;
    this.capture = rob.createScreenCapture(distRect);

    BufferedImage thresholded = ImageReader.threshold(this.capture);
    List<Rectangle> parts = ImageReader.segment(thresholded);
    this.transformed = ImageReader.drawParts(thresholded, parts);
    return ImageReader.readDigits(thresholded, parts);
  }

  public RunInfo playOneGame(String str, long maxDuration) {
    
    log.log("Playing " + str);
    doWait(500); // 0.5s wait to be sure QWOP is ready to run
    this.start = System.currentTimeMillis();
    this.nextCheck = this.start + CHECK_INTERVAL;
    if (maxDuration > 0) {
      this.timeLimit = this.start + maxDuration;
    } else {
      this.timeLimit = 0;
    }
    while (!(isFinished() || stop)) {
      playString(str);
    }
    stopRunning();
    checkSpeed();

    if (++nbRuns == MAX_RUNS_BETWEEN_RELOAD) {
      nbRuns = 0;
      refreshBrowser();
      log.log("Refreshing browser");
    }
    
    long end = System.currentTimeMillis();
    doWait(1000);
    float distance = Float.parseFloat(captureDistance());
    RunInfo info;
    if (stop) {
      info = new RunInfo(str, this.delay, false, true, end - this.start,
          distance);
    } else {
      info = new RunInfo(str, this.delay, distance < 100, false, end -
                                                                 this.start,
          distance);
    }
    return info;
  }
}
