package com.mgatelabs.piper.shared.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSet;
import com.mgatelabs.piper.shared.image.ImageWrapper;
import com.mgatelabs.piper.shared.util.AdbUtils;
import com.mgatelabs.piper.shared.util.JsonTool;
import okhttp3.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Set;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/7/2017 for Phone-Piper
 */
public class DeviceHelper {

    private String ipAddress;
    private final ObjectMapper objectMapper;
    private final OkHttpClient client;

    private int failures;

    public DeviceHelper(String ipAddress) {
        this.ipAddress = ipAddress;
        objectMapper = JsonTool.getInstance();
        client = new OkHttpClient();
        failures = 0;
    }

    public boolean ready() {
        return this.ipAddress != null && ipAddress.length() > 5;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public boolean setup(InfoTransfer info) {

        ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();

        try {
            objectMapper.writeValue(arrayOutputStream, info);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        Request request = new Request.Builder()
                .url("http://" + ipAddress + ":8080/setup").post(RequestBody.create(MediaType.parse("application/json"), arrayOutputStream.toByteArray()))
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

    public Set<String> check(String menu) {
        Request request = new Request.Builder()
                .url("http://" + ipAddress + ":8080/check/" + menu).get()
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

    public int[] pixel(int offset) {
        Request request = new Request.Builder()
                .url("http://" + ipAddress + ":8080/pixel/" + offset).get()
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

    public ImageWrapper download() {
        Request request = new Request.Builder()
                .url("http://" + ipAddress + ":8080/download").post(RequestBody.create(MediaType.parse("text/plain"), new byte[0]))
                .build();
        try (Response response = client.newCall(request).execute()) {
            return AdbUtils.getScreenFrom(response.body().bytes());
        } catch (IOException ioEx) {
            ioEx.printStackTrace();
            failures++;
            return null;
        }
    }

    public int getFailures() {
        return failures;
    }
}
