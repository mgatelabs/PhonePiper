package com.mgatelabs.ffbe.shared.util;

import com.mgatelabs.ffbe.shared.details.ActionType;
import com.mgatelabs.ffbe.shared.details.ComponentDefinition;
import com.mgatelabs.ffbe.shared.image.ImageWrapper;
import com.mgatelabs.ffbe.shared.image.RawImageWrapper;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.SecureRandom;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/4/2017.
 */
public class AdbUtils {

    private static final SecureRandom RANDOM = new SecureRandom();

    public static void component(ComponentDefinition componentDefinition, ActionType type, AdbShell shell) {
        switch (type) {
            case TAP: {
                final int x = getStartX(componentDefinition, type);
                final int y = getStartY(componentDefinition, type);
                shell.exec("input tap " + x + " " + y);
            }
            break;
            case SWIPE_DOWN:
            case SWIPE_RIGHT:
            case SWIPE_LEFT:
            case SWIPE_UP: {
                final int x1 = getStartX(componentDefinition, type);
                final int y1 = getStartY(componentDefinition, type);
                final int x2 = getEndX(componentDefinition, type);
                final int y2 = getEndY(componentDefinition, type);
                shell.exec("input swipe " + x1 + " " + y1 + " "  + x2 + " " + y2 + " 100");
            } break;
            default: {
                System.out.println("Unhandled component command: " + type.name());
            } break;
        }
    }

    private static int getStartX(ComponentDefinition componentDefinition, ActionType type) {
        int x = componentDefinition.getX();
        switch (type) {
            case SWIPE_LEFT: {
                x += (componentDefinition.getW() - (componentDefinition.getW() / 8));
            } break;
            case SWIPE_RIGHT: {
                x += (componentDefinition.getW() / 8);
            } break;
            case SWIPE_UP:
            case SWIPE_DOWN: {
                x += componentDefinition.getW() / 2;
            } break;
            default: {
                x += RANDOM.nextInt(componentDefinition.getW());
            } break;
        }
        return x;
    }

    private static int getEndX(ComponentDefinition componentDefinition, ActionType type) {
        int x = componentDefinition.getX();
        switch (type) {
            case SWIPE_RIGHT: {
                x += (componentDefinition.getW() - (componentDefinition.getW() / 8));
            } break;
            case SWIPE_LEFT: {
                x += (componentDefinition.getW() / 8);
            } break;
            default: {
                return getStartX(componentDefinition, type);
            }
        }
        return x;
    }

    private static int getStartY(ComponentDefinition componentDefinition, ActionType type) {
        int y = componentDefinition.getY();
        switch (type) {
            case SWIPE_UP: {
                y += (componentDefinition.getH() - (componentDefinition.getH() / 8));
            } break;
            case SWIPE_DOWN: {
                y += (componentDefinition.getH() / 8);
            } break;
            case SWIPE_LEFT:
            case SWIPE_RIGHT: {
                y += componentDefinition.getH() / 2;
            } break;
            default: {
                y += RANDOM.nextInt(componentDefinition.getH());
            } break;
        }
        return y;
    }

    private static int getEndY(ComponentDefinition componentDefinition, ActionType type) {
        int y = componentDefinition.getY();
        switch (type) {
            case SWIPE_DOWN: {
                y += (componentDefinition.getH() - (componentDefinition.getH() / 8));
            } break;
            case SWIPE_UP: {
                y += (componentDefinition.getH() / 8);
            } break;
            default: {
                return getStartY(componentDefinition, type);
            }
        }
        return y;
    }

    public static void persistScreen(AdbShell shell) {
        shell.exec("screencap /mnt/sdcard/framebuffer.raw");
    }

    public static ImageWrapper getScreen() {
        byte[] bytes = execStream("adb exec-out screencap");
        try {
            int w, h;
            if (bytes.length > 12) { // Sanity
                ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
                byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
                w = byteBuffer.getInt();
                h = byteBuffer.getInt();
            } else {
                w = 0;
                h = 0;
            }
            return new RawImageWrapper(w, h, RawImageWrapper.ImageFormats.RGBA, 12, bytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static boolean execWait(final String command) {
        try {
            Process myProcess = Runtime.getRuntime().exec(command);
            myProcess.waitFor();
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private static byte[] execStream(final String command) {
        try {
            Process myProcess = Runtime.getRuntime().exec(command);
            return repair(myProcess.getInputStream());
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private static byte[] repair(InputStream bytesIn) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int value;
        int temp = 0;
        boolean useTemp = false;
        BufferedInputStream bufferedInputStream = new BufferedInputStream(bytesIn);
        while ((value = bufferedInputStream.read()) != -1) {
            if (useTemp && value == 0x0A) {
                baos.write(value);
                useTemp = false;
                continue;
            } else if (useTemp) {
                baos.write(temp);
                useTemp = false;
            }
            if (value == 0x0d) {
                useTemp = true;
                temp = value;
            } else {
                baos.write(value);
            }
        }
        baos.close();
        return baos.toByteArray();
    }
}
