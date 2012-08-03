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

package com.wentam.defcol.palette;

import com.wentam.defcol.R;
import com.wentam.defcol.PaletteFile;

import android.view.View;
import android.view.MotionEvent;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import android.app.AlertDialog;

import android.util.AttributeSet;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

import java.lang.Math;
import java.lang.Integer;
import java.lang.Exception;
import java.lang.String;

import com.wentam.defcol.colorpicker.colorPickerActivity;

public class RadialPaletteView extends View {
    private int radius;
    private ArrayList<Integer> colors;
    private boolean downOnAddNew = false;
    private PaletteFile paletteFile;
    private int palette_id;

    private float last_known_touch_x;
    private float last_known_touch_y;

    private boolean ignoreLongClick = false;
    private boolean ignoreClick = false;

    private Context mContext;

    private Bitmap plus;

    private boolean plus_resized = false;

    public RadialPaletteView(final Context context) {
	super(context);
	init(context);
    }

    public RadialPaletteView(final Context context, AttributeSet a) {
	super(context, a);
	init(context);
    }

    public void init (final Context context) {
	paletteFile = new PaletteFile();
	mContext = context;

	plus = BitmapFactory.decodeResource(context.getResources(), R.drawable.plus);
    }

    // MUST be called during an onCreate()
    public void setColors(String[] colors_string) {
	colors = new ArrayList<Integer>();

	for (String color : colors_string) {
	    colors.add(Integer.parseInt(color, 16)+0xFF000000);
	}

	invalidate();
    }

    public String[] getColors() {
	// int[] intcols = colors.toArray(new int[colors.size()]);

	int[] intcols = convertIntegers(colors);

	String[] stringcols = new String[colors.size()];
	int i = 0;
	for (int color : intcols) {
	    stringcols[i] = String.format("%06X", (0xFFFFFF & color));
	    i++;
	}
	
	return stringcols;
    }

    // MUST be called during an onCreate()
    public void setPalette(int pal) {
	palette_id = pal;

	final Context context = mContext;

	// this is a strange place for it, but this is the only place in the view's flow that we can define our onLongClickListener, as it's the only time we know that the view hasn't been drawn yet, and that we have enough data
	setOnLongClickListener(new OnLongClickListener()
	    {
		@Override
		public boolean onLongClick(View v)
		{
		    ignoreClick = true;
		    if (ignoreLongClick == false) {
			float center_x = getMeasuredWidth()/2;
			float center_y = (radius/2)+5;

			final int color_to_delete = touchingItem(last_known_touch_x,last_known_touch_y,center_x,center_y, false);
			final CharSequence[] items = {"Delete"};

			AlertDialog.Builder builder = new AlertDialog.Builder(context)
			    .setItems(items,
				      new DialogInterface.OnClickListener() {
					  public void onClick(DialogInterface dialog, int index) {
					      if (colors.size() > 1) {
						  colors.remove(color_to_delete);
						  invalidate();
						  paletteFile.deleteColor(palette_id, color_to_delete);
					      }
					  }
				      });
		    
			builder.show();

			return true;    
		    } else {
			return false;
		    }
		}
	    });
    }

    protected void onDraw(Canvas canvas) {
	super.onDraw(canvas);

	if (getMeasuredWidth() > getMeasuredHeight()) {
	    radius = getMeasuredHeight()-10;
	} else {
	    radius = getMeasuredWidth()-10;
	}

	if (plus_resized == false) {
	    plus = getResizedBitmap(plus, (radius/10),  (radius/10));
	    plus_resized = true;
	}
	
	
	Paint paint = new Paint();
		
	paint.setAntiAlias(true);
	paint.setTextSize(25);

	//int[] colors = {0xFF001100, 0xFF112211, 0xFF223322, 0xFF334433, 0xFF445544, 0xFF556655, 0xFF667766, 0xFF778877};

	if (colors.size() == 1) {
	    paint.setColor(colors.get(0));
	    canvas.drawArc(new RectF((getMeasuredWidth()/2)-(radius/2), 5, (getMeasuredWidth()/2)+(radius/2), radius), 0, 360, true, paint);
	} else {
	    float sweep_angle = (new Float(360)/new Float(colors.size()))-1; // because an int divided by an int will always return an int, we need to convert the numbers to floats first
	    int loopcount = 0;
	    
	    Iterator iterator = colors.iterator();
	    while (iterator.hasNext()) {
	    	paint.setColor(Integer.parseInt(iterator.next().toString()));
	    	canvas.drawArc(new RectF((getMeasuredWidth()/2)-(radius/2), 5, (getMeasuredWidth()/2)+(radius/2), radius), (loopcount*sweep_angle)+(1*loopcount), sweep_angle, true, paint);
	    	loopcount++;
	    }
	}

	// draw circle in middle
	paint.setColor(0xFF666666);
	canvas.drawCircle(getMeasuredWidth()/2, (radius/2)+5, getMeasuredHeight()/10, paint);

	// draw plus
	canvas.drawBitmap(plus, (getMeasuredWidth()/2)-plus.getWidth()/2, ((radius/2)+5)-plus.getHeight()/2, paint);
    }

    public int getRadius() {
	return radius;
    }

    private float gestureStartX;
    private float gestureStartY;
    private int draggingOn = -1;
    private int draggingOnStart = -1;
    
    private int click_start = -1;

    private OnColorClickListener ColorClickListener;

    public void setOnColorClickListener (OnColorClickListener listener) {
	ColorClickListener = listener;
    }

    public boolean onTouchEvent (MotionEvent event) {
	int action = event.getAction();

	float x = event.getX();
	float y = event.getY();

	float center_x = getMeasuredWidth()/2;
	float center_y = (radius/2)+5;

	switch(action){
	case MotionEvent.ACTION_DOWN:
	    last_known_touch_y = y;
	    last_known_touch_x = x;

	    click_start = touchingItem(x,y,center_x,center_y, false);
	    
	    draggingOn = -1;

	    gestureStartX = new Float(x);
	    gestureStartY = new Float(y);

	    if (distanceBetweenTwoPoints(x,y, center_x, center_y) < getMeasuredHeight()/10) {
		downOnAddNew = true;
	    }
	    break;
	case MotionEvent.ACTION_UP:
	    if (click_start != -1 && ignoreClick == false && touchingItem(x,y,center_x,center_y, false) == click_start) {
		ColorClickListener.click(click_start, colors.get(click_start));

		click_start = -1;
	    } else {
		ignoreClick = false;
	    }

	    if (draggingOn != -1) {
		// final visual swap to avoid file differing from visual version
		int itemToSwapWith = touchingItem(x,y,center_x,center_y, true);
		if (itemToSwapWith != draggingOn) {
		    int targetCol = new Integer((int) colors.get(itemToSwapWith));
		    int SourceCol = new Integer((int) colors.get(draggingOn));
		    colors.set(itemToSwapWith, SourceCol);
		    colors.set(draggingOn, targetCol);
		    
		    draggingOn = itemToSwapWith;

		    invalidate();
		}

		// write new colors to file
		if (itemToSwapWith != draggingOnStart) {
		    paletteFile.setColors(palette_id, colors);
		}

		ignoreLongClick = false;

		draggingOn = -1;
	    }

	    if (distanceBetweenTwoPoints(x,y, center_x, center_y) < getMeasuredHeight()/10 && downOnAddNew == true) {
		addColor();
		downOnAddNew = false;
	    }
	    break;	
	case MotionEvent.ACTION_MOVE:

	    if (draggingOn == -1) {
		if (gestureStartX-x > 1 || gestureStartX-x < -1 && gestureStartY-y > 1 || gestureStartY-y < -1) {
		    draggingOn = touchingItem(x,y,center_x,center_y, false);
		    draggingOnStart = new Integer((int) draggingOn);
		}
	    }

	    if (draggingOn > -1) {	       
		int itemToSwapWith = touchingItem(x,y,center_x,center_y, true);
		if (itemToSwapWith != draggingOn) {
		    
		    ignoreLongClick = true;
		    ignoreClick = true;

		    // try/catch is a tmp fix for random crash (ArrayIndexOutOfBoundsException)
		    try {
			int targetCol = new Integer((int) colors.get(itemToSwapWith));
			int SourceCol = new Integer((int) colors.get(draggingOn));
			colors.set(itemToSwapWith, SourceCol);
			colors.set(draggingOn, targetCol);
		    } catch (Exception e) {}
		    
		    draggingOn = itemToSwapWith;

		    invalidate();
		}
	    }

	    //touchingItem(x,y,center_x,center_y);
	    break;
	default:
	}
	super.onTouchEvent(event);
	return true;
    }

    private int touchingItem(float x,float y,float center_x,float center_y, boolean ignoreDistance) {
	float distance = distanceBetweenTwoPoints(x,y, center_x, center_y);
	float angle = angleBetweenTwoPoints(x,y, center_x, center_y);
	int item = -1; // the item we are touching

	if ((distance > getMeasuredHeight()/10 && distance < radius/2) || ignoreDistance == true) {

	    int sections = colors.size();
	    float sweep_angle = new Float(360)/new Float(sections); // calculate our own sweep angle, to remove the gaps in the calculation

	    int i2 = sections-1;
	    for (int i = 0; i<sections; i++) {
		if (i*sweep_angle < angle && (i*sweep_angle)+sweep_angle > angle) {
		    item = i2;
		}
		i2--;
	    }
	}	

	return item;
    }

    private float distanceBetweenTwoPoints (float x1, float y1, float x2, float y2) {
	return (float) Math.sqrt(Math.pow(x2-x1,2)+Math.pow(y2-y1,2));
    }

    private float angleBetweenTwoPoints (float x1, float y1, float x2, float y2) {
	float raw = (float) Math.toDegrees(Math.atan2(x1-x2,y1-y2))-90;

	// atan2 gives us two 180 degree (technically radians, but I convert them afterwards) halves, one negative and one positive. I want this in a 360 degree format.
	if (raw < 0) {
	    return raw+360;
	} else {
	    return raw;
	}
    }

    private void addColor() {
	colors.add(0xFF000000);
	paletteFile.addNewColor(palette_id,"000000");
	invalidate();
    }

    private Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {
	int width = bm.getWidth();
	int height = bm.getHeight();

 	float scaleWidth = ((float) newWidth) / width;
	float scaleHeight = ((float) newHeight) / height;

 	Matrix matrix = new Matrix();
	matrix.postScale(scaleWidth, scaleHeight);


	Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);

 	return resizedBitmap;
   }


    private static int[] convertIntegers(List<Integer> integers)
    {
	int[] ret = new int[integers.size()];
	for (int i=0; i < ret.length; i++)
	    {
		ret[i] = integers.get(i).intValue();
	    }
	return ret;
    }

}