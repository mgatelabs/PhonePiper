package com.mgatelabs.piper.ui.panels;

import com.mgatelabs.piper.shared.mapper.FloorDefinition;
import com.mgatelabs.piper.shared.mapper.MapDefinition;
import com.mgatelabs.piper.shared.mapper.MapSampleArea;
import com.mgatelabs.piper.shared.mapper.TileDefinition;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/10/2017 for Phone-Piper
 */
public class MapRenderPanel extends JPanel {

    private MapDefinition mapDefinition;

    private int viewX;
    private int viewY;

    private String floorId;
    private FloorDefinition floorDefinition;

    private int sampleX;
    private int sampleY;
    private MapSampleArea mapSampleArea;

    private Timer timer;

    private static final int cellSize = 10;

    public MapRenderPanel() {
        floorDefinition = null;
        floorId = null;
        mapSampleArea = null;

        viewX = 0;
        viewY = 0;

        sampleX = 0;
        sampleY = 0;

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
                    shift(x * xMulti, y * yMulti);
                    repaint();
                } else {
                    timer.stop();
                }
            }
        };

        timer = new Timer(500, taskPerformer);

        setFocusable(true);

        this.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("LEFT"),
                "leftPressed");
        this.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("released LEFT"),
                "leftReleased");

        this.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("RIGHT"),
                "rightPressed");
        this.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("released RIGHT"),
                "rightReleased");

        this.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("UP"),
                "upPressed");
        this.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("released UP"),
                "upReleased");

        this.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("DOWN"),
                "downPressed");
        this.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("released DOWN"),
                "downReleased");

        this.getActionMap().put("leftPressed", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                leftState = true;
                xMulti = 1;
                keyPressed(-1, 0);
            }
        });

        this.getActionMap().put("leftReleased", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                leftState = false;
                keyReleased();
            }
        });

        this.getActionMap().put("rightPressed", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                rightState = true;
                xMulti = 1;
                keyPressed(1, 0);
            }
        });

        this.getActionMap().put("rightReleased", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                rightState = false;
                keyReleased();
            }
        });

        // UP

        this.getActionMap().put("upPressed", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                upState = true;
                yMulti = 1;
                keyPressed(0, -1);
            }
        });

        this.getActionMap().put("upReleased", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                upState = false;
                keyReleased();
            }
        });

        // DOWN

        this.getActionMap().put("downPressed", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                downState = true;
                yMulti = 1;
                keyPressed(0, 1);
            }
        });

        this.getActionMap().put("downReleased", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                downState = false;
                keyReleased();
            }
        });
    }

    public MapDefinition getMapDefinition() {
        return mapDefinition;
    }

    public void setMapDefinition(MapDefinition mapDefinition) {
        this.mapDefinition = mapDefinition;
    }

    public void setFloor(String floorId) {

        // Clean first

        this.floorId = null;
        this.floorDefinition = null;

        if (mapDefinition != null) {
            FloorDefinition definition = mapDefinition.getFloors().get(floorId);
            if (definition != null) {
                this.floorId = floorId;
                this.floorDefinition = definition;
            }
        }

    }

    public Color getColorFor(int x, int y) {
        if (x >= 0 && x < floorDefinition.getWidth() && y >= 0 && y < floorDefinition.getHeight()) {
            TileDefinition tileDefinition = floorDefinition.getTile(x, y);
            TileDefinition sampleDefinition = null;
            if (isInsideSample(x, y)) {
                sampleDefinition = mapSampleArea.getTile(x - sampleX, y - sampleY);
            }
            if (tileDefinition == null && sampleDefinition != null) {
                return getColorForTileDefinition(sampleDefinition);
            } else if (tileDefinition != null && sampleDefinition == null) {
                return getColorForTileDefinition(tileDefinition);
            } else if (tileDefinition == null && sampleDefinition == null) {
                return Color.YELLOW;
            } else {
                if (tileDefinition.getType() == sampleDefinition.getType()) {
                    return Color.GREEN;
                } else {
                    return Color.RED;
                }
            }
        }
        return Color.YELLOW;
    }

    public Color getColorForTileDefinition(TileDefinition tile) {
        switch (tile.getType()) {
            case UNKNOWN: {

            }
            break;
            case FLOOR: {
                return Color.blue;
            }
            case EXIT: {
                return Color.red;
            }
            case DOOR: {
                return Color.CYAN;
            }
            case ENCOUNTER: {
                return Color.orange;
            }
            case START: {
                return Color.green;
            }
            case EMPTY: {
                return Color.gray;
            }
            case WALL: {
                return Color.magenta;
            }
            case FALSEFLOOR: {
                return Color.white;
            }
        }
        return Color.YELLOW;
    }

    public boolean isInsideSample(int x, int y) {
        if (mapSampleArea != null) {
            if (x >= sampleX && x < sampleX + mapSampleArea.getWidth() && y >= sampleY && y < sampleY + mapSampleArea.getHeight()) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        if (floorDefinition != null) {
            int possibleWidth = getWidth() / cellSize;
            int possibleHeight = (getHeight() - 24) / cellSize;
            if (possibleWidth > 1 && possibleHeight > 1) {
                if (mapSampleArea != null) {
                    sampleX = viewX + ((possibleWidth - mapSampleArea.getWidth()) / 2);
                    sampleY = viewY + ((possibleHeight - mapSampleArea.getHeight()) / 2);
                } else {
                    sampleX = 0;
                    sampleY = 0;
                }
                final int maxW = floorDefinition.getWidth();
                final int maxH = floorDefinition.getHeight();
                for (int y = 0; y < possibleHeight; y++)
                    for (int x = 0; x < possibleWidth; x++) {
                        final int startX = x * cellSize;
                        final int startY = y * cellSize;
                        int offX = viewX + x;
                        int offY = viewY + y;
                        Color border = isInsideSample(offX, offY) ? Color.BLUE : Color.gray;
                        if (offX >= 0 && offX < maxW && offY >= 0 && offY < maxH) {
                            Color c = getColorFor(offX, offY);
                            if (c == Color.gray) {
                                border = Color.white;
                            }
                            g2d.setColor(c);
                            g2d.fillRect(startX, startY, cellSize, cellSize);
                        } else {
                            g2d.setColor(Color.BLACK);
                            g2d.fillRect(startX, startY, cellSize, cellSize);
                        }
                        g2d.setColor(border);
                        g2d.drawRect(startX + 1, startY + 1, cellSize - 2, cellSize - 2);
                    }
            } else {
                // To small, cancel out
                g2d.setColor(Color.BLACK);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }

            g2d.setColor(Color.white);
            g2d.fillRect(0, getHeight() - 24, getWidth(), 24);

            g2d.setColor(Color.black);
            g2d.drawString(String.format("X: %d, Y: %d", viewX, viewY), 4, getHeight() - 8);

        } else {
            g2d.setColor(Color.BLACK);
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }
    }


    boolean spaceState;
    boolean upState;
    boolean downState;
    boolean leftState;
    boolean rightState;
    public int xMulti = 1;
    public int yMulti = 1;

    public void keyPressed(int x, int y) {
        if (upState || downState || leftState || rightState) {
            if (x != 0 || y != 0) {
                shift(x, y);
                repaint();
            }
            timer.start();
        } else {
            xMulti = 1;
            yMulti = 1;
            timer.stop();
        }

    }

    public void keyReleased() {
        if (upState || downState || leftState || rightState) {
            timer.start();
        } else {
            timer.stop();
        }
    }

    public void shift(int x, int y) {
        viewX += x;
        viewY += y;
    }

    public void set(int x, int y) {
        viewX = x;// - centerIndex;
        viewY = y;// - centerIndex;
    }
}
