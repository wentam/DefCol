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

public class rect {
    private GL10 _gl;
    
    private float _left;   public float getLeft  (){return _left;  }
    private float _top;    public float getTop   (){return _top;   }
    private float _right;  public float getRight (){return _right; }
    private float _bottom; public float getBottom(){return _bottom;}

    private float _lineWidth;
    
    private FloatBuffer _vb; // vertex buffer
    private FloatBuffer _cb; // color buffer

    private float _onePixelWidth;
    private float _onePixelHeight;

    private int _screen_width;
    private int _screen_height;

    private float _screen_ratio;

    public rect (GL10 gl, float left, float top, float right, float bottom, float lineWidth, float onePixelWidth, float onePixelHeight, int screen_width, int screen_height) {
	_gl = gl;
	
	_left = left;
	_top = top;
	_right = right;
	_bottom = bottom;

	_onePixelWidth = onePixelWidth;
	_onePixelHeight = onePixelHeight;

	_screen_width = screen_width;

	_screen_height = screen_height;
	    
	_screen_ratio = new Float(screen_width)/new Float(screen_height);

	_lineWidth = lineWidth;

	updateData();
    }

    public void setLocation(float left, float top, float right, float bottom) {	
	_left = left;
	_top = top;
	_right = right;
	_bottom = bottom;

	updateData();
    }


    public void updateData() {

	float vertices[] = {
	    _left, _top,
	    _left, _bottom,
	    _right, _bottom,
	    _right, _top,
	    _left, _top
	};

	float colors[] = {
	    0f,0f,0f,1f,
	    0f,0f,0f,1f,
	    0f,0f,0f,1f,
	    0f,0f,0f,1f,
	    0f,0f,0f,1f
	};
       
	ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4); 
	vbb.order(ByteOrder.nativeOrder());
	_vb = vbb.asFloatBuffer();
	_vb.put(vertices);
	_vb.position(0);

	ByteBuffer cbb = ByteBuffer.allocateDirect(colors.length * 4); 
	cbb.order(ByteOrder.nativeOrder());
	_cb = cbb.asFloatBuffer(); 
	_cb.put(colors);
	_cb.position(0); 
    }

    public void draw () {
	_gl.glLineWidth(_lineWidth);
	_gl.glColorPointer(4, _gl.GL_FLOAT, 0, _cb);
	_gl.glVertexPointer (2, _gl.GL_FLOAT , 0, _vb); 
	_gl.glDrawArrays (_gl.GL_LINE_STRIP, 0, 5);
    }

    public float floatFromPx (int px, int axis) {
	float result = 0f;

	if (px != 0) {
	    for (int i = 0; i < px; i++) {
		if (axis == 0) {
		    result += _onePixelWidth;
		} else {
		    result += _onePixelHeight;
		}
	    }
	}

	return result;
    }

}