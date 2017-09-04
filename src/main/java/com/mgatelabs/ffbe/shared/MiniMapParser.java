package com.mgatelabs.ffbe.shared;

import com.mgatelabs.ffbe.shared.image.RawImageWrapper;

/**
 * Created by @mgatelabs (Michael Fuller) on 8/29/2017.
 */
public class MiniMapParser {

    /**
     * Example the mini-map and build a small image form it
     * @param rawImage
     * @param miniMapComponent
     * @param miniMapCenterComponent
     * @return
     */
    public static byte [][] parseMap(RawImageWrapper rawImage, ComponentDetail miniMapComponent, ComponentDetail miniMapCenterComponent) {

        int rows = miniMapComponent.getW() / miniMapCenterComponent.getW();
        int columns = miniMapComponent.getH() / miniMapCenterComponent.getH();

        if (rows % 2 == 0 || columns % 2 == 0) {
            throw new RuntimeException("Calculated Map size must be uneven");
        }

        int halfRows = rows / 2;
        int halfColumns = columns / 2;

        int deadRowStart = halfRows;
        int deadRowEnd = halfRows + 2;

        int deadColumnStart = halfColumns;
        int deadColumnEnd = halfColumns + 2;

        byte [][] result = new byte [rows][columns];

        ColorSample sample = new ColorSample();

        final int sampleStart = -3;
        final int sampleEnd = 3;


        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < columns; x++) {
                if (x >= deadRowStart && x <= deadRowEnd && y >= deadColumnStart && y <= deadColumnEnd) {
                    result[y][x] = '?';
                    continue;
                }

                int px = miniMapComponent.getX() + (x * miniMapCenterComponent.getW()) + (miniMapCenterComponent.getW() / 2);
                int py = miniMapComponent.getY() + (y * miniMapCenterComponent.getH()) + (miniMapCenterComponent.getH() / 2);

                result[y][x] = '#';

                short validSamples = 0;

                for (int sx = sampleStart; sx <= sampleEnd; sx++) {
                    for (int sy = sampleStart; sy <= sampleEnd; sy++) {
                        sample.parse(rawImage.getPixel(px + sx, py + sy));
                        if (sample.getG() > 128 || sample.getB() > 128 || sample.getR() > 128) {
                            validSamples++;
                        }
                    }
                }

                if (validSamples > 7) {
                    result[y][x] = '-';
                }
            }
        }

        return result;
    }

}
