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

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.ByteOrder;

public class gradientLine {
    private GL10 gl;
    private float _x1;
    private float _x2;
    private float _y1;
    private float _y2;
    
    private float[] _colors;
    
    private FloatBuffer VB; // vertex buffer
    private FloatBuffer CB; // color buffer

    public gradientLine (GL10 g, float x1, float x2, float y1, float y2, float[] colors) {
	gl = g; 

	_x1 = x1;
	_x2 = x2;

	_y1 = y1;
	_y2 = y2;
	
	_colors = colors;

	updateData();

	gl.glLineWidth(1.0f);
    }

    public void setPoints(float x1, float x2, float y1, float y2) {
	_x1 = x1;
	_x2 = x2;

	_y1 = y1;
	_y2 = y2;

	updateData();
    }

    public void setColor(float[] colors) {
	_colors = colors;
	
        CB.put(_colors);
        CB.position(0); 
    }

    public void incrementX(boolean left){
	if (left == true) {
	    _x1 -= 1.0f;
	} else {
	    _x1 += 1.0f;
	}

	updateData();
    }

    public void incrementY(boolean up){
	if (up == true) {
	    _y1 -= 1.0f;
	} else {
	    _y1 += 1.0f;
	}

	updateData();
    }

    public void draw() {
	// gl.glMatrixMode(GL10.GL_MODELVIEW);
        // gl.glLoadIdentity();    
        // GLU.gluLookAt(gl, 0, 0, -5, 0f, 0f, 0f, 0f, 1.0f, 0.0f); 


	// draw line
	gl.glVertexPointer(2, GL10.GL_FLOAT, 0, VB);
	gl.glColorPointer(4, GL10.GL_FLOAT, 0, CB);
	gl.glDrawArrays(GL10.GL_LINE_STRIP, 0, 2);
    }

    private void updateData() {
	float[] V = {
	    _x1, _y1,
	    _x2, _y2
	};


	ByteBuffer vbb = ByteBuffer.allocateDirect(V.length * 4); 
        vbb.order(ByteOrder.nativeOrder());
	VB = vbb.asFloatBuffer(); 
	VB.put(V);
        VB.position(0);

	// square colors
	ByteBuffer cbb = ByteBuffer.allocateDirect(_colors.length * 4); 
        cbb.order(ByteOrder.nativeOrder());
        CB = cbb.asFloatBuffer(); 
        CB.put(_colors);
        CB.position(0); 
    }

}