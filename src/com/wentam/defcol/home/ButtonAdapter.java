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

import android.os.Bundle;

import android.widget.BaseAdapter;
import android.widget.RelativeLayout;

import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;

import android.content.Context;

import android.app.Activity;

public class ButtonAdapter extends BaseAdapter {
    private Context mContext;

    public ButtonAdapter(Context c) {
        mContext = c;
    }

    public int getCount() {
        return 3;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
	if (position == 0) {
	    return ((LayoutInflater) mContext.getSystemService(mContext.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.manage_palettes_icon,null);
	} else if (position == 1) {
	    return ((LayoutInflater) mContext.getSystemService(mContext.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.color_pick_icon,null);
	} else if (position == 2) {
	    return ((LayoutInflater) mContext.getSystemService(mContext.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.computer_connect_icon,null);
	}


	return ((LayoutInflater) mContext.getSystemService(mContext.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.manage_palettes_icon,null);
    }

}