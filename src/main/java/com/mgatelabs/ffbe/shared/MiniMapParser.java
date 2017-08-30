package com.mgatelabs.ffbe.shared;

import java.awt.image.BufferedImage;

/**
 * Created by @mgatelabs (Michael Fuller) on 8/29/2017.
 */
public class MiniMapParser {

    /**
     * Example the mini-map and build a small image form it
     * @param bufferedImage
     * @param miniMapComponent
     * @param miniMapCenterComponent
     * @return
     */
    public static byte [][] parseMap(BufferedImage bufferedImage, ComponentDetail miniMapComponent, ComponentDetail miniMapCenterComponent) {

        int rows = miniMapComponent.getW() / miniMapCenterComponent.getW();
        int columns = miniMapComponent.getH() / miniMapCenterComponent.getH();

        int halfRows = rows / 2;
        int halfColumns = columns / 2;

        int deadRowStart = halfRows - 1;
        int deadRowEnd = halfRows + 1;

        int deadColumnStart = halfColumns - 1;
        int deadColumnEnd = halfColumns + 1;

        byte [][] result = new byte [rows][columns];

        ColorSample sample = new ColorSample();

        final int sampleStart = -3;
        final int sampleEnd = 3;


        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < columns; x++) {
                if (x >= deadRowStart && x <= deadRowEnd && y >= deadColumnStart && y <= deadColumnEnd) {
                    result[y][x] = 9;
                    continue;
                }


                int px = miniMapComponent.getX() + (x * miniMapCenterComponent.getW()) + (miniMapCenterComponent.getW() / 2);
                int py = miniMapComponent.getY() + (y * miniMapCenterComponent.getH()) + (miniMapCenterComponent.getH() / 2);

                result[y][x] = 0;

                short validSamples = 0;

                for (int sx = sampleStart; sx <= sampleEnd; sx++) {
                    for (int sy = sampleStart; sy <= sampleEnd; sy++) {
                        sample.parse(bufferedImage.getRGB(px + sx, py + sy));
                        if (sample.getG() > 128 || sample.getB() > 128 || sample.getR() > 128) {
                            validSamples++;
                        }
                    }
                }

                if (validSamples > 7) {
                    result[y][x] = 1;
                }
            }
        }

        return result;
    }

}
