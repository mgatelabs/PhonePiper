package com.mgatelabs.ffbe.shared.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSet;
import okhttp3.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Set;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/7/2017.
 */
public class HelperUtils {

    private String ipAddress;
    private ObjectMapper objectMapper;
    private final OkHttpClient client;

    private int failures;

    public HelperUtils(String ipAddress) {
        this.ipAddress = ipAddress;
        objectMapper = new ObjectMapper();
        client = new OkHttpClient();
        failures = 0;
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
                .url("http://"+ipAddress+":8080/setup").post(RequestBody.create(MediaType.parse("application/json"), arrayOutputStream.toByteArray()))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            HelperStatus helperStatus = objectMapper.readValue(response.body().byteStream(), HelperStatus.class);
            if (helperStatus.getStatus() == HelperStatus.Status.FAIL) {
                System.out.println(helperStatus.getMsg());
                failures++;
                return false;
            }
            failures = 0;
            return helperStatus.getStates() != null;
        } catch (IOException ioEx) {
            ioEx.printStackTrace();
            failures++;
            return false;
        }
    }

    public Set<String> check(String menu) {
        Request request = new Request.Builder()
                .url("http://"+ipAddress+":8080/check/" + menu).get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            HelperStatus helperStatus = objectMapper.readValue(response.body().byteStream(), HelperStatus.class);
            if (helperStatus.getStatus() == HelperStatus.Status.FAIL) {
                System.out.println(helperStatus.getMsg());
                failures++;
                return ImmutableSet.of();
            }
            failures = 0;
            return ImmutableSet.copyOf(helperStatus.getScreens());
        } catch (IOException ioEx) {
            failures++;
            ioEx.printStackTrace();
            return ImmutableSet.of();
        }
    }

    public int [] pixel(int offset) {
        Request request = new Request.Builder()
                .url("http://"+ipAddress+":8080/pixel/" + offset).get()
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            HelperStatus helperStatus = objectMapper.readValue(response.body().byteStream(), HelperStatus.class);
            if (helperStatus.getStatus() == HelperStatus.Status.FAIL) {
                System.out.println(helperStatus.getMsg());
                failures++;
                return null;
            }
            failures = 0;
            return helperStatus.getPixels();
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