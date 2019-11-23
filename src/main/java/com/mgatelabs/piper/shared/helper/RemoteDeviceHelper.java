package com.mgatelabs.piper.shared.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSet;
import com.mgatelabs.piper.runners.ScriptRunner;
import com.mgatelabs.piper.shared.details.ConnectionDefinition;
import com.mgatelabs.piper.shared.image.ImageWrapper;
import com.mgatelabs.piper.shared.util.AdbUtils;
import com.mgatelabs.piper.shared.util.AdbWrapper;
import com.mgatelabs.piper.shared.util.JsonTool;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/7/2017 for Phone-Piper
 */
public class RemoteDeviceHelper implements DeviceHelper {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private ConnectionDefinition connectionDefinition;
    private final ObjectMapper objectMapper;
    private final OkHttpClient client;

    private int failures;

    public RemoteDeviceHelper(ConnectionDefinition connectionDefinition) {
        this.connectionDefinition = connectionDefinition;
        objectMapper = JsonTool.getInstance();
        client = new OkHttpClient();
        failures = 0;
    }

    @Override
    public boolean ready() {
        return connectionDefinition.getIp() != null && connectionDefinition.getIp().length() > 5;
    }

    @Override
    public String getIpAddress() {
        return connectionDefinition.getIp();
    }

    @Override
    public void setConnectionDefinition(ConnectionDefinition connectionDefinition) {
        this.connectionDefinition = connectionDefinition;
    }

    @Override
    public boolean setup(InfoTransfer info) {

        ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();

        try {
            objectMapper.writeValue(arrayOutputStream, info);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        Request request = new Request.Builder()
                .url("http://" + connectionDefinition.getIp() + ":" + connectionDefinition.getHelperPort() + "/setup").post(RequestBody.create(MediaType.parse("application/json"), arrayOutputStream.toByteArray()))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            DeviceStatus deviceStatus = objectMapper.readValue(response.body().byteStream(), DeviceStatus.class);
            if (deviceStatus.getStatus() == DeviceStatus.Status.FAIL) {
                System.out.println(deviceStatus.getMsg());
                failures++;
                return false;
            }
            failures = 0;
            return deviceStatus.getStates() != null;
        } catch (IOException ioEx) {
            ioEx.printStackTrace();
            failures++;
            return false;
        }
    }

    @Override
    public Set<String> check(String menu) {

        logger.debug("Helper: /check/" + menu);

        Request request = new Request.Builder()
                .url("http://" + connectionDefinition.getIp() + ":" + connectionDefinition.getHelperPort() + "/check/" + menu).get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            //if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            DeviceStatus deviceStatus = objectMapper.readValue(response.body().byteStream(), DeviceStatus.class);
            if (deviceStatus.getStatus() == DeviceStatus.Status.FAIL) {
                System.out.println(deviceStatus.getMsg());
                failures++;
                return ImmutableSet.of();
            }
            failures = 0;
            return ImmutableSet.copyOf(deviceStatus.getScreens());
        } catch (IOException ioEx) {
            failures++;
            ioEx.printStackTrace();
            return ImmutableSet.of();
        }
    }

    @Override
    public int[] pixel(int offset) {
        Request request = new Request.Builder()
                .url("http://" + connectionDefinition.getIp() + ":" + connectionDefinition.getHelperPort() + "/pixel/" + offset).get()
                .build();
        try (Response response = client.newCall(request).execute()) {
            //if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            DeviceStatus deviceStatus = objectMapper.readValue(response.body().byteStream(), DeviceStatus.class);
            if (deviceStatus.getStatus() == DeviceStatus.Status.FAIL) {
                System.out.println(deviceStatus.getMsg());
                failures++;
                return null;
            }
            failures = 0;
            return deviceStatus.getPixels();
        } catch (IOException ioEx) {
            ioEx.printStackTrace();
            failures++;
            return null;
        }
    }

    @Override
    public ImageWrapper download() {
        Request request = new Request.Builder()
                .url("http://" + connectionDefinition.getIp() + ":" + connectionDefinition.getHelperPort() + "/download").post(RequestBody.create(MediaType.parse("text/plain"), new byte[0]))
                .build();
        try (Response response = client.newCall(request).execute()) {
            return AdbUtils.getScreenFrom(response.body().bytes());
        } catch (IOException ioEx) {
            ioEx.printStackTrace();
            failures++;
            return null;
        }
    }

    @Override
    public int getFailures() {
        return failures;
    }

    @Override
    public boolean refresh(AdbWrapper shell) {

        long startTime = System.nanoTime();

        if (!AdbUtils.persistScreen(shell, false)) {
            logger.warn("Helper Image Failure");
            waitFor(250);
            return false;
        }
        long endTime = System.nanoTime();
        long dif = endTime - startTime;

        //lastImageDate = new Date();
        float lastImageDuration = ((float) dif / 1000000000.0f);
        logger.debug("Helper Image Persisted in " + ScriptRunner.THREE_DECIMAL.format(lastImageDuration) + "s");

        return true;
    }

    @Override
    public DeviceHelper makeReady(AdbWrapper shell) {
        return this;
    }

    private void waitFor(long milli) {
        try {
            final long startTime = System.nanoTime();
            final long endTime = startTime + TimeUnit.MILLISECONDS.toNanos(milli);

            while (true) {
                if (System.nanoTime() >= endTime) return;
                Thread.sleep(25);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
