package com.mgatelabs.piper.ui.utils;

import com.google.common.collect.Lists;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 *
 * Created by @mgatelabs (Michael Fuller) on 9/20/2017
 */
public class Constants {

    public static final String ACTION_RUN = "run";
    public static final String ACTION_EDIT = "edit";
    public static final String ACTION_CREATE = "create";
    public static final String ACTION_DELETE = "delete";

    public static final String MODE_MAP = "map";
    public static final String MODE_SCRIPT = "script";
    public static final String MODE_DEVICE = "device";
    public static final String MODE_VIEW = "view";

    public static final Pattern ID_PATTERN = Pattern.compile("^[a-zA-Z0-9-_]+$");
    public static final String PATH_DEVICES = "./devices";
    public static final String PATH_VIEWS = "./views";
    public static final String PATH_CONFIGS = "./configs";
    public static final String PATH_SCRIPTS = "./scripts";
    public static final String PATH_STATES = "./states";

    public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    public static String[] listJsonFilesIn(File dir, final boolean skipIncludes) {
        List<String> itemList = Lists.newArrayList();
        itemList.add("");
        for (File f : dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".json") && (!skipIncludes || (!name.startsWith("_") && skipIncludes));
            }
        })) {
            itemList.add(f.getName().substring(0, f.getName().length() - 5));
        }
        Collections.sort(itemList);
        String[] itemArray = new String[itemList.size()];
        itemList.toArray(itemArray);
        return itemArray;
    }

    public static String[] listFoldersFilesIn(File dir) {
        List<String> itemList = Lists.newArrayList();
        itemList.add("");
        for (File f : dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        })) {
            itemList.add(f.getName());
        }
        Collections.sort(itemList);
        String[] itemArray = new String[itemList.size()];
        itemList.toArray(itemArray);
        return itemArray;
    }

    public static List<String> arrayToList(String[] items) {
        List<String> list = Lists.newArrayList();
        for (String item : items) {
            list.add(item);
        }
        return list;
    }
}
