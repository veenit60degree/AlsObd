package com.als.obd.tools;


import java.util.ArrayList;
import java.util.List;

/**
 * @ProjectName: ELDBOX
 * @Package: com.android.eldbox
 * @ClassName: AppData getter and setter
 * @Description: AppData
 * @Author: HJH
 * @CreateDate: 11/29/2019 1:41 PM
 * @UpdateUser: 更新者：
 * @Version: 1.0
 */
public class AppData {

    private List<byte[]> dtcList = new ArrayList<byte[]>() {
    };

    private String Version = "null";

    private int SleepDelay = -1;

    public byte[] getDtcList() {
        if (dtcList.size() == 0)
            return new byte[]{0x6e, 0x3d, 0x30};//n=0
        return dtcList.get(0);
    }

    public void setDtcList(byte[] dtc) {
        this.dtcList.clear();
        this.dtcList.add(dtc);
    }

    public String getVersion() {
        return Version;
    }

    public void setVersion(String version) {
        Version = version;
    }

    public int getSleepDelay() {
        return SleepDelay;
    }

    public void setSleepDelay(int sleepDelay) {
        SleepDelay = sleepDelay;
    }
}
