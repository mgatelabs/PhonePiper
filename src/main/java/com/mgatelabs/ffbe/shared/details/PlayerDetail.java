package com.mgatelabs.ffbe.shared.details;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mgatelabs.ffbe.shared.util.JsonTool;

import java.io.File;
import java.io.IOException;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/4/2017.
 */
public class PlayerDetail {

    public static final int MIN_ENERGY = 1;
    public static final int MAX_ENERGY = 165;
    public static final int MAX_LEVEL = 150;

    private int level;
    private String ip;

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    @JsonIgnore
    public int getTotalEnergy() {
        int energy = 40;

        if (level >= 100) {
            energy += 100;
            energy += (level - 100) / 2;
        } else {
            energy += level;
        }
        return energy;
    }

    public static PlayerDetail read() {
        File playerFile = new File("player.json");
        if (playerFile.exists()) {
            ObjectMapper objectMapper = JsonTool.INSTANCE;
            try {
                return objectMapper.readValue(playerFile, PlayerDetail.class);
            } catch (JsonParseException e) {
                e.printStackTrace();
            } catch (JsonMappingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new PlayerDetail();
    }

    public boolean write() {
        File playerFile = new File("player.json");
        ObjectMapper objectMapper = JsonTool.INSTANCE;
        try {
            objectMapper.writeValue(playerFile, this);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
