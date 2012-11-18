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

import android.graphics.Color;

import android.util.Log;
import android.util.DisplayMetrics;

import android.os.SystemClock;


public class h_svRenderer implements GLSurfaceView.Renderer {
    private boolean circleShowing = false;

    private int circleXstart = 0;
    private int circleYstart = 0;
    private int circleX = 0;
    private int circleY = 0;
    private float circleSize = 0;

    private int width;
    private int height;

    private float onePixelWidth = 0f;
    private float onePixelHeight = 0f;

    private sv_2d_gradient sv_2d_gradient;
    private hueBar hueBar;

    private circle circle;
    private circle selected_sv_circle;

    private rect hueRect;
    private float hueRectWidth;

    private int dpi;

    private float hue; public float getHue(){return hue;}
    private float _saturation; public float getSaturation(){return _saturation;}
    private float _value; public float getValue(){return _value;}


    // color change task that gets ran in onSurfaceChanged
    private boolean doSetColorJob = false;
    private float colorJobHue;
    private float colorJobSaturation;
    private float colorJobValue;

    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
	gl.glEnableClientState(GL10.GL_VERTEX_ARRAY); // enable vertex arrays
	gl.glEnableClientState(GL10.GL_COLOR_ARRAY); // enable color arrays
	gl.glShadeModel(GL10.GL_SMOOTH);

        gl.glClearColor(0,0,0,0);
    }

    public void setColor(float[] color) {
        colorJobHue = color[0];
	colorJobSaturation = color[1];
        colorJobValue = color[2];       

	doSetColorJob = true;
    }

    public void showCircle() {
	circleShowing = true;
    }
    public void hideCircle() {
	circleShowing = false;
    }

    private boolean pointInsideMainGradient (int x, int y) {
	if (sv_2d_gradient == null) { 
	    return false;
	}

	// left
	float left = (sv_2d_gradient.getLeft())/onePixelWidth;
	int leftmargin = (int)(left+(width/2))+1;

	boolean result = true;

	if ((x < leftmargin)) {
	    result = false;
	}

	
	// right
	float right = (sv_2d_gradient.getRight())/onePixelWidth;
	int rightmargin = (int) (right+(width/2));

	if ((x > rightmargin)) {
	    result = false;
	}

	// top
	float top = (sv_2d_gradient.getTop())/onePixelHeight;
	int topmargin = (int) Math.abs((top-(height/2)));

	if ((y < topmargin)) {
	    result = false;
	}

	// bottom 
	float bottom = (sv_2d_gradient.getBottom())/onePixelHeight;
	int bottommargin = (int) Math.abs((bottom-(height/2)));
	
	
	if ((y > bottommargin)) {
	    result = false;
	}
	
	return result;
    }

    public boolean pointInsideHueSlider (int x, int y) {
	if (hueRect == null) {
	    return false;
	}

	// left
	float left = (hueBar.getLeft())/onePixelWidth;
	int leftmargin = (int)(left+(width/2))+1;

	boolean result = true;

	if ((x < leftmargin)) {
	    result = false;
	}

	// right
	float right = (hueBar.getRight())/onePixelWidth;
	int rightmargin = (int) (right+(width/2));

	if ((x > rightmargin)) {
	    result = false;
	}

	// top
	float top = (hueBar.getTop())/onePixelHeight;
	int topmargin = (int) (top-(height/2));

	if ((y < topmargin)) {
	    result = false;
	}

	// // bottom 
	// float bottom = (hueRect.getBottom())/onePixelHeight;
	// int bottommargin = (int) Math.abs((bottom-(height/2)));
	
	
	// if ((y > bottommargin)) {
	//     result = false;
	// }
	
	return result;
    } 

    public void startMoveCircle(float x, float y) {
	circleXstart = (int) x;
	circleYstart = (int) y;
    }    

    public void moveCircle(float x, float y, float size) {
	if (pointInsideMainGradient(circleXstart, circleYstart)) {
	    
	    circleX = (int) x;
	    circleY = (int) y;
	    circleSize =  size;

	    if (sv_2d_gradient != null) {
		// handle edges

		// left
		float left = (sv_2d_gradient.getLeft())/onePixelWidth;
		int leftmargin = (int)(left+(width/2))+1;
	
		if (circleX < leftmargin) {
		    circleX = (int) leftmargin;
		}


		// right
		float right = (sv_2d_gradient.getRight())/onePixelWidth;
		int rightmargin = (int) (right+(width/2));

		if (circleX > rightmargin) {
		    circleX = (int) rightmargin;
		}

		// top
		float top = (sv_2d_gradient.getTop())/onePixelHeight;
		int topmargin = (int) Math.abs((top-(height/2)));

		if (circleY < topmargin) {
		    circleY = (int) topmargin;
		}

		// bottom 
		float bottom = (sv_2d_gradient.getBottom())/onePixelHeight;
		int bottommargin = (int) Math.abs((bottom-(height/2)));


		if (circleY > bottommargin) {
		    circleY = (int) bottommargin;
		}

		float value = (sv_2d_gradient.getValues()[circleX-leftmargin]);
		float saturation = (sv_2d_gradient.getSaturations()[circleY-topmargin]);
		// float hue = sv_2d_gradient.getHue();


		value = reverseFloatWithRange(value, 0f, 1f);

		if (value > 1f) {
		    value = 1f;
		}

		if (value < 0.0000001f) {
		    value = 0f;
		}

		_value = value;
		_saturation = saturation;
	    }
	} else if (pointInsideHueSlider (circleXstart, circleYstart)) {

	    // calculate new location
	    float left = (hueBar.getLeft())/onePixelWidth;
	    int leftmargin = (int)(left+(width/2))+1;

	    float right = (hueBar.getRight())/onePixelWidth;
	    int rightmargin = (int)(right+(width/2))+1;

	    int bottom_margin_dp = 20;
	    int b_px = bottom_margin_dp*(dpi/160);       
	    float bottom_margin = heightPxToFloat(b_px);

	    int screen_height_dp = height/(dpi/160);

	    int top_margin_dp = (screen_height_dp)-90;
	    int px = (top_margin_dp*(dpi/160));
	    float top_margin = heightPxToFloat(px);

	    float screen_width_f = floatFromPx(width, 0);

	    
	    float x2;
	    if (x<=leftmargin+1) {
		x=leftmargin+1;
		x2 = floatFromPx((int)x, 0);
	    } else {
		x2 = floatFromPx((int)x, 0);
	    }
	    
	    if (x>=rightmargin-1) {
	    	x=rightmargin-1;
	    	x2 = floatFromPx((int)x, 0);
	    } else {
	    	x2 = floatFromPx((int)x, 0);
	    }

	    float new_left = ((-(hueRectWidth))+x2)-screen_width_f/2;
	    float new_top = -1f+bottom_margin;
	    float new_right = (((hueRectWidth))+x2)-screen_width_f/2;
	    float new_bottom = 1f-top_margin;

	    hueRect.setLocation(new_left, new_top, new_right, new_bottom);

	    // set hue for main gradient
	    float hue1 = floatFromPx((int)(x-leftmargin),0);	   

	    hue1 = ((hue1/floatFromPx(hueBar.getWidth(),0)));

	    float hueReal = hue1*360f;

	    hueReal = reverseFloatWithRange(hueReal,0f, 360f);
	    
	    sv_2d_gradient.updateHue(hueReal);

	    // Log.i("DEFCOL","current hue: "+hueReal);
	    hue = hueReal;
	}
       


    }

    private float reverseFloatWithRange (float f, float min, float max) {
	float middle;

	if (min == 0f) {
	    middle = max/2;
	} else {
	    middle = max/min; // (max-min)/2?
	}
	
	if (f > middle) {
	    f = f-((f-middle)*2);
	} else if (f < middle) {
	    f = f+((middle-f)*2);
	}

	return f;
    }

    
    // DRAW-DRAW-DRAW-DRAW-DRAW!! *spins in chair with pencil on head*
    public void onDrawFrame(GL10 gl) {
	gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

	// draw main box \\
	sv_2d_gradient.draw();



	// if (circleShowing == true) {
	//     circle.updateLocation(circleX, circleY, 13f);
	//     circle.draw();
	// }

	selected_sv_circle.updateLocation(circleX, circleY, 0.1f);
	selected_sv_circle.draw();

	hueBar.draw();
	hueRect.draw();
    }

    public void setDpi(int DPI) {
	dpi = DPI;
    }
    
    public void onSurfaceChanged(GL10 gl, int _width, int _height) {
	// send to gl
        gl.glViewport(0, 0, _width, _height);

	// save data in memory
	width = _width;
	height = _height;

	onePixelWidth = (new Float(1)/new Float(width))*2;
	onePixelHeight = (new Float(1)/new Float(height))*2;

	// calculate top margin
	int top_margin_dp = 20;
	int px = top_margin_dp*(dpi/160);       
	float top_margin = heightPxToFloat(px);

	// calculate bottom margin
	int bottom_margin_dp = 100;
	int b_px = bottom_margin_dp*(dpi/160);       
	float bottom_margin = heightPxToFloat(b_px);

	// create gradient
	sv_2d_gradient = new sv_2d_gradient(gl, -0.9f, 0.9f, -1f+bottom_margin, 1f-top_margin, 250f, onePixelWidth, onePixelHeight);
	

	// calculate bottom margin
	bottom_margin_dp = 20;
	b_px = bottom_margin_dp*(dpi/160);       
	bottom_margin = heightPxToFloat(b_px);

	int screen_height_dp = height/(dpi/160);

	top_margin_dp = (screen_height_dp)-90;
	px = (top_margin_dp*(dpi/160));
	top_margin = heightPxToFloat(px);

	hueRectWidth = (5*onePixelWidth)/2;

	// create hue bar
	hueBar = new hueBar(gl, -0.9f, 0.9f, -1f+bottom_margin, 1f-top_margin, onePixelWidth, onePixelHeight);

	// create circle
	circle = new circle(gl, 0.5f, 0.5f, 0.5f, 2.0f, onePixelWidth, onePixelHeight, width, height);
	selected_sv_circle = new circle(gl, 0.5f, 0.5f, 0.1f, 2.0f, onePixelWidth, onePixelHeight, width, height);

	// create hueRect
	hueRect = new rect(gl, -0.9f-(hueRectWidth), -1f+bottom_margin, -0.9f+(hueRectWidth), 1f-top_margin,  2f, onePixelWidth, onePixelHeight, width, height);


	if (doSetColorJob == true) {
	    float screen_width_f = floatFromPx(width, 0);

	    // hue
	    float scaledToOne = colorJobHue/360f;

	    float x3 = (scaledToOne*(hueBar.getWidth()))+((((1f-Math.abs(hueBar.getLeft())))/onePixelWidth));
	    x3 = reverseFloatWithRange(x3, 0f, width);

	    circleXstart = (int) (width/2);
	    circleYstart = (int) (height-bottom_margin);
	    moveCircle(x3, (height-bottom_margin), 0.5f);

	    // saturation and value
	    if (colorJobValue == 0f && colorJobSaturation == 0f) {
		int left_margin = (int)(((1f-Math.abs(hueBar.getLeft())))/onePixelWidth);
		
		top_margin_dp = 20;
		px = top_margin_dp*(dpi/160);       
		top_margin = px;

		x3 = left_margin;;
		float y3 = top_margin+sv_2d_gradient.getHeight();

		circleXstart = (int) x3+1;
		circleYstart = (int) y3;
		moveCircle(x3+1, y3, 0.5f);
	    } else {

		top_margin_dp = 20;
		px = top_margin_dp*(dpi/160);       
		top_margin = px;

		scaledToOne = colorJobSaturation;

		int left_margin = (int)(((1f-Math.abs(hueBar.getLeft())))/onePixelWidth);

		x3 = (scaledToOne*(hueBar.getWidth()));
		x3 = reverseFloatWithRange(x3, 0f, hueBar.getWidth());
	    
		x3 += left_margin;

		scaledToOne = colorJobValue;
		float y3 = (scaledToOne*(sv_2d_gradient.getHeight()));
		y3 = reverseFloatWithRange(y3, 0f, sv_2d_gradient.getHeight());

		y3 += top_margin;
	    
		// x3 = reverseFloatWithRange(x3, 0f, width);

		circleXstart = (int) x3+1;
		circleYstart = (int) y3;
		moveCircle(x3+1, y3, 0.5f);
	    }
	}

    }

    public float heightPxToFloat(int px) { // deprecated
	float f = 0.0f;
	if (px != 0) {
	    for (int i = 0; i < px; i++) {
		f += onePixelHeight;
	    }
	}

	return f;
    }
  
    public float floatFromPx (int px, int axis) {
	float result = 0f;

	if (px != 0) {
	    for (int i = 0; i < px; i++) {
		if (axis == 0) {
		    result += onePixelWidth;
		} else {
		    result += onePixelHeight;
		}
	    }
	}

	return result;
    }

}