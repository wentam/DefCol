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

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;


import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Button;
import android.widget.ImageButton;

import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;
import android.util.Log;

public class PaletteListAdapter extends BaseAdapter {
    private LayoutInflater layoutInflater;
    private ArrayList<String> items;
    private PaletteFile paletteFile;
    private PaletteListAdapter me;
    private Context context;

    // --
    // listeners
    // --

    OnClickListener deleteListener = new OnClickListener() {
	    @Override
	    public void onClick(View v) {
		int position = (Integer) v.getTag();

		me.paletteFile.deletePalette(position);
		me.items.remove(position);
		me.notifyDataSetChanged();
	    }
	};

    OnClickListener mainListener = new OnClickListener() {
	    @Override
	    public void onClick(View v) {
		int position = (Integer) v.getTag();

		Intent intent = new Intent(me.context, PaletteActivity.class);
		intent.putExtra("pallete_id",""+position);
		int[] tmp = {1};
		intent.putExtra("pallete_colors",""+me.paletteFile.getRow(position, tmp));
		me.context.startActivity(intent);
	    }
	};
    
    // --
    // constructor
    // --
    
    public PaletteListAdapter(Context c, ArrayList<String> i, PaletteFile p) {
	super();

	this.layoutInflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	this.items = i;
	this.paletteFile = p;
	this.me = this;
	this.context = c;
    }
    
    // --
    // methods
    // --

    public View getView (int position, View convertView, ViewGroup parent) {
	ImageButton delete_btn;
	Button main_btn;

	if (convertView == null) {
	    convertView = layoutInflater.inflate(R.layout.palette_list_item, null);

	    delete_btn = ((ImageButton) convertView.findViewById(R.id.delete_btn));
	    delete_btn.setTag(position);
	    delete_btn.setOnClickListener(deleteListener);

	    main_btn = ((Button) convertView.findViewById(R.id.main_btn));
	    main_btn.setTag(position);
	    main_btn.setOnClickListener(mainListener);
	} else {
	    delete_btn = ((ImageButton) convertView.findViewById(R.id.delete_btn));
	    delete_btn.setTag(position);
	    
	    main_btn = ((Button) convertView.findViewById(R.id.main_btn));
	    main_btn.setTag(position);
	}

	((TextView) convertView.findViewById(R.id.textview)).setText(items.get(position));
	
	return convertView;
    }

    public int getCount() {
	return this.items.size();
    }

    public long getItemId (int position) {
	return position;
    }

    public Object getItem (int position) {
	return this.items.get(position);
    }
}