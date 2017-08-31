package com.mgatelabs.ffbe;

import com.mgatelabs.ffbe.shared.mapper.*;

import java.util.*;

/**
 * @author <a href="mailto:mfuller@acteksoft.com">Michael Fuller</a>
 * Creation Date: 8/30/2017
 */
public class DungeonMapper {

  int minX;
  int minY;
  int maxX;
  int maxY;

  int x;
  int y;

  public String name;

  public List<MapTile> tiles;

  public DungeonMapper(String name) {
    x = 0;
    y = 0;

    minX = 0;
    minY = 0;

    maxX = 0;
    maxY = 0;

    this.name = name;

    this.tiles = new ArrayList<>();
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void addTile(MapTile tile) {

    int strut = tile.getArea().length / 2;

    int maxX = tile.getX() + strut;
    int minX = tile.getX() - strut;
    int maxY = tile.getY() + strut;
    int minY = tile.getY() - strut;

    if (maxX > this.maxX) {
      this.maxX = maxX;
    }

    if (maxY > this.maxY) {
      this.maxY = maxY;
    }

    if (minX < this.minX) {
      this.minX = minX;
    }

    if (minY < this.minY) {
      this.minY = minY;
    }

    this.tiles.add(tile);
  }

  public void write() {
    byte [][] area = new byte [(maxY - minY) + 1][(maxX - minX) + 1];

    for (MapTile tile: tiles) {
      int leftX = tile.getTopleftX();
      int topY = tile.getTopLeftY();

      for (int y = 0; y < tile.getArea().length; y++) {
        for (int x = 0; x < tile.getArea()[y].length; x++) {
          int fx = (-minX) + leftX + x;
          int fy = (-minY) + topY + y;
          area[fy][fx] = tile.getArea()[y][x];
        }
      }

    }

  }
}
