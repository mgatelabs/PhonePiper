package com.mgatelabs.ffbe.shared;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Created by @mgatelabs (Michael Fuller) on 8/29/2017.
 */
public class ImageUtil {
    public static BufferedImage readImage(final File file) {
        try {
            BufferedImage bufferedImage = ImageIO.read(file);
            return bufferedImage;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
