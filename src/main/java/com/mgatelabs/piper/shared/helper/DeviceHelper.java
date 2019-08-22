package com.mgatelabs.piper.shared.helper;

import com.mgatelabs.piper.shared.details.ConnectionDefinition;
import com.mgatelabs.piper.shared.image.ImageWrapper;
import com.mgatelabs.piper.shared.util.AdbShell;

import java.util.Set;

/**
 * Created by @mgatelabs (Michael Fuller) on 8/21/2019 for Phone-Piper.
 */
public interface DeviceHelper {
    boolean ready();

    String getIpAddress();

    void setConnectionDefinition(ConnectionDefinition connectionDefinition);

    boolean setup(InfoTransfer info);

    Set<String> check(String menu);

    int[] pixel(int offset);

    ImageWrapper download();

    int getFailures();

    boolean refresh(AdbShell shell);
}
