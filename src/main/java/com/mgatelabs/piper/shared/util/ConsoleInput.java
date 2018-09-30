package com.mgatelabs.piper.shared.util;

import java.util.Scanner;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/1/2017 for Phone-Piper
 */
public class ConsoleInput {

    public static int getInt() {
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String value = scanner.nextLine();
            try {
                return Integer.parseInt(value);
            } catch (Exception ex) {
                System.out.println("Invalid integer input: " + value);
            }
        }
        return -1;
    }

    public static String getString() {
        Scanner scanner = new Scanner(System.in);
        if (scanner.hasNextLine()) {
            return scanner.nextLine();
        }
        return "";
    }

    public static boolean yesNo() {
        String resp = ConsoleInput.getString();
        if (resp.isEmpty() || !resp.startsWith("y")) {
            return false;
        }
        return true;
    }

}
