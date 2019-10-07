package com.mk.m_folder.data;

public class Temp {
    private static final Temp ourInstance = new Temp();

    public static Temp getInstance() {
        return ourInstance;
    }

    private Temp() {
    }
}
