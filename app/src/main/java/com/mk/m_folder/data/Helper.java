package com.mk.m_folder.data;

public class Helper {

    public static String disableExtension (String str) {
        // Handle null case specially.
        if (str == null) return null;

        // Get position of last '.'.
        int pos = str.lastIndexOf(".");

        // If there wasn't any '.' just return the string as is.
        if (pos == -1) return str;

        // Otherwise return the string, up to the dot.
        return str.substring(0, pos);
    }

}
