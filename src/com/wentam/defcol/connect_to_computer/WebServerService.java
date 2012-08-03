//////////////////////////////////////////////////////////////////////////////
// Copyright 2012 Matthew Egeler
// 									       
// Licensed under the Apache License, Version 2.0 (the "License");	       
// you may not use this file except in compliance with the License.	       
// You may obtain a copy of the License at				      
// 									       
//     http://www.apache.org/licenses/LICENSE-2.0			       
// 									       
// Unless required by applicable law or agreed to in writing, software      
// distributed under the License is distributed on an "AS IS" BASIS,	       
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
// See the License for the specific language governing permissions and      
// limitations under the License.					      
//////////////////////////////////////////////////////////////////////////////

package com.wentam.defcol.connect_to_computer;

import com.wentam.defcol.R;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import java.lang.Thread;
import java.lang.Runnable;

import android.app.NotificationManager;
import android.app.Notification;
import android.app.PendingIntent;

import android.content.Context;

import android.content.Intent;

import android.os.Bundle;


import android.net.wifi.*;
public class WebServerService extends Service {
    private Context context = this;

    private WebServer server = null;
    private String jquery;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
	Bundle extras = intent.getExtras();
	jquery = extras.getString("jquery");

	server = new WebServer(this, jquery);

	this.startForeground(3331, this.createNotification());

	server.startServer();
		
	return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
	super.onCreate();
	this.startForeground(3331, this.createNotification());
    }

    @Override
    public void onDestroy() {
	server.stopServer();
    }

    @Override
    public IBinder onBind(Intent intent) {
	return null;
    }

    private Notification createNotification() {
	String ip = getIpAddress();

	int icon = R.drawable.icon;
	CharSequence tickerText = "Connect with "+ip+":8910";
	long when = System.currentTimeMillis();
	final Notification notification = new Notification(icon, tickerText, when);

	CharSequence contentTitle = "DefCol computer connection";
	CharSequence contentText = "Go to "+ip+":8910 in your browser";
	Intent notificationIntent = new Intent(this, WebInterfaceActivity.class);
	notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
	PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

	notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);

	notification.flags = Notification.FLAG_ONGOING_EVENT;

	return notification;
    }

    private String getIpAddress() {
	WifiInfo winfo = ((WifiManager) this.getSystemService(Context.WIFI_SERVICE) ).getConnectionInfo();
	
	int myIp = winfo.getIpAddress();

	int intMyIp3 = myIp/0x1000000;
	int intMyIp3mod = myIp%0x1000000;
      
	int intMyIp2 = intMyIp3mod/0x10000;
	int intMyIp2mod = intMyIp3mod%0x10000;
      
	int intMyIp1 = intMyIp2mod/0x100;
	int intMyIp0 = intMyIp2mod%0x100;
	return  
	    String.valueOf(intMyIp0)
	    + "." + String.valueOf(intMyIp1)
	    + "." + String.valueOf(intMyIp2)
	    + "." + String.valueOf(intMyIp3);
         
    }
}