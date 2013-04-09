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

import android.app.Activity;
import android.os.Bundle;

import android.widget.GridView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.Button;
import android.widget.TextView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;

import android.content.Intent;
import android.content.Context;


import android.util.Log;

import android.view.Menu;
import android.view.MenuItem;

import android.graphics.drawable.ColorDrawable;


import java.lang.Thread;
import java.lang.Runnable;

import android.app.NotificationManager;
import android.app.Notification;
import android.app.PendingIntent;

import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.util.Enumeration;

import java.net.NetworkInterface;
import java.net.InetAddress;
import java.net.SocketException;

import android.net.wifi.*;


public class WebInterfaceActivity extends Activity {
    private Context context = this;
    Intent webServerService;
    private NotificationManager mNotificationManager;

    private boolean running = false;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

	Bundle extras = getIntent().getExtras();
	
	if (extras != null) {
	    if (extras.containsKey("die_on_start")) {		
		 finish();
	     }
	 }

        setContentView(R.layout.computer_connect);

	
	getActionBar().setDisplayHomeAsUpEnabled(true);
	// getActionBar().setBackgroundDrawable(new ColorDrawable(0xFF222222));
	getActionBar().setTitle("Connect to a Computer");


	String ns = Context.NOTIFICATION_SERVICE;
	mNotificationManager = (NotificationManager) getSystemService(ns);

	String ip = getIpAddress();

	TextView text = (TextView) findViewById(R.id.text);
	text.setText("Press start, and then go to "+ip+":8910 in your computer's browser");

	Button button = (Button) findViewById(R.id.start);

	button.setOnClickListener(new OnClickListener() {
		public void onClick(View v) {
		    running = true;

		    // load jquery string from file
		    InputStream inputStream = getResources().openRawResource(R.raw.jquery);
		    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

		    int i;
		    try {
			i = inputStream.read();
			while (i != -1) {
			    byteArrayOutputStream.write(i);
			    i = inputStream.read();
			}
			inputStream.close();
		    } catch (IOException e) {
			e.printStackTrace();
		    }

		    String jquery = byteArrayOutputStream.toString();


		    webServerService =  new Intent(context, com.wentam.defcol.connect_to_computer.WebServerService.class);		    
		    webServerService.putExtra("jquery",jquery);
		    context.startService(webServerService);       
		}
	    });


	Button button2 = (Button) findViewById(R.id.stop);

	button2.setOnClickListener(new OnClickListener() {
		public void onClick(View v) {
		    running = false;

		    if (webServerService == null) {
			String jquery = "dummytext";
			webServerService = new Intent(context, com.wentam.defcol.connect_to_computer.WebServerService.class);
			webServerService.putExtra("jquery",jquery);		
		    }
	
		    context.stopService(webServerService);
		}
	    });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
	switch (item.getItemId()) {
        case android.R.id.home:
	    finish();
            return true;
        default:
            return super.onOptionsItemSelected(item);
	}
    }

    public String getIpAddress() {
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
