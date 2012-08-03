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

package com.wentam.defcol.palette;

import com.wentam.defcol.R;
import com.wentam.defcol.PaletteFile;

import android.app.Activity;
import android.os.Bundle;

import android.widget.GridView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import android.view.LayoutInflater;
import android.view.View;
import android.view.Display;
import android.view.Window;

import android.graphics.Color;

import android.content.res.Configuration;

import android.graphics.Point;
import android.graphics.Rect;

import android.graphics.drawable.ColorDrawable;

import android.content.Intent;
import android.content.Context;;

import android.util.Log;

import java.lang.Runnable;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.ActionProvider;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import android.graphics.drawable.ColorDrawable;

public class PaletteActivity extends SherlockActivity
{
    private Context c = this;
    private PaletteFile paletteFile;
    private int palette_id;
    private RadialPaletteView radialPalette;
    private String[] colors;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
	
	getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	getSupportActionBar().setBackgroundDrawable(new ColorDrawable(0xFF222222));

        setContentView(R.layout.palette);

	paletteFile = new PaletteFile();

	Bundle extras = getIntent().getExtras();

	palette_id = Integer.parseInt(extras.getString("pallete_id"));

	int tmp[] = {0};

	String name = paletteFile.getRow(palette_id,tmp);
	getSupportActionBar().setTitle(name);


	// give the radialPaletteView it's colors!

	if (savedInstanceState != null && savedInstanceState.containsKey("colors")) {
	    colors = savedInstanceState.getStringArray("colors");
	} else {
	    colors = extras.getString("pallete_colors").split("\\.");
	}

	radialPalette = (RadialPaletteView) findViewById(R.id.radialPalette);
	radialPalette.setColors(colors);
	radialPalette.setPalette(palette_id);

	radialPalette.setOnColorClickListener(new OnColorClickListener(){
		public void click (int color_id, int color) {
		    Intent intent = new Intent(c, com.wentam.defcol.colorpicker.colorPickerActivity.class);
		    intent.putExtra("startingColor",""+color);
		    intent.putExtra("color_id",""+color_id);
		    startActivityForResult(intent,0);
		}
	});
	
	// move @id/plus to be in the middle of @id/radialPalette
	final RelativeLayout parent = (RelativeLayout) findViewById(R.id.parent);

	parent.post(new Runnable(){
		@Override
		public void run(){
		    int screen_width = parent.getMeasuredWidth();
		    int screen_height = parent.getMeasuredHeight();

		    // RelativeLayout header = (RelativeLayout) findViewById(R.id.headerWrapper);
		    // ImageView plus = (ImageView) findViewById(R.id.plus);
	

		    // RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) plus.getLayoutParams();
		    // if (screen_width < screen_height) {
		    // 	params.topMargin += (screen_width/2-);
		    // } else {
		    // 	params.topMargin += ((screen_height)/2);
		    // }

		    // plus.setLayoutParams(params);
		}
	    });
    }

    protected void onSaveInstanceState (Bundle state) {
	state.putStringArray("colors",(String[])radialPalette.getColors().clone());
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
	super.onActivityResult(requestCode, resultCode, data);

	Bundle extras = data.getExtras();

	float[] hsv = new float[3];
	
	hsv[0] = Float.parseFloat(extras.getString("hue"));
	hsv[1] = Float.parseFloat(extras.getString("saturation"));	
	hsv[2] = Float.parseFloat(extras.getString("value"));


	String color = (Integer.toHexString((Color.HSVToColor(hsv))).substring(2));

	int color_id = Integer.parseInt(extras.getString("color_id"));
	
	paletteFile.changeColor(palette_id, color_id, color);

	int[] tmp = {1};

	String[] colors = paletteFile.getRow(palette_id, tmp).split("\\.");

	radialPalette.setColors(colors);
    }
}
