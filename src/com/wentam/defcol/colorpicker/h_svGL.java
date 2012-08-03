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

package com.wentam.defcol.colorpicker;

import android.app.Activity;
import android.os.Bundle;

import android.widget.GridView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.view.MotionEvent;

import android.view.LayoutInflater;
import android.view.View;

import android.content.Intent;
import android.content.Context;

import android.util.Log;
import android.util.AttributeSet;

import android.opengl.GLSurfaceView;

import android.graphics.PixelFormat;

public class h_svGL extends GLSurfaceView {
    private colorChangeListener colorChangeListener;

    private h_svRenderer h_svRenderer;

    private float hsv[];

    public h_svGL(Context context, AttributeSet a){
        super(context, a);

	h_svRenderer = new h_svRenderer();
        
	setEGLConfigChooser(8,8,8,8,16,0);
	setZOrderOnTop(true);
        setRenderer(h_svRenderer);

    }

    public void setOnColorChangeListener(colorChangeListener listener){
	colorChangeListener = listener;
    };

    public void setDpi(int dpi) {
	h_svRenderer.setDpi(dpi);
    }

    public void setColor(float[] hsvcolor) {
	hsv = hsvcolor;
	h_svRenderer.setColor(hsvcolor);
    };

    public boolean onTouchEvent (MotionEvent event) {
	int action = event.getAction();

	float x = event.getX();
	float y = event.getY();
	float size = event.getSize();


	switch(action){
	case MotionEvent.ACTION_DOWN:
	    h_svRenderer.showCircle();
	    h_svRenderer.startMoveCircle(x, y);
	    h_svRenderer.moveCircle(x, y, size);
	    break;
	case MotionEvent.ACTION_UP:
	    h_svRenderer.hideCircle();

	    float[] tmp = {h_svRenderer.getHue(), h_svRenderer.getSaturation(), h_svRenderer.getValue()};	    

	    if (colorChangeListener != null) {
		colorChangeListener.onColorChange(tmp);
	    }

	    break;	
	case MotionEvent.ACTION_MOVE:
	    h_svRenderer.moveCircle(x, y, size);

	    float[] tmp2 = {h_svRenderer.getHue(), h_svRenderer.getSaturation(), h_svRenderer.getValue()};	    

	    if (colorChangeListener != null) {
		colorChangeListener.onColorChange(tmp2);
	    }

	    break;
	default:
	}

	super.onTouchEvent(event);
	return true;
    }
}