package com.mgatelabs.piper.ui.dialogs;

import com.google.common.collect.Lists;
import com.mgatelabs.piper.shared.image.*;

import javax.swing.*;
import javax.swing.Timer;
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
  //private JList<SamplePoint> pointList;

  private List<SamplePoint> points;
  //RefreshableListModel<SamplePoint> sampleModel;

  public static final int cellSize = 10;

  //public static final int drawSize = cellSize * cells;

  boolean isOk = false;

  private Timer timer;

  private int miniScale;
  private int miniWidth;
  private int miniHeight;

  private final JDialog dialog;

  private final Mode mode;

  private final PickerHandler handler;

  private final ImagePixelPickerDialog instance;

  public ImagePixelPickerDialog(Mode mode, JFrame frame, PickerHandler handler) {
    super(frame, "Pixel Picker", false);
    this.points = new ArrayList<>();
    this.dialog = this;
    this.mode = mode;
    this.handler = handler;
    instance = this;

    buildComponents();
    setResizable(true);
    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

    setMinimumSize(new Dimension(800, 600));
  }

  private void buildComponents() {

    GridBagConstraints c = new GridBagConstraints();


    container = new JPanel();

    container.setLayout(new GridBagLayout());


    c.fill = GridBagConstraints.BOTH;
    c.gridx = 0;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.weighty = 1.0f;
    c.weightx = 1.0f;


    drawPanel = new ImageRenderer(points, this.mode);
    drawPanel.setBackground(Color.BLACK);
    //drawPanel.setMinimumSize(new Dimension(drawSize, drawSize));
    //drawPanel.setPreferredSize(drawPanel.getMinimumSize());
    //drawPanel.setMaximumSize(drawPanel.getMinimumSize());
    container.add(drawPanel, c);

    drawPanel.addMouseListener(new MouseListener() {
      @Override
      public void mouseClicked(MouseEvent e) {

        final int offX = (drawPanel.wCells * cellSize);

        if (e.getX() > offX) {
          if (e.getY() < miniHeight) {

            drawPanel.viewX = (e.getX() - offX) * miniScale;
            drawPanel.viewY = e.getY() * miniScale;

            drawPanel.viewX -= drawPanel.getCenterX();
            drawPanel.viewY -= drawPanel.getCenterY();

            drawPanel.repaint();

          } else {
            for (VirtualButton button: drawPanel.buttons) {
              int bX = offX + button.getX();
              if (e.getX() > bX && e.getX() < (bX + button.getW()) && e.getY() > button.getY() && e.getY() < (button.getY() + button.getH())) {
                switch (button.getActionId()) {
                  case 1: {
                    isOk = true;
                    if (handler != null) {
                      handler.finished(instance);
                    }
                    dialog.dispose();
                  } break;
                  case 2: {
                    if (handler != null) {
                      handler.finished(instance);
                    }
                    dialog.dispose();
                  } break;
                  case 3: {
                    drawPanel.clearPoints();
                  } break;
                  case 4: {
                    drawPanel.move(1);
                  } break;
                  case 5: {
                    drawPanel.move(-1);
                  } break;
                  default: break;
                }
                break;
              }
            }
          }
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

    /*
    JPanel sidePanel = new JPanel();
    sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
    container.add(sidePanel);

    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
    sidePanel.add(buttonPanel);
    */

    /*
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
*/

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

    //this.pointList.repaint();

    this.pack();
  }

  public void setup(ImageWrapper imageWrapper, int x, int y, int w, int h) {
    this.points.clear();
    this.points.add(new SamplePoint(x, y, 0, 0, 0));
    this.points.add(new SamplePoint(x + w, y + h, 0, 0, 0));

    commonSetup(imageWrapper);

    //this.pointList.repaint();

    this.pack();
  }

  public void commonSetup(ImageWrapper rawImageWrapper) {

    drawPanel.setImageWrapper(rawImageWrapper);

    miniScale = 10;
    miniHeight = rawImageWrapper.getHeight() / miniScale;
    miniWidth = rawImageWrapper.getWidth() / miniScale;

    drawPanel.setMiniHeight(miniHeight);
    drawPanel.setMiniWidth(miniWidth);
    drawPanel.setMiniScale(miniScale);

    drawPanel.makeReady();
  }

  public void start() {
    this.setVisible(true);
    this.toFront();
    this.repaint();
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
      //sampleModel.refresh();
    }

    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
      isOk = true;
      dialog.dispose();
    }

    if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
      dialog.dispose();
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

  public static class VirtualButton {
    private Color border;
    private Color fill;
    private Color fore;
    private String title;
    private int actionId;

    private int x;
    private int y;
    private int w;
    private int h;

    public VirtualButton(Color border, Color fill, Color fore, String title, int actionId) {
      this.border = border;
      this.fill = fill;
      this.fore = fore;
      this.title = title;
      this.actionId = actionId;
    }

    public Color getBorder() {
      return border;
    }

    public void setBorder(Color border) {
      this.border = border;
    }

    public Color getFill() {
      return fill;
    }

    public void setFill(Color fill) {
      this.fill = fill;
    }

    public Color getFore() {
      return fore;
    }

    public void setFore(Color fore) {
      this.fore = fore;
    }

    public String getTitle() {
      return title;
    }

    public void setTitle(String title) {
      this.title = title;
    }

    public int getActionId() {
      return actionId;
    }

    public void setActionId(int actionId) {
      this.actionId = actionId;
    }

    public int getX() {
      return x;
    }

    public void setX(int x) {
      this.x = x;
    }

    public int getY() {
      return y;
    }

    public void setY(int y) {
      this.y = y;
    }

    public int getW() {
      return w;
    }

    public void setW(int w) {
      this.w = w;
    }

    public int getH() {
      return h;
    }

    public void setH(int h) {
      this.h = h;
    }
  }

  private static class ImageRenderer extends JPanel {

    private ImageWrapper imageWrapper = null;
    private final List<SamplePoint> points;
    private int currentIndex = 0;

    private SamplePoint[] boxPoints;

    private int miniScale;
    private int miniWidth;
    private int miniHeight;

    private int viewX;
    private int viewY;

    //private int offX;

    private SamplePoint[][] samples;

    private Mode mode;

    private int getCenterX() {
      return wCells / 2;
    }

    private int getCenterY() {
      return hCells / 2;
    }

    public void move(int dir) {
      currentIndex += dir;

      if (currentIndex >= points.size()) {
        currentIndex = 0;
      } else if (currentIndex < 0) {
        currentIndex = points.size() - 1;
      }
      if (currentIndex >= 0 && currentIndex < points.size()) {
        SamplePoint point = points.get(currentIndex);
        set(point.getX(), point.getY());
        repaint();
      }
    }

    public ImageRenderer(List<SamplePoint> points, Mode mode) {
      this.points = points;
      viewX = 0;
      viewY = 0;
      this.mode = mode;

      //offX = cellSize * cells;

      boxPoints = new SamplePoint[2];
      boxPoints[0] = new SamplePoint(0, 0, 0, 0, 0);
      boxPoints[1] = new SamplePoint(0, 0, 0, 0, 0);
    }

    public SamplePoint[] getBoxPoints() {
      return boxPoints;
    }

    public void shift(int x, int y) {
      viewX += x;
      viewY += y;
    }

    public void set(int x, int y) {
      viewX = x - getCenterX();
      viewY = y - getCenterY();
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

    private List<VirtualButton> buttons = Lists.newArrayList();

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

      buttons.add(new VirtualButton(Color.white, Color.gray, Color.white, "OK", 1));
      buttons.add(new VirtualButton(Color.red, Color.gray, Color.white, "CANCEL", 2));
      buttons.add(new VirtualButton(Color.red, Color.darkGray, Color.white, "CLEAR", 3));
      buttons.add(new VirtualButton(Color.white, Color.gray, Color.white, "NEXT", 4));
      buttons.add(new VirtualButton(Color.white, Color.gray, Color.white, "PREVIOUS", 5));

      int trippleSize = (miniWidth / 3);
      int offY = miniHeight + trippleSize * 3 + 12;
      for (int i = 0; i < buttons.size(); i++) {
        VirtualButton button = buttons.get(i);
        button.setW(miniWidth);
        button.setH(24);
        button.setX(0);
        button.setY(offY);
        offY += 32;
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
      int px = viewX + getCenterX();
      int py = viewY + getCenterY();

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

    public int wCells = 51;
    public int hCells = 51;

    @Override
    protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      Graphics2D g2d = (Graphics2D) g;
      Sampler sample = new Sampler();

      wCells = (this.getWidth() - miniWidth) / cellSize;
      hCells = (this.getHeight()) / cellSize;

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

      int centerXOffset = getCenterX();
      int centerYOffset = getCenterY();

      // Pixels
      for (int y = 0; y < hCells; y++) {
        for (int x = 0; x < wCells; x++) {
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
          if (x == centerXOffset && y == centerYOffset) {
            g2d.setColor(Color.RED);
            g2d.drawRect((x * cellSize) + 1, ((y) * cellSize) + 1, cellSize - 2, cellSize - 2);
          }
        }
      }

      final int offX = wCells * cellSize;

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
      int centerX = ((int) ((viewX + centerXOffset) / (float) miniScale)) + offX;
      int centerY = ((int) ((viewY + centerYOffset) / (float) miniScale));

      g2d.setColor(Color.RED);

      g2d.drawLine(centerX - 2, centerY - 2, centerX + 2, centerY + 2);
      g2d.drawLine(centerX - 2, centerY + 2, centerX + 2, centerY - 2);

      // Color Grid

      int cx = viewX + getCenterX();
      int cy = viewY + getCenterY();

      //int cx = centerXOffset + viewX;
      //int cy = centerXOffset + viewY;


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

      // Buttons

      for (VirtualButton button: buttons) {

        g2d.setColor(button.getFill());
        g2d.fillRect(offX + button.getX(), button.getY(), button.getW(), button.getH());

        g2d.setColor(button.getBorder());
        g2d.drawRect(offX + button.getX(), button.getY(), button.getW(), button.getH());

        g2d.setColor(button.getFore());
        g2d.drawString(button.getTitle(),offX + button.getX() + 2, button.getY() + (button.getH() / 2));
      }

    } // end paintComponent
  }

}
