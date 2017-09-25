package com.mgatelabs.ffbe.shared.details;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/4/2017.
 */
public class PlayerDefinitionTest {
    @Test
    public void getTotalEnergy() throws Exception {
        PlayerDefinition detail = new PlayerDefinition();

        // Maximum
        detail.setLevel(150);
        Assert.assertEquals(165, detail.getTotalEnergy());

        // Sample
        detail.setLevel(98);
        Assert.assertEquals(138, detail.getTotalEnergy());
    }
}