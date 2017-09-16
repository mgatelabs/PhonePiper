package com.mgatelabs.ffbe.shared.mapper;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/15/2017.
 */
public class TileDefinitionTest {
    @Test
    public void calculate() throws Exception {

        // Make sure the Neighbor tile calculation is working

        TileDefinition[][] map = new TileDefinition[3][3];

        map[0][0] = new TileDefinition();
        map[0][1] = new TileDefinition();
        map[0][2] = new TileDefinition();
        map[1][0] = new TileDefinition();
        map[1][1] = new TileDefinition();
        map[1][2] = new TileDefinition();
        map[2][0] = new TileDefinition();
        map[2][1] = new TileDefinition();
        map[2][2] = new TileDefinition();

        for (int y = 0; y < 3; y++)
        for (int x = 0; x < 3; x++){
            map[y][x].setType(TileType.FLOOR);
        }

        // All Bits
        Assert.assertEquals(511, TileDefinition.calculate(map, 1, 1));

        map[0][0].setType(TileType.WALL);

        // One corner
        Assert.assertEquals(510, TileDefinition.calculate(map, 1, 1));

        map[0][2].setType(TileType.WALL);

        // Two Corners
        Assert.assertEquals(506, TileDefinition.calculate(map, 1, 1));

        map[2][2].setType(TileType.WALL);

        // Three Corners
        Assert.assertEquals(250, TileDefinition.calculate(map, 1, 1));
    }

}