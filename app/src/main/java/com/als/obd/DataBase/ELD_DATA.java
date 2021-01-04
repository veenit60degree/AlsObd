package com.als.obd.DataBase;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

/**
 *
 * @ProjectName:
 * @Package:        com.android.eldbox.DataBase
 * @ClassName:      ELD_DATA.java
 * @Description:    ROOM　Entity for ELD Data
 * @Author:         HGY
 * @UpdateUser:     HJH
 * @UpdateDate:
 * @UpdateRemark:
 * @Version:        1.0
 */
@Entity(indices = @Index("TIME_STAMP"))
public class ELD_DATA {
    @ColumnInfo
    public int RPM           = 0;//转速 Rotating speed
    @ColumnInfo
    public int VSS           = 0;//车速 Vehicle Speed
    @PrimaryKey
    @ColumnInfo
    public long TIME_STAMP    = 0;//时间戳。做主键用 Timestamp. Use as the primary key
    @ColumnInfo
    public int Trip_Distance = 0;//短里程 Trip distance
    @ColumnInfo
    public int Odometer      = 0;//总里程 odometer
    @ColumnInfo
    public double ENGINE_Hours  = 0;//引擎工作时间 Engine hours
    @ColumnInfo(typeAffinity = ColumnInfo.INTEGER)
    public long High_Precision_Odometer = 0;//高精度总里程，仅1939可用 HighPrecisionOdometer，only J1939
}
