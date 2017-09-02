package com.mgatelabs.ffbe;

import com.mgatelabs.ffbe.shared.GameState;
import com.mgatelabs.ffbe.shared.Phone;
import com.mgatelabs.ffbe.shared.SamplePoint;
import com.mgatelabs.ffbe.shared.ScreenDetail;
import org.junit.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.Buffer;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:mfuller@acteksoft.com">Michael Fuller</a>
 * Creation Date: 8/31/2017
 */
public class DungeonMapperTest {
  @Test
  public void write() throws Exception {

    BufferedImage bufferedImage = ImageIO.read(new File("F:\\Investigate\\FFBE\\pieces\\20170830_063631.png"));

    GameRunner runner = new GameRunner();

    Phone phone = runner.loadPhone("axon7");

    ScreenDetail screenDetail = phone.getScreens().get("daily-complete");

    for (SamplePoint samplePoint : screenDetail.getPoints()) {
      for (int y = -5; y <= 5; y++) {
        for (int x = -5; x <=5 ; x++) {
          bufferedImage.setRGB(samplePoint.getX() + x, samplePoint.getY() + y, 0xffff00ff);
        }
      }
    }

    ImageIO.write(bufferedImage, "PNG", new File("F:\\Investigate\\FFBE\\pieces\\20170830_063631_PIXELS.png"));
  }

}