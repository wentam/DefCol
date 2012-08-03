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
import com.wentam.defcol.PaletteFile;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentProducer;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

import android.content.Context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import java.util.HashMap;

import java.io.InputStream;
import java.io.ByteArrayOutputStream;

import org.json.*;


// TODO refactor the way this works completely
public class HomeCommandHandler implements HttpRequestHandler {
    private Context context = null;
    private PaletteFile pFile;
    private String jquery;

    public HomeCommandHandler(Context context, String _jquery) {
	this.context = context;
	pFile = new PaletteFile();

	jquery = _jquery;
    }
    

    public String getJs() {
	// load jquery string from file
	InputStream inputStream = context.getResources().openRawResource(R.raw.js);
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

	return byteArrayOutputStream.toString();

    }

    public String getHtml() {
	// load jquery string from file
	InputStream inputStream = context.getResources().openRawResource(R.raw.html);
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

	return byteArrayOutputStream.toString();

    }

    @Override
    public void handle(final HttpRequest request, final HttpResponse response,
		       HttpContext httpContext) throws HttpException, IOException {
	HttpEntity entity = new EntityTemplate(new ContentProducer() {
		public void writeTo(final OutputStream outstream) throws IOException {
		    OutputStreamWriter writer = new OutputStreamWriter(outstream, "UTF-8");
		    String req = request.getRequestLine().getUri();

		    req = req.replaceAll("/\\?", "");

		    String[] pairs = req.split("&");

		    HashMap data = new HashMap();

		    for (int i = 0; i < pairs.length; i++) {
			if (pairs[i].contains("=")) {
			    String[] pair = pairs[i].split("=");
			    data.put(pair[0],pair[1]);
			}
		    }

		    
		    String action = "none";
		    if (data.containsKey("action")) {
			action = (String) data.get("action");
		    }

		    String resp = "404 on "+action;		    
		    if (action.equals("none") || action.equals("home")) {
			response.setHeader("Content-Type", "text/html");			

			resp = getHtml();
	
		    } else if (action.equals("getPalettes")) {
			response.setHeader("Content-Type", "application/json");

			JSONArray json = new JSONArray();

			int tmp[] = {0};
			ArrayList<String> palettes = pFile.getRows(tmp);
			Iterator i = palettes.iterator();
			while(i.hasNext()) {
			    JSONObject item = new JSONObject();
			    try {item.put("name",i.next());} catch(JSONException e) {}
			    json.put(item);
			}

			resp = json.toString();
			
		    } else if (action.equals("getJquery")) {
			response.setHeader("Content-Type", "application/javascript");
			resp = jquery;
		    } else if (action.equals("getJs")) {
			response.setHeader("Content-Type", "application/javascript");
			resp = getJs();
		    } else if (action.equals("getPaletteColors")) {
			response.setHeader("Content-Type", "application/javascript");
			int id =  Integer.parseInt((String) data.get("id"));

			int tmp[] = {1};
			String row = pFile.getRow(id, tmp);
			
			String colors[] = row.split("\\.");

			JSONArray json = new JSONArray();

			for (int i = 0; i < colors.length; i++) {
			    json.put(colors[i]);
			}

			resp = json.toString();		
		    }

		    writer.write(resp);
		    writer.flush();
		}
	    });

	response.setEntity(entity);
    }

    public Context getContext() {
	return context;
    }
}