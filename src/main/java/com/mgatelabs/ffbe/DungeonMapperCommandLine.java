package com.mgatelabs.ffbe;

import com.mgatelabs.ffbe.shared.*;
import com.mgatelabs.ffbe.shared.mapper.*;

import java.io.*;
import java.util.*;

/**
 * @author <a href="mailto:mfuller@acteksoft.com">Michael Fuller</a>
 * Creation Date: 8/30/2017
 */
public class DungeonMapperCommandLine {

  private static final File OutputImage = new File("mapper.png");

  private DungeonMapper mapper;
  private Phone phone;
  private int x;
  private int y;

  public DungeonMapperCommandLine(Phone phone) {
    this.mapper = new DungeonMapper("untitled");
    this.phone = phone;
  }

  public void run() {

    /*

    ComponentDetail mapPosition = phone.getComponents().get("mini-map");
    ComponentDetail centerPosition = phone.getComponents().get("mini-map-center");

    // Get Name
    System.out.print("Dungeon Name:");
    mapper.setName(getString());

    // Start Loop
    int command = 0;
    while (command != 99) {
      System.out.println("Position:" + x + " : " + y);
      System.out.println("Commands:");
      System.out.println("0: Move & Snap");
      System.out.println("1: New Segment");
      System.out.println("2: Write");
      System.out.println("99: Stop");
      System.out.print("Command: ");

      command = getInt();

      switch (command) {
        case 0: {
          System.out.print("X Offset: ");
          int offX = getInt();
          System.out.print("Y Offset: ");
          int offY = getInt();

          x += offX;
          y += offY;

          byte [][] area = MiniMapParser.parseMap(GameRunner.getScreen(), mapPosition, centerPosition);

          MapTile tile = new MapTile(area, x, y);

          mapper.addTile(tile);

        } break;
        case  1: {

        } break;
        case 2: {
          mapper.write();
        } break;
        case 99: {
          // Stop
        } break;
      }

    }
    */
  }

  public int getInt() {
    Scanner scanner = new Scanner(System.in);
    while (scanner.hasNextLine()) {
      String value = scanner.next();
      try {
        return Integer.parseInt(value);
      } catch (Exception ex) {
        System.out.println("Invalid Integer Input: " + value);
      }
    }
    return -1;
  }

  public String getString() {
    Scanner scanner = new Scanner(System.in);
    if (scanner.hasNextLine()) {
      return scanner.next();
    }
    return "";
  }
}
