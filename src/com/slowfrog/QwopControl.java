package com.slowfrog;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Random;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

public class QwopControl extends JFrame implements Log {
  
  private static final long serialVersionUID = 1;

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
  private Random random;

  public QwopControl() throws AWTException {
    super("QWOP control");
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

    final JButton go = new JButton("Run, Qwop, run!");
    bar.add(go);

    JPanel top = new JPanel();
    top.setLayout(new BorderLayout());
    top.add(new JLabel("Current: "), BorderLayout.WEST);
    sequence = new JTextField();
    top.add(sequence, BorderLayout.CENTER);
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

    go.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ev) {
        // This is to restore the mouse approximately at its starting position
        // after having clicked on the QWOP window to transfer keyboard focus
        final Point screenPoint = new Point(5, 5);
        SwingUtilities.convertPointToScreen(screenPoint, go);
        execOutOfAWT(new Runnable() {
          public void run() {
            qwopper.startGame();
            rob.mouseMove(screenPoint.x, screenPoint.y);
            String dna = Qwopper.makeRealisticRandomString(10 + random.nextInt(21));
            sequence.setText(dna);
            qwopper.playOneRandomGame(dna);
          }
        });
      }
    });
  }

  private void execOutOfAWT(Runnable r) {
    Thread t = new Thread(r);
    t.start();
  }

  public void log(String message) {
    // Using setText() to enable auto-scrolling
    logOutput.setText(logOutput.getText() + message + "\n");
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
