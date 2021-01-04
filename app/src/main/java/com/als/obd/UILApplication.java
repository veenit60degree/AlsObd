/*******************************************************************************
 * Copyright 2011-2013 Sergey Tarasevich
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.als.obd;

import android.app.Application;
import android.content.Context;
import android.os.StrictMode;
import android.arch.persistence.room.Room;
import android.support.multidex.MultiDex;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.WindowManager;


import com.als.obd.DataBase.DataBase;
import com.als.obd.tools.AppData;
import com.android.eldbox_api.BleService;
import com.android.eldbox_api.SerialAgent;
import com.hjq.toast.ToastUtils;

public class UILApplication extends Application {




	//isBleELD true
	public BleService bleService;
	//whether bleservice bind
	private Boolean isBleBind = false;
	//isBleELD false
	public SerialAgent serialAgent;

	public Boolean isWaitDataSync = false;

	public Boolean showAsJ1939 = true;

	public DataBase dataBase;

	public AppData mAppData;



	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(base);
		MultiDex.install(this);
	}




	@Override
	public void onCreate() {
		super.onCreate();

		//dataBase = Room.databaseBuilder(getApplicationContext(), DataBase.class, "data").build();

		//升级数据库addMigrations(MIGRATION_1_2)
		dataBase = Room.databaseBuilder(getApplicationContext(), DataBase.class, "data")
				.addMigrations(DataBase.MIGRATION_1_2)
				.addMigrations(DataBase.MIGRATION_2_3)
				.build();

		mAppData = new AppData();//for get and set app data


		StrictMode.ThreadPolicy old = StrictMode.getThreadPolicy();
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder(old)
				.permitDiskWrites()
				.build());
		//doCorrectStuffThatWritesToDisk();
		StrictMode.setThreadPolicy(old);


		ToastUtils.init(this);
		ToastUtils.setGravity(Gravity.CENTER_HORIZONTAL,0,initScreenHeight()/4);


     /*   if(isBleELD) {
            Intent intent = new Intent();
            intent.setClass(this, BleService.class);
            startService(intent);
            isBleBind = bindService(intent, bleServiceConnection, Context.BIND_AUTO_CREATE);
        }*/
	}


	private int initScreenHeight() {
		DisplayMetrics metrics = new DisplayMetrics();
		WindowManager windowManager = (WindowManager)
				this.getSystemService(Context.WINDOW_SERVICE);
		windowManager.getDefaultDisplay().getMetrics(metrics);
		int myScreenHeight = metrics.heightPixels;
		return myScreenHeight;
	}


	//--------------Bluetooth ELD-------------------
  /*  private ServiceConnection bleServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (bleService == null) {
                bleService = ((BleService.LocalBinder) service).getService();
                bleService.startAutoConnect();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bleService = null;
        }
    };
*/

/*

    public void unBindAndStopService() {
        bleService.stopAutoConnect();
        if(isBleBind)
            unbindService(bleServiceConnection);
        stopService(new Intent(getApplicationContext(), BleService.class));
    }

    public BleService getService() {
        return bleService;
    }
*/

	//--------------Serial ELD-------------------

	public void disConnectSerial() {
		serialAgent.close();
		serialAgent = null;
	}

	public SerialAgent createSerialAgent() {
		serialAgent = new SerialAgent();
		return serialAgent;
	}


}