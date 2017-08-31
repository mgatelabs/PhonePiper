package com.mgatelabs.ffbe.shared.mapper;

/**
 * @author <a href="mailto:mfuller@acteksoft.com">Michael Fuller</a>
 * Creation Date: 8/30/2017
 */
public class MapTile {
  byte [][] area;
  int x;
  int y;

  public MapTile(byte[][] area, int x, int y) {
    this.area = area;
    this.x = x;
    this.y = y;
  }

  public int getTopleftX() {
    return this.x - ((this.area.length / 2));
  }

  public int getTopLeftY() {
    return this.y + ((this.area.length / 2));
  }

  public byte[][] getArea() {
    return area;
  }

  public void setArea(byte[][] area) {
    this.area = area;
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
}
