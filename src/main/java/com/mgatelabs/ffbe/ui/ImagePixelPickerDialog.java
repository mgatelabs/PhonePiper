package com.mgatelabs.ffbe.ui;

import com.mgatelabs.ffbe.shared.image.*;
import com.mgatelabs.ffbe.ui.utils.RefreshableListModel;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**
 * @author <a href="mailto:mfuller@acteksoft.com">Michael Fuller</a>
 * Creation Date: 9/1/2017
 */
public class ImagePixelPickerDialog extends JDialog implements KeyListener {

  public enum Mode {
    PIXELS,
    BOX
  }

  private JPanel container;

  private ImageRenderer drawPanel;
  private JList<SamplePoint> pointList;

  private List<SamplePoint> points;
  RefreshableListModel<SamplePoint> sampleModel;

  public static final int cellSize = 10;
  public static final int cells = 51;
  public static final int drawSize = cellSize * cells;
  public static final int centerIndex = (cells / 2) + 1;

  boolean isOk = false;

  private Timer timer;

  private int miniScale;
  private int miniWidth;
  private int miniHeight;

  private final JDialog dialog;

  private final Mode mode;

  public ImagePixelPickerDialog(Mode mode, JFrame frame) {
    super(frame, "Pixel Picker", true);
    this.points = new ArrayList<>();
    this.dialog = this;
    this.mode = mode;

    buildComponents();
    setResizable(false);
    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
  }

  private void buildComponents() {

    container = new JPanel();

    drawPanel = new ImageRenderer(points, this.mode);
    drawPanel.setBackground(Color.BLACK);
    drawPanel.setMinimumSize(new Dimension(drawSize, drawSize));
    drawPanel.setPreferredSize(drawPanel.getMinimumSize());
    drawPanel.setMaximumSize(drawPanel.getMinimumSize());
    container.add(drawPanel);

    JPanel sidePanel = new JPanel();
    sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
    container.add(sidePanel);

    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
    sidePanel.add(buttonPanel);

    JButton moveButton = new JButton("MOVE");
    buttonPanel.add(moveButton);
    moveButton.addKeyListener(this);

    JButton clearButton = new JButton("CLEAR");
    buttonPanel.add(clearButton);
    clearButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        drawPanel.clearPoints();
        sampleModel.refresh();
      }
    });

    JButton okButton = new JButton("OK");
    buttonPanel.add(okButton);
    okButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        isOk = true;
        dialog.dispose();
      }
    });

    JButton cancelButton = new JButton("CANCEL");
    buttonPanel.add(cancelButton);
    cancelButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        dialog.dispose();
      }
    });

    sampleModel = new RefreshableListModel<SamplePoint>(points);

    pointList = new JList<>(sampleModel);
    pointList.addListSelectionListener(new ListSelectionListener() {
      @Override
      public void valueChanged(ListSelectionEvent e) {
        int index = pointList.getSelectedIndex();
        if (index >= 0 && index < points.size()) {
          SamplePoint point = points.get(index);
          drawPanel.set(point.getX(), point.getY());
          drawPanel.repaint();
        }
      }
    });
    JScrollPane pointListScroller = new JScrollPane(pointList);
    pointListScroller.setPreferredSize(new Dimension(200, 200));
    sidePanel.add(pointListScroller);

    this.addKeyListener(this);

    this.getRootPane().setContentPane(container);

    ActionListener taskPerformer = new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent evt) {

        int x = 0;
        int y = 0;

        if (upState) {
          y = -1;
          yMulti++;
        } else if (downState) {
          y = 1;
          yMulti++;
        } else {
          yMulti = 1;
        }

        if (leftState) {
          x = -1;
          xMulti++;
        } else if (rightState) {
          x = 1;
          xMulti++;
        } else {
          xMulti = 1;
        }

        if (x != 0 || y != 0) {
          drawPanel.shift(x * xMulti, y * yMulti);
          drawPanel.repaint();
        } else {
          timer.stop();
        }
      }
    };

    timer = new Timer(500, taskPerformer);
  }

  public boolean isOk() {
    return isOk;
  }

  public void setup(ImageWrapper imageWrapper, List<SamplePoint> points) {
    this.points.clear();
    this.points.addAll(points);

    commonSetup(imageWrapper);

    this.pointList.repaint();

    this.pack();
  }

  public void setup(ImageWrapper imageWrapper, int x, int y, int w, int h) {
    this.points.clear();
    this.points.add(new SamplePoint(x, y, 0, 0, 0));
    this.points.add(new SamplePoint(x + w, y + h, 0, 0, 0));

    commonSetup(imageWrapper);

    this.pointList.repaint();

    this.pack();
  }

  public void commonSetup(ImageWrapper rawImageWrapper) {

    drawPanel.setImageWrapper(rawImageWrapper);

    int miniScale = 15;
    miniHeight = rawImageWrapper.getHeight() / miniScale;
    miniWidth = rawImageWrapper.getWidth() / miniScale;

    drawPanel.setMiniHeight(miniHeight);
    drawPanel.setMiniWidth(miniWidth);
    drawPanel.setMiniScale(miniScale);

    drawPanel.setPreferredSize(new Dimension(drawSize + miniWidth, drawSize));
    drawPanel.setMaximumSize(drawPanel.getPreferredSize());

    drawPanel.makeReady();
  }

  public void start() {
    this.setVisible(true);
  }

  public List<SamplePoint> getPoints() {
    return points;
  }

  boolean spaceState;
  boolean upState;
  boolean downState;
  boolean leftState;
  boolean rightState;
  public int xMulti = 1;
  public int yMulti = 1;

  @Override
  public void keyTyped(KeyEvent e) {

  }

  @Override
  public void keyPressed(KeyEvent e) {
    int x = 0;
    int y = 0;

    if (e.getKeyCode() == KeyEvent.VK_SPACE && !spaceState) {
      spaceState = true;
    }

    if (e.getKeyCode() == KeyEvent.VK_UP && !upState) {
      upState = true;
      downState = false;
      y = -1;
      yMulti = 1;
    }
    if (e.getKeyCode() == KeyEvent.VK_DOWN && !downState) {
      downState = true;
      upState = false;
      y = 1;
      yMulti = 1;
    }
    if (e.getKeyCode() == KeyEvent.VK_LEFT && !leftState) {
      leftState = true;
      rightState = false;
      x = -1;
      xMulti = 1;
    }
    if (e.getKeyCode() == KeyEvent.VK_RIGHT && !rightState) {
      rightState = true;
      leftState = false;
      x = 1;
      xMulti = 1;
    }

    if (upState || downState || leftState || rightState) {

      if (x != 0 || y != 0) {
        drawPanel.shift(x, y);
        drawPanel.repaint();
      }

      timer.start();
    } else {
      timer.stop();
    }
  }

  @Override
  public void keyReleased(KeyEvent e) {

    if (e.getKeyCode() == KeyEvent.VK_SPACE) {
      spaceState = false;
      drawPanel.togglePoint();
      sampleModel.refresh();
    }

    if (e.getKeyCode() == KeyEvent.VK_UP) {
      upState = false;
      yMulti = 1;
    }
    if (e.getKeyCode() == KeyEvent.VK_DOWN) {
      downState = false;
      yMulti = 1;
    }
    if (e.getKeyCode() == KeyEvent.VK_LEFT) {
      leftState = false;
      xMulti = 1;
    }
    if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
      rightState = false;
      xMulti = 1;
    }

    if (upState || downState || leftState || rightState) {
      timer.start();
    } else {
      timer.stop();
    }
  }

  private static class ImageRenderer extends JPanel {

    private ImageWrapper imageWrapper = null;
    private final List<SamplePoint> points;

    private SamplePoint[] boxPoints;

    private int miniScale;
    private int miniWidth;
    private int miniHeight;

    private int viewX;
    private int viewY;

    private int offX;

    private SamplePoint[][] samples;

    private Mode mode;

    public ImageRenderer(List<SamplePoint> points, Mode mode) {
      this.points = points;
      viewX = 0;
      viewY = 0;
      this.mode = mode;

      offX = cellSize * cells;

      boxPoints = new SamplePoint[2];
      boxPoints[0] = new SamplePoint(0, 0, 0, 0, 0);
      boxPoints[1] = new SamplePoint(0, 0, 0, 0, 0);

      this.addMouseListener(new MouseListener() {
        @Override
        public void mouseClicked(MouseEvent e) {

          if (e.getX() < offX) {

          } else if (e.getY() < miniHeight) {
            // In the Zone

            viewX = (e.getX() - offX) * miniScale;
            viewY = e.getY() * miniScale;

            viewX -= centerIndex;
            viewY -= centerIndex;

            repaint();
          }
        }

        @Override
        public void mousePressed(MouseEvent e) {

        }

        @Override
        public void mouseReleased(MouseEvent e) {

        }

        @Override
        public void mouseEntered(MouseEvent e) {

        }

        @Override
        public void mouseExited(MouseEvent e) {

        }
      });

    }

    public SamplePoint[] getBoxPoints() {
      return boxPoints;
    }

    public void shift(int x, int y) {
      viewX += x;
      viewY += y;
    }

    public void set(int x, int y) {
      viewX = x - centerIndex;
      viewY = y - centerIndex;
    }

    public int getMiniScale() {
      return miniScale;
    }

    public void setMiniScale(int miniScale) {
      this.miniScale = miniScale;
    }

    public int getMiniWidth() {
      return miniWidth;
    }

    public void setMiniWidth(int miniWidth) {
      this.miniWidth = miniWidth;
    }

    public int getMiniHeight() {
      return miniHeight;
    }

    public void setMiniHeight(int miniHeight) {
      this.miniHeight = miniHeight;
    }

    public int getViewX() {
      return viewX;
    }

    public void setViewX(int viewX) {
      this.viewX = viewX;
    }

    public int getViewY() {
      return viewY;
    }

    public void setViewY(int viewY) {
      this.viewY = viewY;
    }

    public ImageWrapper getImageWrapper() {
      return imageWrapper;
    }

    public void setImageWrapper(ImageWrapper imageWrapper) {
      this.imageWrapper = imageWrapper;

    }

    public void makeReady() {
      if (imageWrapper != null) {
        samples = new SamplePoint[this.imageWrapper.getHeight()][this.imageWrapper.getWidth()];
        for (SamplePoint point : points) {
          samples[point.getY()][point.getX()] = point;
        }
      } else {
        samples = new SamplePoint[0][0];
      }
    }

    public void clearPoints() {
      for (SamplePoint point: points) {
        samples[point.getY()][point.getX()] = null;
      }
      points.clear();
      this.repaint();
    }

    public void togglePoint() {
      int px = viewX + centerIndex;
      int py = viewY + centerIndex;

      if (px >= 0 && px < imageWrapper.getWidth() && py >= 0 && py < imageWrapper.getHeight()) {
        if (samples[py][px] == null) {
          Sampler sample = new Sampler();
          imageWrapper.getPixel(px, py, sample);
          SamplePoint point = new SamplePoint(px, py, sample.getR(), sample.getG(), sample.getB());
          samples[py][px] = point;
          points.add(point);
        } else {
          points.remove(samples[py][px]);
          samples[py][px] = null;
        }

        this.repaint();
      }
    }

    public List<SamplePoint> getPoints() {
      return points;
    }

    @Override
    protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      Graphics2D g2d = (Graphics2D) g;
      Sampler sample = new Sampler();

      int boxX1 = -1;
      int boxX2 = -1;
      int boxY1 = -1;
      int boxY2 = -1;

      if (mode == Mode.BOX && points.size() == 2) {
        boxX1 = points.get(0).getX();
        boxY1 = points.get(0).getY();
        boxX2 = points.get(1).getX();
        boxY2 = points.get(1).getY();
        if (boxX1 > boxX2) {
          int temp = boxX1;
          boxX1 = boxX2;
          boxX2 = temp;
        }
        if (boxY1 > boxY2) {
          int temp = boxY1;
          boxY1 = boxY2;
          boxY2 = temp;
        }
      }


      // Pixels
      for (int y = 0; y < cells; y++) {
        for (int x = 0; x < cells; x++) {
          int px = x + viewX;
          int py = y + viewY;
          boolean isPoint = false;
          if (px >= 0 && px < imageWrapper.getWidth() && py >= 0 && py < imageWrapper.getHeight()) {
            imageWrapper.getPixel(px, py, sample);
            g2d.setColor(new Color(sample.getR(), sample.getG(), sample.getB()));
            g2d.fillRect(x * cellSize, (y) * cellSize, cellSize, cellSize);
            isPoint = samples[py][px] != null;
          } else {
            g2d.setColor(Color.GRAY);
            g2d.fillRect(x * cellSize, (y) * cellSize, cellSize, cellSize);
          }
          if (mode == Mode.PIXELS) {
            if (isPoint) {
              g2d.setColor(Color.GREEN);
              g2d.drawRect((x * cellSize) + 2, ((y) * cellSize) + 2, cellSize - 4, cellSize - 4);
            }
          } else if (mode == Mode.BOX) {
            if (boxX1 != -1 && px >= boxX1 && px <= boxX2 && py >= boxY1 && py <= boxY2) {
              g2d.setColor(Color.YELLOW);
              g2d.drawRect((x * cellSize) + 2, ((y) * cellSize) + 2, cellSize - 4, cellSize - 4);
            } else if (boxX1 == -1 && isPoint) {
              g2d.setColor(Color.GREEN);
              g2d.drawRect((x * cellSize) + 2, ((y) * cellSize) + 2, cellSize - 4, cellSize - 4);
            }
          }
          if (x == centerIndex && y == centerIndex) {
            g2d.setColor(Color.RED);
            g2d.drawRect((x * cellSize) + 1, ((y) * cellSize) + 1, cellSize - 2, cellSize - 2);
          }
        }
      }

      // Mini Map
      for (int y = 0; y < miniHeight; y++) {
        for (int x = 0; x < miniWidth; x++) {
          int px = x * miniScale;
          int py = y * miniScale;
          Color color = Color.BLACK;
          if (px >= 0 && px < imageWrapper.getWidth() && py >= 0 && py < imageWrapper.getHeight()) {
            imageWrapper.getPixel(px, py, sample);
            if (boxX1 != -1 && px >= boxX1 && px <= boxX2 && py >= boxY1 && py <= boxY2) {
              color = Color.YELLOW;
            } else {
              color = new Color(sample.getR(), sample.getG(), sample.getB());
            }
          }
          g2d.setColor(color);
          g2d.fillRect(offX + x, y, 1, 1);
        }
      }

      // Cross
      int centerX = ((int) ((viewX + centerIndex) / (float) miniScale)) + offX;
      int centerY = ((int) ((viewY + centerIndex) / (float) miniScale));

      g2d.setColor(Color.RED);

      g2d.drawLine(centerX - 2, centerY - 2, centerX + 2, centerY + 2);
      g2d.drawLine(centerX - 2, centerY + 2, centerX + 2, centerY - 2);

      // Color Grid

      int cx = centerIndex + viewX;
      int cy = centerIndex + viewY;


      if (cx > 0 && cx < imageWrapper.getWidth() - 1 && cy > 0 && cy <= imageWrapper.getHeight() - 1) {
        Sampler center = new Sampler();
        imageWrapper.getPixel(cx, cy, center);
        int trippleSize = (miniWidth / 3);
        for (int y = -1; y <= 1; y++) {
          for (int x = -1; x <= 1; x++) {
            imageWrapper.getPixel(cx + x, cy + y, sample);

            int diffR = center.getR() - sample.getR();
            int diffG = center.getG() - sample.getG();
            int diffB = center.getB() - sample.getB();
            if (diffR < 0) diffR *= -1;
            if (diffG < 0) diffG *= -1;
            if (diffB < 0) diffB *= -1;

            g2d.setColor(new Color(sample.getR(), sample.getG(), sample.getB()));
            g2d.fillRect(offX + (trippleSize + (x * trippleSize)), miniHeight + (trippleSize + (y * trippleSize)), trippleSize, trippleSize);

            if (x == 0 && y == 0) {

            } else {

              if (diffR <= 6 && diffG <= 6 && diffB <= 6) {
                g2d.setColor(Color.GREEN);
              } else {
                g2d.setColor(Color.RED);
              }

              g2d.drawRect(offX + (trippleSize + (x * trippleSize)) + 1, miniHeight + (trippleSize + (y * trippleSize)) + 1, trippleSize - 2, trippleSize - 2);
            }
          }
        }
      }

    } // end paintComponent
  }

}
