package com.disorderlylabs.inventory.faultInjection;

public class Fault {
    public enum FAULT_TYPES {DELAY, DROP_PACKET}

    public static String SEQ_DELIM = ";";
    public static String FIELD_DELIM = ":";

    //maybe constructor will take in service names

    //more complex fault handling should be done in this class
}
