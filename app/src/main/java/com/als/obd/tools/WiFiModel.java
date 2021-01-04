package com.als.obd.tools;

/**
 * Created by kumar on 3/15/2018.
 */

public class WiFiModel {
    String networkSSID;
    String networkPass;


    public WiFiModel(String networkSSID, String networkPass) {
        this.networkSSID = networkSSID;
        this.networkPass = networkPass;
    }

    public String getNetworkSSID() {
        return networkSSID;
    }

    public String getNetworkPass() {
        return networkPass;
    }
}
