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

import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.ByteOrder;

public class circle {
    private GL10 _gl;
    private float _x;
    private float _y;
    private float _radius;
    
    private float[] _colors;

    private float _lineWidth;
    
    private FloatBuffer _vb; // vertex buffer
    private FloatBuffer _cb; // color buffer

    private float _onePixelWidth;
    private float _onePixelHeight;

    private int _screen_width;
    private int _screen_height;

    private float _screen_ratio;

    public circle (GL10 gl, float x, float y, float radius, float lineWidth, float onePixelWidth, float onePixelHeight, int screen_width, int screen_height) {
	_gl = gl; 

	_x = x;
	_y = y;
	_radius = radius;

	_onePixelWidth = onePixelWidth;
	_onePixelHeight = onePixelHeight;

	_screen_width = screen_width;
	_screen_height = screen_height;


	_screen_ratio = new Float(screen_width)/new Float(screen_height);


	updateData();

	// gl.glLineWidth(lineWidth);
	_lineWidth = lineWidth;
    }

    public void updateData() {

	// define vertices //
	// TODO allow for the number of vertices to be set
	float vertices[] = new float[50*2+2];

	// convert x and y to a floating point value [-1..1]
	
	float x_float = 0f;
	float y_float = 0f;

	if (_x != 0.0f) {
	    for (int i = 0; i < _x; i++) {
		x_float += _onePixelWidth;
	    }
	}

	if (_y != 0.0f) {
	    for (int i = 0; i < _y; i++) {
		y_float += _onePixelHeight;
	    }
	}
	
	int vIndex=0;
	for (float i = 0; i < 360.0f; i+=(360.0f/50)) {

		float staticScaleDownAmount = 30f-(_radius*2);

		float basis_width = 1f;

		vertices[vIndex++] = (float)
		    (((
		       (Math.cos(Math.toRadians(i)))            // get x coord for point in circle
		       /(staticScaleDownAmount))) // scale down by a static amount based on screen ratio
		     +(x_float-1f)                              // move circle
		     );

		staticScaleDownAmount = 30f-(_radius*2);

		vertices[vIndex++] = (float)
		     ((
		       ((Math.sin(Math.toRadians(i))) // get y coord for point in  circle
			*_screen_ratio)
		       /(staticScaleDownAmount) // scale down by a static amount based on screen ratio
		       )) 
		    -(y_float-1f);                                                                         // move circle
	}
	
	vertices[50*2] = vertices[0];
	vertices[50*2+1] = vertices[1];


	// define colors //
	// TODO allow for modification of color through constructor and accesor method
	_colors = new float[360];
	int i = 0;
	for (int i2 = 0; i2<360/4; i2+=1){
	    _colors[i] = 0.7f;
	    _colors[i+1] = 0.7f;
	    _colors[i+2] = 1f;
	    _colors[i+3] = 0f;
	    i += 4;
	}
	
	
	// create buffers from data //
	ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4); 
	vbb.order(ByteOrder.nativeOrder());
	_vb = vbb.asFloatBuffer();
	_vb.put(vertices);
	_vb.position(0);

	ByteBuffer cbb = ByteBuffer.allocateDirect(_colors.length * 4); 
	cbb.order(ByteOrder.nativeOrder());
	_cb = cbb.asFloatBuffer(); 
	_cb.put(_colors);
	_cb.position(0); 
    }

    public void updateLocation (int x, int y, float size) {
	_x = x;
	_y = y;
	_radius = size;
	
	updateData();
    }

    public void draw () {
	_gl.glLineWidth(_lineWidth);
	_gl.glColorPointer(4, GL10.GL_FLOAT, 0, _cb);
	_gl.glVertexPointer (2, _gl.GL_FLOAT , 0, _vb); 
	_gl.glDrawArrays (_gl.GL_LINE_STRIP, 0, 51);

    }

    // ---------
    // Accesors TODO
    // ---------
    
    
    
}