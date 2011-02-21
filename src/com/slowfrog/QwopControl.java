package com.slowfrog;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Random;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class QwopControl extends JFrame implements Log {

  private static final long serialVersionUID = 1;

  private static final Font FONT = new Font("Lucida Sans", Font.BOLD, 24);

  public static void main(String[] args) {
    try {
      JFrame f = new QwopControl();
      f.pack();
      f.setVisible(true);

    } catch (Throwable t) {
      t.printStackTrace();
    }
  }

  private Robot rob;
  protected Qwopper qwopper;
  private JTextArea logOutput;
  private JScrollPane logScroll;
  private JTextField sequence;
  private JLabel distance;
  private JLabel distance2;
  private JLabel distance3;

  private long startTime;
  private Random random;
  private Timer timer;

  public QwopControl() throws AWTException {
    super("QWOP control");
    this.move(200, 0);
    this.setDefaultCloseOperation(EXIT_ON_CLOSE);

    rob = new Robot();
    random = new Random(System.currentTimeMillis());
    qwopper = new Qwopper(rob, this);

    Container c = this.getContentPane();
    c.setLayout(new BorderLayout());

    JPanel bar = new JPanel();
    bar.setLayout(new BoxLayout(bar, BoxLayout.X_AXIS));
    c.add(bar, BorderLayout.SOUTH);

    JButton init = new JButton("Find game area");
    bar.add(init);

    final JButton goRandom = new JButton("Random...");
    bar.add(goRandom);

    final JButton go = new JButton("Run, Qwop, run!");
    bar.add(go);

    final JButton stop = new JButton("Stop");
    bar.add(stop);

    JPanel top = new JPanel();
    top.setLayout(new BorderLayout());
    top.add(new JLabel("Current: "), BorderLayout.WEST);
    sequence = new JTextField();
    top.add(sequence, BorderLayout.CENTER);
    JPanel bottom = new JPanel();
    top.add(bottom, BorderLayout.SOUTH);
    bottom.setLayout(new FlowLayout());
    distance = new JLabel();
    distance.setPreferredSize(new Dimension(200, 30));
    bottom.add(distance);
    distance2 = new JLabel();
    distance2.setPreferredSize(new Dimension(200, 30));
    bottom.add(distance2);
    distance3 = new JLabel();
    distance3.setFont(FONT);
    distance3.setPreferredSize(new Dimension(300, 30));
    bottom.add(distance3);
    c.add(top, BorderLayout.NORTH);

    logOutput = new JTextArea(20, 60);
    logOutput.setEditable(false);
    logScroll = new JScrollPane(logOutput);
    logScroll
        .setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    c.add(logScroll, BorderLayout.CENTER);

    // Add event handlers
    init.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ev) {
        try {
          int[] origin = qwopper.findRealOrigin();
          logf("Origin at %d,%d", origin[0], origin[1]);

        } catch (Throwable e) {
          log("Error finding origin", e);
        }
      }
    });

    goRandom.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ev) {
        String dna = Qwopper.makeRealisticRandomString(10 + random.nextInt(21));
        sequence.setText(dna);
      }
    });

    go.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ev) {
        startTime = System.currentTimeMillis();
        runGame();
        go.setEnabled(false);
      }
    });

    stop.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ev) {
        qwopper.stop();
        go.setEnabled(true);
      }
    });

    timer = new Timer(1000, new ActionListener() {
      public void actionPerformed(ActionEvent ev) {
        Rectangle distRect = new Rectangle();
        int[] origin = qwopper.getOrigin();
        distRect.x = origin[0] + 200;
        distRect.y = origin[1] + 20;
        distRect.width = 200;
        distRect.height = 30;
        BufferedImage distImg = rob.createScreenCapture(distRect);
        ImageIcon icon = new ImageIcon();
        icon.setImage(distImg);
        distance.setIcon(icon);

        BufferedImage transformed = ImageReader.threshold(distImg);
        List<Rectangle> parts = ImageReader.segment(transformed);
        BufferedImage segmented = ImageReader.drawParts(transformed, parts);
        ImageIcon icon2 = new ImageIcon();
        icon2.setImage(segmented);
        distance2.setIcon(icon2);
        long duration = (System.currentTimeMillis() - startTime) / 1000;
        if (duration == 0) {
          duration = 1; // To avoid division by zero
        }
        String time = (duration / 60) + ":" +
                      new DecimalFormat("00").format(duration % 60);
        float distance = Float.parseFloat(ImageReader.readDigits(transformed,
            parts));
        float speed = (distance / duration);
        DecimalFormatSymbols symbols = new DecimalFormat()
            .getDecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        DecimalFormat df = new DecimalFormat("0.000", symbols);
        String speedStr = df.format(speed);
        distance3.setText(distance + " in " + time + ", v=" + speedStr);

        if (!qwopper.isRunning()) {
          timer.stop();
          go.setEnabled(true);
        }
      }
    });
  }

  private void runGame() {
    // This is to restore the mouse to its starting position
    // after having clicked on the QWOP window to transfer keyboard focus
    final Point screenPoint = MouseInfo.getPointerInfo().getLocation();
    execOutOfAWT(new Runnable() {
      public void run() {
        qwopper.startGame();
        rob.mouseMove(screenPoint.x, screenPoint.y);
        timer.setDelay(250);
        timer.start();

        String dna = sequence.getText();
        qwopper.playOneRandomGame(dna);
      }
    });
  }

  private void execOutOfAWT(Runnable r) {
    Thread t = new Thread(r);
    t.start();
  }

  public void log(final String message) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        // Using setText() to enable auto-scrolling
        logOutput.setText(logOutput.getText() + message + "\n");
      }
    });
  }

  public void log(String message, Throwable e) {
    log(message);
    StringWriter sout = new StringWriter();
    PrintWriter out = new PrintWriter(sout);
    e.printStackTrace(out);
    out.flush();
    log(sout.getBuffer().toString());
  }

  public void logf(String format, Object... args) {
    StringWriter sout = new StringWriter();
    PrintWriter out = new PrintWriter(sout);
    out.printf(format, args);
    out.flush();
    log(sout.getBuffer().toString());
  }

}
