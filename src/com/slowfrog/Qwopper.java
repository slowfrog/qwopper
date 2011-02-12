/*
 * Copyright SlowFrog 2011
 *
 * License granted to anyone for any kind of purpose as long as you don't sue me.
 */
package com.slowfrog;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
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

  /** Look for the origin of the game area on screen. */
  private static int[] findOrigin(Robot rob) throws AWTException {
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
  private static boolean isFinished(Robot rob, int[] origin) {
    Color col1 = rob.getPixelColor(origin[0] + 157, origin[1] + 126);
    if (colorMatches(
        (col1.getRed() << 16) | (col1.getGreen() << 8) | col1.getBlue(),
        0xffff00)) {
      Color col2 = rob.getPixelColor(origin[0] + 482, origin[1] + 126);
      if (colorMatches(
          (col2.getRed() << 16) | (col2.getGreen() << 8) | col2.getBlue(),
          0xffff00)) {
        return true;
      }

    }
    return false;
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
   * Play a string. Interpret a string of QWOPqwop+ as a music sheet. QWOP means
   * press the key Q, W, O or P qwop means release the key + means wait for a
   * small delay
   * 
   */
  private static void playString(Robot rob, String str) {
    for (int i = 0; i < str.length(); ++i) {
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
        doWait(100);
        break;

      default:
        System.out.println("Unkown 'note': " + c);
      }
    }
  }

  /** All possible 'notes' */
  private static final String NOTES = "QWOPqwop+";

  /** Builds a completely random string of 'notes'. */
  private static String makeRandomString(int len) {
    Random random = new Random(System.currentTimeMillis());
    String l = "";
    for (int i = 0; i < len; ++i) {
      int rnd = random.nextInt(NOTES.length());
      l += NOTES.substring(rnd, rnd + 1);
    }
    return l;
  }

  private static void playOneRandomGame(Robot rob, int[] origin) {
    String str = makeRandomString(30);
    while (!isFinished(rob, origin)) {
      playString(rob, str);
    }
    System.out.println("\nFinished!");
    doWait(5000);
  }

  public static void main(String[] args) {
    try {
      Robot rob = new Robot();
      int[] origin = findOrigin(rob);
      System.out.printf("Origin: %d,%d", origin[0], origin[1]);
      clickAt(rob, origin[0], origin[1]);

      while (true) {
        playOneRandomGame(rob, origin);
        clickKey(rob, KeyEvent.VK_SPACE);
      }

    } catch (Throwable t) {
      t.printStackTrace();
    }
  }
}
