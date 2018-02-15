package com.mgatelabs.ffbe.shared;

import com.mgatelabs.ffbe.Runner;
import com.mgatelabs.ffbe.shared.image.Sampler;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/7/2017.
 */
public class RawMapParserTest {

    @Test
    public void test() {

        final int width = 1440;
        final int bpp = 4;
        final int dataOffset = 12;

        final int mmx = 63;
        final int mmy = 145;
        final int mmw = 486;
        final int mmh = 486;
        final int mmcw = 18;
        final int mmch = 18;

        final int columns = mmw / mmcw;
        final int rows = mmh / mmch;

        final int blockSize = mmcw;
        final int blockStartSkip = 0;
        final int blockEndSkip = 1;

        final int startOffset = getOffset(width, 12, 4, mmx, mmy);
        final int nextRowOffset = (width * bpp) - (mmw * bpp);

        FileInputStream fileInputStream = null;

        Sampler[][] grid = new Sampler[columns][rows];

        for (int y = 0; y < grid.length; y++) {
            for (int x = 0; x < grid[y].length; x++) {
                grid[y][x] = new Sampler();
            }
        }

        try {
            fileInputStream = new FileInputStream(new File(Runner.WORKING_DIRECTORY,"framebuffer-dungeon.raw"));

            fileInputStream.skip(startOffset);

            byte[] temp = new byte[3];

            int sampleCount = 0;

            int rowSkip = 6;

            int middleStart = (columns / 2) - 1;
            int middleEnd = middleStart + 2;

            for (int y = 0; y < rows; y++) {
                for (int z = 0; z < blockSize; z += rowSkip) {
                    for (int x = 0; x < columns; x++) {
                        for (int p = 0; p < blockSize; p++) {
                            if (p % 4 != 0) {
                             fileInputStream.skip(3 + blockStartSkip + blockEndSkip);
                                continue;
                            } else if (x >= middleStart && x <= middleEnd && y >= middleStart && y <= middleEnd) {
                                fileInputStream.skip(3 + blockStartSkip + blockEndSkip);
                                continue;
                            }
                            if (blockStartSkip > 0) fileInputStream.skip(blockStartSkip);
                            fileInputStream.read(temp);
                            grid[y][x].add((0xff & temp[0]), (0xff & temp[1]), (0xff & temp[2]));
                            if (blockEndSkip > 0) fileInputStream.skip(blockEndSkip);
                            if (x == 0 && y == 0) {
                                sampleCount++;
                            }
                        }
                    }
                    fileInputStream.skip(nextRowOffset);
                    fileInputStream.skip((width * bpp) * (rowSkip - 1));
                }
            }

            final float samples = sampleCount;

            for (int y = 0; y < grid.length; y++) {
                for (int x = 0; x < grid[y].length; x++) {
                    if (x >= middleStart && x <= middleEnd && y >= middleStart && y <= middleEnd) {
                        System.out.print("?");
                        continue;
                    }
                    if ((grid[y][x].getR() / (samples)) > 128) {
                        System.out.print("R");
                    } else if ((grid[y][x].getG() / (samples)) > 128) {
                        System.out.print("G");
                    } else if ((grid[y][x].getB() / (samples)) > 128) {
                        System.out.print("B");
                    } else {
                        System.out.print("#");
                    }
                }
                System.out.println();
            }


        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            com.mgatelabs.ffbe.shared.util.Closer.close(fileInputStream);
        }

    }

    public int getOffset(int width, int offset, int bpp, int x, int y) {
        return offset + ((width * y * bpp) + (x * bpp));
    }

}
