package com.megajohnny.a2a;

public enum DisconnectReason {
    VOLUNTARY, TIMEOUT, READ_ERROR, UNKNOWN;
    public static final String[] reasonNames = {"Quit", "Timeout", "Read error", "Unknown"};
}