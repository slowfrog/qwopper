package com.slowfrog.qwop;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

/**
 * That class is supposed to contain the code used to read the distance on
 * screen. That's where all the image recognition fun lives. And it's unit
 * tested too!
 */
public class ImageReader {

  private static final BufferedImage REF_DIGITS = loadRefDigits();
  private static final String DIGITS = "-.0123456789";
  private static final int[] DIGIT_X = { 3, 15, 24, 44, 57, 76, 93, 113, 130,
                                        149, 167, 186, 205 };

  private static BufferedImage loadRefDigits() {
    try {
      return ImageIO.read(new File("img/digits.png"));

    } catch (IOException e) {
      throw new RuntimeException(" Error reading reference digits", e);
    }
  }

  public static float readDistance(BufferedImage img) {
    BufferedImage thresholded = threshold(img);
    List<Rectangle> parts = segment(thresholded);
    String str = readDigits(thresholded, parts);
    return Float.parseFloat(str);
  }

  private static int luminosity(int col) {
    int r = (col >> 16) & 0xff;
    int g = (col >> 8) & 0xff;
    int b = col & 0xff;
    int lum = (g * 8 + r * 5 + b * 3) >> 4;
    return lum;
  }

  public static BufferedImage threshold(BufferedImage input) {
    int width = input.getWidth();
    int height = input.getHeight();
    BufferedImage output = new BufferedImage(width, height,
        BufferedImage.TYPE_BYTE_BINARY);
    for (int y = 0; y < height; ++y) {
      for (int x = 0; x < width; ++x) {
        int col = input.getRGB(x, y);
        int lum = luminosity(col) < 180 ? 0 : 0xff;
        int col2 = (lum << 16) | (lum << 8) | lum;
        output.setRGB(x, y, col2);
      }
    }

    return output;
  }

  private static String compareDigit(BufferedImage img, Rectangle rect) {
    String ret = "";
    int bestMatch = -1;
    for (int i = 0; i < DIGITS.length(); ++i) {
      String d = DIGITS.substring(i, i + 1);
      int startx = DIGIT_X[i];
      int endx = DIGIT_X[i + 1] - 3;
      int w = Math.min(endx - startx, rect.width);

      int sum = 0;
      for (int x = 0; x < w; ++x) {
        int refx = startx + x;
        int imgx = rect.x + x;
        for (int y = 0; y < rect.height; ++y) {
          sum += (img.getRGB(imgx, y) == REF_DIGITS.getRGB(refx, y)) ? 1 : 0;
        }
      }
      int match = (sum * 100) / (w * rect.height);
      // System.out.printf("comp(%s)=%d: %d\n", d, sum, match);
      if ((bestMatch == -1) || (match > bestMatch)) {
        bestMatch = match;
        ret = d;
      }
    }
    // System.out.println("==>" + ret + "   " + bestMatch);
    if (bestMatch < 90) {
      ret = "";
    }

    return ret;
  }

  public static List<Rectangle> segment(BufferedImage input) {
    int width = input.getWidth();
    int height = input.getHeight();

    List<Rectangle> parts = new ArrayList<Rectangle>();
    int lastFreeX = -1;
    X_LOOP:

    for (int x = 0; x < width; ++x) {
      for (int y = 0; y < height; ++y) {
        int col = input.getRGB(x, y) & 0xffffff;
        if (col != 0) {
          continue X_LOOP;
        }
      }

      // Nothing in that column
      if (x > lastFreeX + 1) {
        Rectangle rect = new Rectangle(lastFreeX + 1, 0, x - lastFreeX - 1,
            height);
        parts.add(rect);
      }
      lastFreeX = x;
    }
    return parts;
  }

  public static BufferedImage drawParts(BufferedImage input,
      List<Rectangle> parts) {
    
    int width = input.getWidth();
    int height = input.getHeight();
    BufferedImage output = new BufferedImage(width, height,
        BufferedImage.TYPE_3BYTE_BGR);
    for (int x = 0; x < width; ++x) {
      for (int y = 0; y < height; ++y) {
        output.setRGB(x, y, 0xff0000);
      }
    }

    int lastX = 0;
    for (Rectangle rect : parts) {
      for (int x = 0; x < rect.width; ++x) {
        for (int y = 0; y < rect.height; ++y) {
          output.setRGB(lastX + x, y, input.getRGB(rect.x + x, y));
        }
      }
      lastX += rect.width + 3;
    }    
    return output;
  }

  public static String readDigits(BufferedImage input, List<Rectangle> parts) {
    String str = "";
    for (Rectangle rect : parts) {
      str += compareDigit(input, rect);
    }
    return str;
  }
}
