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

package com.wentam.defcol.home;

import com.wentam.defcol.R;

import android.app.Activity;

import android.os.Bundle;

import android.widget.GridView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.Display;
import android.view.ViewGroup;

import android.graphics.Point;

import android.content.Intent;

import android.util.Log;
import android.util.DisplayMetrics;

import android.graphics.drawable.ColorDrawable;

import android.content.res.Resources;


public class HomeActivity extends Activity
{

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

	// get screen width and height
	Display display = getWindowManager().getDefaultDisplay();
	int width = display.getWidth();
	int height = display.getHeight();

	// get screen DPI(aka PPI)
	DisplayMetrics metrics = getResources().getDisplayMetrics();
	int dpi = (int) metrics.densityDpi;

	// decide on the number of columns to display in the gridview
	int maxcols = 3;

	int cols = width/dpi;

	if (cols > maxcols) {
	    cols = maxcols;
	}

	// get the gridview from the layout file, and set it up
	GridView gridview = (GridView) findViewById(R.id.gridview);       

	gridview.setNumColumns(cols);
	gridview.getLayoutParams().width=cols*dpi;
	gridview.setAdapter(new ButtonAdapter(this));
	gridview.setSelector(R.drawable.button_highlight);

	gridview.setOnItemClickListener(new OnItemClickListener() {
		public void onItemClick (AdapterView<?> parent, View v, int position, long id) {
		    goToPage(position+1);
		}
	    });
    }

    public void goToPage(int page_id)
    {
        if (page_id == 1) {
	    Intent myIntent = new Intent(this, com.wentam.defcol.paletteList.PaletteListActivity.class);
	    startActivity(myIntent);
	} else if (page_id == 2) {
	    Intent intent = new Intent(this, com.wentam.defcol.colorpicker.colorPickerActivity.class);
	    intent.putExtra("startingColor",""+0xFF000000);
	    intent.putExtra("color_id","-1");
	    startActivity(intent);
	} else if (page_id == 3) {
	    Intent intent = new Intent(this, com.wentam.defcol.connect_to_computer.WebInterfaceActivity.class);
	    startActivity(intent);
	}
    }
}
