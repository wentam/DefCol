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

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLSurfaceView;
import android.opengl.GLU;

import android.graphics.Color;

import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.ByteOrder;

public class hueBar {
    private GL10 _gl;

    private float _x1; public float getLeft(){return _x1;}
    private float _y1; public float getBottom(){return _y1;}
    private float _x2; public float getRight(){return _x2;}
    private float _y2; public float getTop(){return _y2;}

    private float _onePixelWidth;
    private float _onePixelHeight;

    // ---

    private gradientLine gradientLines[];
    private float _frgb[];

    private int _width; public int getWidth() {return _width;}
    private int _height;  public int getHeight() {return _height;}

    public hueBar (GL10 gl, float x1, float x2, float y1, float y2, float onePixelWidth, float onePixelHeight) {
	init(gl, x1, x2, y1, y2, onePixelWidth, onePixelHeight);
    }
   
    public void init (GL10 gl, float x1, float x2, float y1, float y2, float onePixelWidth, float onePixelHeight) {
	_gl = gl;
	
	_x1 = x1;
	_x2 = x2;
	_y1 = y1;
	_y2 = y2;

	_onePixelWidth = onePixelWidth;
	_onePixelHeight = onePixelHeight;

	// calculate width a height
	float floatWidth = x2-x1;
	float floatHeight = y2-y1;

	_width = (int) (floatWidth/onePixelWidth);
	_height = (int) (floatHeight/onePixelHeight);

	generateColors();
    }

    public void generateColors () {
	_width += 1;

	// build array with enough values to fill the width
	float hsv[] = new float[_width*3];
	
	float incr_amnt_w = (new Float(360)/new Float(_width))*2;
	float incr_amnt_h = (new Float(1)/new Float(_height))*2;

	for (int i = 0; i<_width; i++) {
	    hsv[(i*3)] = (new Float(i)*new Float(incr_amnt_w))/2;
	    hsv[(i*3)+1] = 1f;
	    hsv[(i*3)+2] = 1f;
	}
	
	// convert hsv array to integer RGB
	int irgb[] = new int[_width*3];

	for (int i = 0; i<_width; i++) {
	    float hsv_color[] = new float[3];
	    hsv_color[0] = hsv[(i*3)];
	    hsv_color[1] = hsv[(i*3)+1];
	    hsv_color[2] = hsv[(i*3)+2];
	    
	    String rgb_hex_string = Integer.toHexString(Color.HSVToColor(hsv_color)).substring(2);

	    String rgb_hex_strings[] = new String[3];

	    rgb_hex_strings[0] = rgb_hex_string.substring(0,2);
	    rgb_hex_strings[1] = rgb_hex_string.substring(2,4);
	    rgb_hex_strings[2] = rgb_hex_string.substring(4,6);

	    irgb[i*3] = Integer.parseInt(rgb_hex_strings[0], 16);
	    irgb[i*3+1] = Integer.parseInt(rgb_hex_strings[1], 16);
	    irgb[i*3+2] = Integer.parseInt(rgb_hex_strings[2], 16);
	}

	// convert integer RGB array to float RGBA array
	float frgb[] = new float[_width*4];
	float floatStep = new Float(1)/new Float(256);
	
	// loop over colors
	for (int i = 0; i<_width; i++) {
	    int rgb_color[] = new int[3];
	    float frgba_color[] = new float[4];
	    rgb_color[0] = irgb[(i*3)];
	    rgb_color[1] = irgb[(i*3)+1];
	    rgb_color[2] = irgb[(i*3)+2];

	    // loop over channels
	    int i2 = 0;
	    for (int val : rgb_color) {
		float f = 0f;
		for (int i3 = 0; i3<=val; i3++) {
		    f += floatStep;
		}
		
		frgba_color[i2] = f;
		i2++;
	    }
	    
	    frgba_color[3] = 0f; // alpha

	    for (int i3 = 0; i3<4; i3++) {
		frgb[i*4+i3] = frgba_color[i3];
	    }
	}
	
	_frgb = frgb;

	gradientLines = new gradientLine[_width];

	// create array of gradientLines
	float mCurrentX = _x2;
	for (int i = 0; i<_width; i++) {	    
	    float [] startColors = { 
		_frgb[i*4], _frgb[i*4+1], _frgb[i*4+2], 1f,
		_frgb[i*4], _frgb[i*4+1], _frgb[i*4+2], 1f
	    };

	    gradientLines[i] = new gradientLine(_gl, mCurrentX, mCurrentX, _y2, _y1, startColors);
	    mCurrentX -= _onePixelWidth;
	}

	_width -= 1;
    }

    public void draw () {
	for (gradientLine line : gradientLines) {
	    line.draw();
	}
    }
}