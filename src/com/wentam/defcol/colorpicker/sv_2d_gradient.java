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
import android.os.SystemClock;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.ByteOrder;

public class sv_2d_gradient {
    private GL10 _gl;

    private float _x1; public float getLeft(){return _x1;}
    private float _y1; public float getBottom(){return _y1;}
    private float _x2; public float getRight(){return _x2;}
    private float _y2; public float getTop(){return _y2;}


    private float _hue; public float getHue(){return _hue;}

    private float _onePixelWidth;
    private float _onePixelHeight;

    private float _values[]; public float[] getValues(){return _values;}
    private float _saturations[]; public float[] getSaturations(){return _saturations;}
    // ---

    private gradientLine gradientLines[];
    private float _frgb[];
    
    private int _width;
    private int _height; public int getHeight() {return _height;}

    private boolean waitToDraw = false;
    private boolean waitToCalc = false;


    // sv_2d_gradient(GL10 gl, float x1, float x2, float y1, float y2, float hue)
    //
    //      Creates a new instance of the object.
    //
    //      gl: the opengl object
    //      x1, x2, y1, y2: the location and size of the object
    //      hue: a float [0f..360f] that contains the starting hue for the gradient
    //      width: the width of the screen
    //      height: the height of the screen
    public sv_2d_gradient (GL10 gl, float x1, float x2, float y1, float y2, float hue, float onePixelWidth, float onePixelHeight) {
	init(gl, x1, x2, y1, y2, hue, onePixelWidth, onePixelHeight);
    }
   
    public void init (GL10 gl, float x1, float x2, float y1, float y2, float hue, float onePixelWidth, float onePixelHeight) {
	_gl = gl;
	
	_x1 = x1;
	_x2 = x2;
	_y1 = y1;
	_y2 = y2;

	_hue = hue;

	_onePixelWidth = onePixelWidth;
	_onePixelHeight = onePixelHeight;

	// calculate width a height
	float floatWidth = x2-x1;
	float floatHeight = y2-y1;

	_width = (int) (floatWidth/onePixelWidth);
	_height = (int) (floatHeight/onePixelHeight);


	updateHue(hue);
    }

    public void draw () {
	while (waitToDraw == true) {SystemClock.sleep(1);}

	waitToCalc = true;
	for (gradientLine line : gradientLines) {
	    line.draw();
	}
	waitToCalc = false;
    }

    // updateHue(float hue)
    //
    //       updates the gradient's current hue
    //
    //       hue: a float [0f..360f] that contains the hue for the gradient
    public boolean updateHue (float hue) {
	while (waitToCalc == true) {SystemClock.sleep(1);}
	waitToDraw = true;
	_hue = hue;

	float incr_amnt_w = (new Float(1)/new Float(_width))*2;
	float incr_amnt_h = (new Float(1)/new Float(_height))*2;

	// arrays for getting the color
	// values
	_values  = new float[_width+2];

	for (int i=0; i<_width+2; i++) {
	    _values[i] = ((new Float(i)*new Float(incr_amnt_w))/2);
	    if (_values[i] > 1.0f) {
		_values[i] = 1f;
	    }
	}

	// saturations
	_saturations = new float[_height+2];

	int i4 = _height;
	for (int i=0; i<_height+2; i++) {
	    _saturations[i] = ((new Float(i4)*new Float(incr_amnt_h))/2);
	    i4--;
	}

	// build array with enough values to fill the width
	float hsv[] = new float[_width*3];
	
	for (int i = 0; i<_width; i++) {
	    hsv[(i*3)] = hue;
	    hsv[(i*3)+1] = (i*incr_amnt_w)/2;
	    hsv[(i*3)+2] = 1f;
	}
	


	// convert hsv array to float RGBA
	float frgb[] = new float[_width*4];

	float hsv_color[] = new float[3];
	String rgb_hex_strings[] = new String[3];
	int i5;
	int i6;
	String rgb_hex_string;
	for (int i = 0; i<_width; i++) {
	    i5 = i*3;
	    i6 = i*4;

	    hsv_color[0] = hsv[(i5)];
	    hsv_color[1] = hsv[(i5)+1];
	    hsv_color[2] = hsv[(i5)+2];

	    float rgb_tmp[] = HSVtoRGB(hsv_color[0], hsv_color[1], hsv_color[2]);	   

	    frgb[i6] = rgb_tmp[0];
	    frgb[i6+1] = rgb_tmp[1];
	    frgb[i6+2] = rgb_tmp[2];
	    frgb[i6+3] = 0f;
	}

	_frgb = frgb;

	if (gradientLines == null) {
	    gradientLines = new gradientLine[_width];

	    // create array of gradientLines
	    float mCurrentX = _x2;
	    for (int i = 0; i<_width; i++) {	    
		float [] startColors = { 
		    _frgb[i*4], _frgb[i*4+1], _frgb[i*4+2], 1f,
		    0f, 0f, 0f, 1f
		};

		gradientLines[i] = new gradientLine(_gl, mCurrentX, mCurrentX, _y2, _y1, startColors);
		mCurrentX -= _onePixelWidth;
	    }
	} else {
	    int i = 0;
	    for (gradientLine line : gradientLines) {
		if (line != null) {
		    float[] newColors = {
			_frgb[i*4], _frgb[i*4+1], _frgb[i*4+2], 1f,
			0f, 0f, 0f, 1f
		    };
		    line.setColor(newColors);
		}
		i++;
	    }
	}       

	waitToDraw = false;
	return true;
    }
    

    // HSV to RGB algorithim stolen from the intrawebs, mine was too slow
    
    private float[] HSVtoRGB( float h, float s, float v )
    {
	int i;
	float f, p, q, t, r, g, b;
	if( s == 0 ) {
	    // achromatic (grey)
	    r = g = b = v;
	    float[] tmp = {r,g,b};
	    return tmp;
	}
	h /= 60;			// sector 0 to 5
	i = (int)(Math.floor(h));
	f = h - i;			// factorial part of h
	p = v * ( 1 - s );
	q = v * ( 1 - s * f );
	t = v * ( 1 - s * ( 1 - f ) );
	switch( i ) {
	case 0:
	    r = v;
	    g = t;
	    b = p;
	    break;
	case 1:
	    r = q;
	    g = v;
	    b = p;
	    break;
	case 2:
	    r = p;
	    g = v;
	    b = t;
	    break;
	case 3:
	    r = p;
	    g = q;
	    b = v;
	    break;
	case 4:
	    r = t;
	    g = p;
	    b = v;
	    break;
	default:
	    r = v;
	    g = p;
	    b = q;
	    break;
	}

	float[] tmp = {r,g,b};
	return tmp;
    }
}