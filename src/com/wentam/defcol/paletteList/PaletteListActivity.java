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

package com.wentam.defcol.paletteList;

import com.wentam.defcol.R;

import com.wentam.defcol.PaletteFile;
import com.wentam.defcol.palette.PaletteActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;

import android.os.Bundle;

import android.widget.GridView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.EditText;
import android.widget.Button;

import android.view.ViewGroup.LayoutParams;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;

import android.util.Log;

import android.os.Environment;

import android.content.DialogInterface;
import android.content.Context;
import android.content.Intent;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;

import java.util.ArrayList;
import java.util.Arrays;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.ActionProvider;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import android.graphics.drawable.ColorDrawable;

public class PaletteListActivity extends SherlockActivity
{
    private File file;
    private final Context context = this;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
	setContentView(R.layout.manage_palettes);

	getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	getSupportActionBar().setBackgroundDrawable(new ColorDrawable(0xFF222222));
	getSupportActionBar().setTitle("Palettes");
	final PaletteFile paletteFile = new PaletteFile();

	int[] tmp = {0};
	final ArrayList<String> palette_names = paletteFile.getRows(tmp);
       
	// inflate add palette (plus sign) button from xml
	LinearLayout add_new_palette_button = (LinearLayout) ((LayoutInflater) this.getSystemService(this.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.add_new_palette_button, null);
      
	// set up ListView
	final ListView l = (ListView) findViewById(R.id.listview);
	l.setVerticalFadingEdgeEnabled(true);
	l.addFooterView(add_new_palette_button);

	final ArrayAdapter listAdapter = new ArrayAdapter<String>(this, R.layout.palette_list_item, palette_names);
	l.setAdapter(listAdapter);

	l.setOnItemClickListener(new OnItemClickListener() {
		public void onItemClick (AdapterView<?> parent, View v, int position, long id) {
		    if (position == palette_names.size()) {

			// add palette
			paletteFile.addNewPalette("New Palette","000000");
			palette_names.add("New Palette");
			listAdapter.notifyDataSetChanged();

			// if we scroll when we don't need too, there's a strange delay that's longer than the scroll time where the application is completely unresponsive.
			// this seems like something that should be implemented inside the listview by extending it, so I declare this an ugly hack.
			if (l.getMeasuredHeight()+l.getChildAt(0).getMeasuredHeight() >= findViewById(R.id.parentview).getMeasuredHeight()) {
			    l.smoothScrollToPosition(palette_names.size());
			}
		    } else {
			// load palette page!
			Intent myIntent = new Intent(context, PaletteActivity.class);
			myIntent.putExtra("pallete_id",""+position);
			int[] tmp = {1};
			myIntent.putExtra("pallete_colors",""+paletteFile.getRow(position, tmp));
			context.startActivity(myIntent);
		    }
		}
	    });

	// TODO: this block is messy, thanks to the nested listeners
	l.setOnItemLongClickListener(new OnItemLongClickListener() {
		public boolean onItemLongClick(AdapterView<?> parent, View v, final int position, long id) {       
		    if (position < palette_names.size()) {
			final CharSequence[] items = {"Rename","Delete"};

			AlertDialog.Builder builder = new AlertDialog.Builder(context)
			    .setItems(items,
				      new DialogInterface.OnClickListener() {
					  public void onClick(DialogInterface dialog, int index) {
					      if (index == 0) {
						  Log.i("DEFCOL","rename!");
						  AlertDialog.Builder builder;
						  final AlertDialog alertDialog;
						  LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
						  View layout = inflater.inflate(R.layout.rename_palette, null);

						  builder = new AlertDialog.Builder(context);
						  builder.setView(layout);
						  builder.setTitle("Rename Palette");
						  alertDialog = builder.create();
						  alertDialog.show();

						  final EditText rename_text = (EditText) layout.findViewById(R.id.edittext);
						  rename_text.setText(palette_names.get(position));

						  Button cancelButton = (Button) layout.findViewById(R.id.cancel);
						  cancelButton.setOnClickListener(new OnClickListener() {
							  public void onClick(View v)
							  {
							      alertDialog.dismiss();
							  } 
						      });

						  Button doneButton = (Button) layout.findViewById(R.id.done);
						  doneButton.setOnClickListener(new OnClickListener() {
							  public void onClick(View v)
							  {
							      String new_name = rename_text.getText().toString();
							      
							      // rename palette
							      paletteFile.renamePalette(position, new_name);	
							      palette_names.set(position, new_name);
							      listAdapter.notifyDataSetChanged();

							      alertDialog.dismiss();
							  } 
						      });

					      } else {
						  // delete palette
						  paletteFile.deletePalette(position);
						  palette_names.remove(position);
						  listAdapter.notifyDataSetChanged();
					      }
					  }
				      });
		    
			builder.show();

			return true;
		    } else {
			return false;
		    }
		}
	    });
	
    }

    private String readPalleteFile () {
	// check if we can read and write to the sd card
	String state = Environment.getExternalStorageState();
	if (Environment.MEDIA_MOUNTED.equals(state)) {
	    // we can read and write, do nothing
	} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
	    // we can only read, give error
	} else {
	    // we can't read or write, give error
	}

	// get our path, creating it if it doesn't exist
	File sdCard = Environment.getExternalStorageDirectory();
	File dir = new File (sdCard.getAbsolutePath() + "/DefCol");
	dir.mkdir();

	// get file object
	file = new File(dir, "palettes");

	// read palettes file and store it in memory for parsing
	String palettes_file = "";

	BufferedReader br = null;
	try {
	    br = new BufferedReader(new FileReader(file));
	    String tmp ;
	    while ((tmp = br.readLine()) != null) {
		palettes_file += tmp +"\n";
	    }
	    br.close();
	} catch (IOException e) {
	    Log.e("DEFCOL", "Error reading " + file, e);
	}
	
	return palettes_file;
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
}