package com.mgatelabs.ffbe.ui;

import com.mgatelabs.ffbe.shared.*;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * @author <a href="mailto:mfuller@acteksoft.com">Michael Fuller</a>
 * Creation Date: 9/1/2017
 */
public class ImagePixelPickerDialog extends JDialog {

  private JPanel container;

  private ImageRenderer drawPanel;

  private RawImageReader rawImageReader;
  private List<SamplePoint> pointList;

  public static final int cellSize = 5;
  public static final int cells = 540 / 5;

  public ImagePixelPickerDialog() {
    super((JFrame) null, "Pixel Picker", true);
    buildComponents();
  }

  public void setup(RawImageReader rawImageReader, List<SamplePoint> pointList) {
    this.rawImageReader = rawImageReader;
    this.pointList = pointList;
    drawPanel.setRawImageReader(rawImageReader);
    drawPanel.setPointList(pointList);
  }

  private void buildComponents() {

    container = new JPanel();

    drawPanel = new ImageRenderer();
    drawPanel.setBackground(Color.BLACK);
    drawPanel.setMinimumSize(new Dimension(540, 540));
    drawPanel.setPreferredSize(drawPanel.getMinimumSize());
    drawPanel.setMaximumSize(drawPanel.getMinimumSize());

    container.add(drawPanel);

    this.getRootPane().setContentPane(container);
    this.pack();
  }

  public void start() {
    //this.drawPanel.repaint();
    this.setVisible(true);
  }

  private static class ImageRenderer extends JPanel {

    private RawImageReader rawImageReader = null;
    private List<SamplePoint> pointList = null;

    int viewX;
    int viewY;

    public ImageRenderer() {
      viewX = 60;
      viewY = 60;
    }

    public RawImageReader getRawImageReader() {
      return rawImageReader;
    }

    public void setRawImageReader(RawImageReader rawImageReader) {
      this.rawImageReader = rawImageReader;
    }

    public List<SamplePoint> getPointList() {
      return pointList;
    }

    public void setPointList(List<SamplePoint> pointList) {
      this.pointList = pointList;
    }

    @Override
    protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      Graphics2D g2d = (Graphics2D) g;
      ColorSample sample = new ColorSample();
      for (int y = 0; y < cells; y++){
        for(int x = 0; x < cells; x++) {
          int px = x + viewX;
          int py = y + viewY;
          if (px >= 0 && px < rawImageReader.getWidth() && py >= 0 && py < rawImageReader.getHeigth()) {
            rawImageReader.getPixel(px, py, sample);
            g2d.setColor(new Color(sample.getR(), sample.getG(), sample.getB()));
            g2d.fillRect(x * cellSize, y * cellSize, cellSize, cellSize);
          } else {
            g2d.setColor(Color.GRAY);
            g2d.fillRect(x * cellSize, y * cellSize, cellSize, cellSize);
          }
        }
      }
    }
  }

}
