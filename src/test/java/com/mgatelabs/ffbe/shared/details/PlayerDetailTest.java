package com.mgatelabs.ffbe.shared.details;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/4/2017.
 */
public class PlayerDetailTest {
    @Test
    public void getTotalEnergy() throws Exception {
        PlayerDetail detail = new PlayerDetail();

        // Maximum
        detail.setLevel(150);
        Assert.assertEquals(165, detail.getTotalEnergy());

        // Sample
        detail.setLevel(98);
        Assert.assertEquals(138, detail.getTotalEnergy());
    }
}