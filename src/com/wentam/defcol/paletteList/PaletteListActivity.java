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
import com.wentam.defcol.paletteList.PaletteListAdapter;
import com.wentam.defcol.PaletteFile;
import com.wentam.defcol.palette.PaletteActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;

import android.os.Bundle;

import android.widget.GridView;
import android.widget.AdapterView;
import android.widget.Adapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.EditText;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.AbsListView.MultiChoiceModeListener;

import android.view.ViewGroup.LayoutParams;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;

import android.util.Log;
import android.util.SparseBooleanArray;

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

import android.view.ActionProvider;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ActionMode;
import android.view.MenuInflater;
import android.view.SubMenu;

import android.graphics.drawable.ColorDrawable;

import java.lang.NullPointerException;

public class PaletteListActivity extends Activity
{
    private File file;
    private final Context context = this;
    private ArrayList<String> palette_names;
    private PaletteFile paletteFile;
    private PaletteListAdapter listAdapter;
    private ListView l;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("Add Palette")
            .setIcon(R.drawable.new_item)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);       
	
        return true;
    }

    @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            try {
                if (item.getItemId() == android.R.id.home) {
                    finish();
                    return true;
                } else if (item.toString() == "Add Palette") {
                    paletteFile.addNewPalette("New Palette","000000");
                    palette_names.add("New Palette");
                    listAdapter.notifyDataSetChanged();

                    // if the new list item pushed the 'add new palette' button below the screen,
                    // scroll to the bottom
                    if (l.getMeasuredHeight()+l.getChildAt(0).getMeasuredHeight()
                            >=
                            findViewById(R.id.parentview).getMeasuredHeight()) {

                        l.smoothScrollToPosition(palette_names.size());
                    }
                    return true;
                } else {
                    return super.onOptionsItemSelected(item);
                }
            } catch (NullPointerException e) {}
            return false;
        }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
	setContentView(R.layout.manage_palettes);

	getActionBar().setDisplayHomeAsUpEnabled(true);
	getActionBar().setTitle("Palettes");
	paletteFile = new PaletteFile();

	int[] tmp = {0};
	palette_names = paletteFile.getRows(tmp);
       
	// inflate add palette (plus sign) button from xml
	LinearLayout add_new_palette_button = (LinearLayout) ((LayoutInflater) this.getSystemService(this.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.add_new_palette_button, null);
      
	// set up ListView
	l = (ListView) findViewById(R.id.listview);
	// l.addFooterView(add_new_palette_button);
	l.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);

	listAdapter = new PaletteListAdapter(context, palette_names, paletteFile, l);
	l.setAdapter((ListAdapter) listAdapter);


	l.setMultiChoiceModeListener(new MultiChoiceModeListener() {
		@Override
		public void onItemCheckedStateChanged(ActionMode mode, int position,
						      long id, boolean checked) {

		    mode.setTitle(l.getCheckedItemCount()+" selected");
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		    SparseBooleanArray checked = l.getCheckedItemPositions();

		    final ActionMode m = mode;

		    switch (item.getItemId()) {
		    case R.id.delete:
			
			for (int i = palette_names.size()-1; i >= 0; i--) {
			    if (l.isItemChecked(i)) {
				paletteFile.deletePalette(i);
				palette_names.remove(i);
			    }
			}

			listAdapter.notifyDataSetChanged();
				

		    	mode.finish();
		    	return true;
		    case R.id.rename:

			int first_checked_nonfinal = -1;
			for (int i = palette_names.size()-1; i >= 0; i--) {
			    if (l.isItemChecked(i)) {
				first_checked_nonfinal = i;
			    }
			}

			final int first_checked = first_checked_nonfinal;

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
			rename_text.setText(palette_names.get(first_checked));

			Button cancelButton = (Button) layout.findViewById(R.id.cancel);
			cancelButton.setOnClickListener(new OnClickListener() {
				public void onClick(View v)
				{
				    alertDialog.dismiss();
				    m.finish();
				} 
			    });

			Button doneButton = (Button) layout.findViewById(R.id.done);
			doneButton.setOnClickListener(new OnClickListener() {
				public void onClick(View v)
				{
				    String new_name = rename_text.getText().toString();

				    // rename palettes
				    for (int i = palette_names.size()-1; i >= 0; i--) {
					if (l.isItemChecked(i)) {
					    paletteFile.renamePalette(i, new_name);	
					    palette_names.set(i, new_name);
					    listAdapter.notifyDataSetChanged();
					}
				    }

				    alertDialog.dismiss();
				    m.finish();
				} 
			    });			       
			return true;
		    default:
			return false;
		    }
		}

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		    MenuInflater inflater = mode.getMenuInflater();
		    inflater.inflate(R.menu.palette_list_context, menu);
		    return true;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		    return false;
		}
	    });




	// l.setOnItemClickListener(new OnItemClickListener() {
	// 	public void onItemClick (AdapterView<?> parent, View v, int position, long id) {
	// 	    if (position == palette_names.size()) {

	// 		// add palette
	// 		paletteFile.addNewPalette("New Palette","000000");
	// 		palette_names.add("New Palette");
	// 		listAdapter.notifyDataSetChanged();

	// 		// if the new list item pushed the 'add new palette' button below the screen,
	// 		// scroll to the bottom
	// 		if (l.getMeasuredHeight()+l.getChildAt(0).getMeasuredHeight() 
	// 		    >=
	// 		    findViewById(R.id.parentview).getMeasuredHeight()) {

	// 		    l.smoothScrollToPosition(palette_names.size());
	// 		}
	// 	    }
	// 	}
	//     }); 
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
}
