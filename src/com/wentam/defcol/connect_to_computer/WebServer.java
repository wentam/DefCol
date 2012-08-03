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

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import org.apache.http.HttpException;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.impl.DefaultHttpServerConnection;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.BasicHttpProcessor;
import org.apache.http.protocol.HttpRequestHandlerRegistry;
import org.apache.http.protocol.HttpService;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;

import android.content.Context;

import android.util.Log;

import java.lang.Thread;
import java.lang.Runnable;

public class WebServer {

    public static boolean running;
    public static int serverPort = 8910;

    private Context context;

    private BasicHttpProcessor httpproc;
    private BasicHttpContext httpContext;
    private HttpService httpService;
    private HttpRequestHandlerRegistry registry;

    public WebServer(Context context, String jquery) {
	this.setContext(context);

	httpproc = new BasicHttpProcessor();
	httpContext = new BasicHttpContext();

	httpproc.addInterceptor(new ResponseDate());
	httpproc.addInterceptor(new ResponseServer());
	httpproc.addInterceptor(new ResponseContent());
	httpproc.addInterceptor(new ResponseConnControl());

	httpService = new HttpService(httpproc, new DefaultConnectionReuseStrategy(), new DefaultHttpResponseFactory());

	registry = new HttpRequestHandlerRegistry();

	registry.register("/", new HomeCommandHandler(context, jquery));

	httpService.setHandlerResolver(registry);
    }

    private ServerSocket serverSocket;

    public void runServer() {
	try {
	    serverSocket = new ServerSocket(serverPort);

	    serverSocket.setReuseAddress(true);

	    while (running) {
		try {
		    final Socket socket = serverSocket.accept();

		    DefaultHttpServerConnection serverConnection = new DefaultHttpServerConnection();

		    serverConnection.bind(socket, new BasicHttpParams());

		    httpService.handleRequest(serverConnection, httpContext);

		    serverConnection.shutdown();
		} catch (IOException e) {
		    e.printStackTrace();
		} catch (HttpException e) {
		    e.printStackTrace();
		}
	    }

	    serverSocket.close();
	} catch (SocketException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	}

	running = false;
    }

    public synchronized void startServer() {
	running = true;
	
	new Thread(new Runnable() {
		public void run() {
		    runServer();
		}
	    }).start();	

    }

    public synchronized void stopServer() {
	running = false;
	if (serverSocket != null) {
	    try {
		serverSocket.close();
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	}
    }

    public void setContext(Context context) {
	this.context = context;
    }

    public Context getContext() {
	return context;
    }
}