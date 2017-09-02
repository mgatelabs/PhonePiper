package com.mgatelabs.ffbe.ui;

import com.mgatelabs.ffbe.shared.ColorSample;
import com.mgatelabs.ffbe.shared.image.ImageReader;
import com.mgatelabs.ffbe.shared.image.RawImageReader;
import com.mgatelabs.ffbe.shared.SamplePoint;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * @author <a href="mailto:mfuller@acteksoft.com">Michael Fuller</a>
 * Creation Date: 9/1/2017
 */
public class ImagePixelPickerDialog extends JDialog implements KeyListener {

    private JPanel container;

    private ImageRenderer drawPanel;

    private ImageReader imageReader;
    private List<SamplePoint> pointList;

    public static final int cellSize = 10;
    public static final int cells = 51;
    public static final int drawSize = cellSize * cells;
    public static final int centerIndex = (cells / 2) + 1;


    private Timer timer;

    private int miniScale;
    private int miniWidth;
    private int miniHeight;

    public ImagePixelPickerDialog() {
        super((JFrame) null, "Pixel Picker", true);
        buildComponents();
        setResizable(false);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    }

    public void setup(ImageReader rawImageReader, List<SamplePoint> pointList) {
        this.imageReader = rawImageReader;
        this.pointList = pointList;
        drawPanel.setImageReader(rawImageReader);
        drawPanel.setPointList(pointList);


        int miniScale = 15;
        miniHeight = rawImageReader.getHeight() / miniScale;
        miniWidth = rawImageReader.getWidth() / miniScale;

        drawPanel.setMiniHeight(miniHeight);
        drawPanel.setMiniWidth(miniWidth);
        drawPanel.setMiniScale(miniScale);

        drawPanel.setPreferredSize(new Dimension(drawSize + miniWidth, drawSize));
        drawPanel.setMaximumSize(drawPanel.getPreferredSize());

        drawPanel.makeReady();

        this.pack();
    }

    private void buildComponents() {

        container = new JPanel();

        drawPanel = new ImageRenderer();
        drawPanel.setBackground(Color.BLACK);
        drawPanel.setMinimumSize(new Dimension(drawSize, drawSize));
        drawPanel.setPreferredSize(drawPanel.getMinimumSize());
        drawPanel.setMaximumSize(drawPanel.getMinimumSize());

        container.add(drawPanel);

        this.addKeyListener(this);

        this.getRootPane().setContentPane(container);
        this.pack();

        ActionListener taskPerformer = new ActionListener() {
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
                    drawPanel.shift(x * xMulti,y * yMulti);
                    drawPanel.repaint();
                } else {
                    timer.stop();
                }
            }
        };

        timer = new Timer(500, taskPerformer);
    }

    public void start() {
        this.setVisible(true);
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
                drawPanel.shift(x,y);
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

        private ImageReader imageReader = null;
        private List<SamplePoint> pointList = null;

        private int miniScale;
        private int miniWidth;
        private int miniHeight;

        private int viewX;
        private int viewY;

        private int offX;

        private SamplePoint [][] samples;

        public ImageRenderer() {
            viewX = 0;
            viewY = 0;

            offX = cellSize * cells;

            this.addMouseListener(new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {

                    if (e.getX() < offX) {

                    } else if (e.getY() < miniHeight) {
                        // In the Zone

                        viewX = (e.getX() - offX) * miniScale;
                        viewY = e.getY() * miniScale;

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

        public void shift(int x, int y) {
            viewX += x;
            viewY += y;
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

        public ImageReader getImageReader() {
            return imageReader;
        }

        public void setImageReader(ImageReader imageReader) {
            this.imageReader = imageReader;

        }

        public void makeReady() {
            if (imageReader != null) {
                samples = new SamplePoint[this.imageReader.getHeight()][this.imageReader.getWidth()];
                for (SamplePoint point: pointList) {
                    samples[point.getY()][point.getY()] = point;
                }
            } else {
                samples = new SamplePoint[0][0];
            }
        }

        public void togglePoint() {
            int px = viewX + centerIndex;
            int py = viewY + centerIndex;

            if (px >= 0 && px < imageReader.getWidth() && py >=0 && py < imageReader.getHeight()) {
                if (samples[py][px] == null) {
                    ColorSample sample = new ColorSample();
                    imageReader.getPixel(px, py, sample);
                    SamplePoint point = new SamplePoint(px, py, sample.getR(),sample.getG(), sample.getB());
                    samples[py][px] = point;
                    pointList.add(point);
                } else {
                    pointList.remove(samples[py][px]);
                    samples[py][px] = null;
                }

                this.repaint();
            }
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
            for (int y = 0; y < cells; y++) {
                for (int x = 0; x < cells; x++) {
                    int px = x + viewX;
                    int py = y + viewY;
                    boolean isPoint = false;
                    if (px >= 0 && px < imageReader.getWidth() && py >= 0 && py < imageReader.getHeight()) {
                        imageReader.getPixel(px, py, sample);
                        g2d.setColor(new Color(sample.getR(), sample.getG(), sample.getB()));
                        g2d.fillRect(x * cellSize, (y) * cellSize, cellSize, cellSize);
                        isPoint = samples[py][px] != null;
                    } else {
                        g2d.setColor(Color.GRAY);
                        g2d.fillRect(x * cellSize, (y) * cellSize, cellSize, cellSize);
                    }
                    if (isPoint) {
                        g2d.setColor(Color.GREEN);
                        g2d.drawRect((x * cellSize) + 2, ((y) * cellSize) + 2, cellSize - 4, cellSize - 4);
                    }
                    if (x == centerIndex && y == centerIndex) {
                        g2d.setColor(Color.RED);
                        g2d.drawRect((x * cellSize) + 1, ((y) * cellSize) + 1, cellSize - 2, cellSize - 2);
                    }
                }
            }

            for (int y = 0; y < miniHeight; y++) {
                for (int x = 0; x < miniWidth; x++) {
                    int px = x * miniScale;
                    int py = y * miniScale;
                    Color color = Color.BLACK;
                    if (px >= 0 && px < imageReader.getWidth() && py >= 0 && py < imageReader.getHeight()) {
                        imageReader.getPixel(px, py, sample);
                        color = new Color(sample.getR(), sample.getG(), sample.getB());
                    }
                    g2d.setColor(color);
                    g2d.fillRect(offX + x, y, 1, 1);
                }
            }

            int centerX = ((int)((viewX + centerIndex) / (float)miniScale)) + offX;
            int centerY = ((int)((viewY + centerIndex) / (float)miniScale));

            g2d.setColor(Color.RED);

            g2d.drawLine(centerX - 2, centerY - 2, centerX + 2, centerY + 2);
            g2d.drawLine(centerX - 2, centerY + 2, centerX + 2, centerY - 2);
        }
    }

}
