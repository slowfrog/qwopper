package com.slowfrog.qwop;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import org.junit.Test;

import com.slowfrog.qwop.ImageReader;

public class TestImageReader {

  @Test
  public void readImages() throws IOException {
    BufferedImage img = ImageIO.read(new File("img/dist_11.2.png"));
    float result = ImageReader.readDistance(img);
    assertThat(result, equalTo(11.2f));
  }
}
