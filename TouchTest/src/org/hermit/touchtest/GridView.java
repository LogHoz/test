
/**
 * Touch Test: a multi-touch test app for Android.
 * <br>Copyright 2010 Ian Cameron Smith
 *
 * <p>This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2
 * as published by the Free Software Foundation (see COPYING).
 * 
 * <p>This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */


package org.hermit.touchtest;


import java.util.HashMap;

import org.hermit.android.core.SurfaceRunner;
import org.hermit.utils.CharFormatter;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.view.Display;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.WindowManager;


/**
 * The main touch test view.  This class relies on the parent SurfaceRunner
 * class to do the bulk of the animation control.
 */
class GridView
    extends SurfaceRunner
{

    // ******************************************************************** //
    // Constructor.
    // ******************************************************************** //

	/**
	 * Create a GridView instance.
	 * 
	 * @param	app			The application context we're running in.
	 */
    public GridView(Context context) {
        super(context, SURFACE_DYNAMIC | LOOPED_TICKER);

        setDelay(1000);
        
        appResources = context.getResources();
    	windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    	appDisplay = windowManager.getDefaultDisplay();

        trackedPointers = new Pointer[MAX_POINTERS];
        for (int i = 0; i < MAX_POINTERS; ++i)
            trackedPointers[i] = new Pointer();

        paint = new Paint();
        paint.setTextSize(20f);
        paint.setTypeface(Typeface.MONOSPACE);
        
        eventBuf = new char[11];
    }

    
    // ******************************************************************** //
    // Client Methods.
    // ******************************************************************** //

    /**
     * The application is starting.  Perform any initial set-up prior to
     * starting the application.  We may not have a screen size yet,
     * so this is not a good place to allocate resources which depend on
     * that.
     */
    protected void appStart() {
    }
    

    /**
     * Set the screen size.  This is guaranteed to be called before
     * animStart(), but perhaps not before appStart().
     * 
     * @param   width       The new width of the surface.
     * @param   height      The new height of the surface.
     * @param   config      The pixel format of the surface.
     */
    protected void appSize(int width, int height, Bitmap.Config config) {
        appConfig = appResources.getConfiguration();
        sw = width;
        sh = height;

        sizeStr = appDisplay.getWidth() + "x" + appDisplay.getHeight();
        confStr = translateToken(appConfig.orientation, CONFIG_ORIENTS);
        rotateStr = translateToken(appDisplay.getOrientation(), DISP_ORIENTS);

        // Create the edge markers.
        edgeMarker = new Path();
        float x, y;
        
        // Top
        x = sw / 4 + 0.5f;
        y = 0;
        edgeMarker.moveTo(x, y);
        edgeMarker.lineTo(x + ARROW_SIZE, y + ARROW_SIZE);
        edgeMarker.lineTo(x - ARROW_SIZE, y + ARROW_SIZE);
        edgeMarker.close();
        x = sw - x;
        y = 0;
        edgeMarker.moveTo(x, y);
        edgeMarker.lineTo(x + ARROW_SIZE, y + ARROW_SIZE);
        edgeMarker.lineTo(x - ARROW_SIZE, y + ARROW_SIZE);
        edgeMarker.close();
        
        // Right
        x = sw;
        y = sh / 4 + 0.5f;
        edgeMarker.moveTo(x, y);
        edgeMarker.lineTo(x - ARROW_SIZE, y - ARROW_SIZE);
        edgeMarker.lineTo(x - ARROW_SIZE, y + ARROW_SIZE);
        edgeMarker.close();
        x = sw;
        y = sh - y;
        edgeMarker.moveTo(x, y);
        edgeMarker.lineTo(x - ARROW_SIZE, y - ARROW_SIZE);
        edgeMarker.lineTo(x - ARROW_SIZE, y + ARROW_SIZE);
        edgeMarker.close();
        
        // Bottom
        x = sw / 4 + 0.5f;
        y = sh;
        edgeMarker.moveTo(x, y);
        edgeMarker.lineTo(x + ARROW_SIZE, y - ARROW_SIZE);
        edgeMarker.lineTo(x - ARROW_SIZE, y - ARROW_SIZE);
        edgeMarker.close();
        x = sw - x;
        y = sh;
        edgeMarker.moveTo(x, y);
        edgeMarker.lineTo(x + ARROW_SIZE, y - ARROW_SIZE);
        edgeMarker.lineTo(x - ARROW_SIZE, y - ARROW_SIZE);
        edgeMarker.close();
        
        // Left
        x = 0f;
        y = sh / 4f + 0.5f;
        edgeMarker.moveTo(x, y);
        edgeMarker.lineTo(x + ARROW_SIZE, y - ARROW_SIZE);
        edgeMarker.lineTo(x + ARROW_SIZE, y + ARROW_SIZE);
        edgeMarker.close();
        x = 0f;
        y = sh - y;
        edgeMarker.moveTo(x, y);
        edgeMarker.lineTo(x + ARROW_SIZE, y - ARROW_SIZE);
        edgeMarker.lineTo(x + ARROW_SIZE, y + ARROW_SIZE);
        edgeMarker.close();
        
        edgeMarker.close();
    }
    

    /**
     * We are starting the animation loop.  The screen size is known.
     * 
     * <p>doUpdate() and doDraw() may be called from this point on.
     */
    protected void animStart() {
    }
    

    /**
     * We are stopping the animation loop, for example to pause the app.
     * 
     * <p>doUpdate() and doDraw() will not be called from this point on.
     */
    protected void animStop() {
    }
    

    /**
     * The application is closing down.  Clean up any resources.
     */
    protected void appStop() {
    }
    
    
    // ******************************************************************** //
    // Input Handling.
    // ******************************************************************** //
    
    /**
	 * Handle touchscreen input.
	 * 
     * @param	event		The MotionEvent object that defines the action.
     * @return				True if the event was handled, false otherwise.
	 */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        lastActions[lastActionsIndex++] = action;
        lastActionsIndex %= STORED_ACTIONS;

        int npointers = event.getPointerCount();

        // Get the action and pointer ID.
        int pact = action & ~MotionEvent.ACTION_POINTER_ID_MASK;
        int pid = (action & MotionEvent.ACTION_POINTER_ID_MASK) >> MotionEvent.ACTION_POINTER_ID_SHIFT;

        // Update the up/down state of this pointer.
        if (pact == MotionEvent.ACTION_DOWN || pact == MotionEvent.ACTION_POINTER_DOWN) {
            trackedPointers[pid].down = true;
        } else if (pact == MotionEvent.ACTION_UP || pact == MotionEvent.ACTION_POINTER_UP) {
            trackedPointers[pid].down = false;
        } else if (pact == MotionEvent.ACTION_MOVE) {
            trackedPointers[pid].down = true;
        }

        // Save the pointer states for all the pointers.
        for (int i = 0; i < npointers; ++i) {
            int p = event.getPointerId(i);
            Pointer rec = trackedPointers[p];
            
            rec.seen = true;
            rec.x = event.getX(i);
            rec.y = event.getY(i);
            rec.size = event.getSize(i);
        }

        postUpdate();
        
        return true;
    }

    
    // ******************************************************************** //
    // Animation.
    // ******************************************************************** //

    /**
     * Update the state of the application for the current frame.
     * 
     * <p>Applications must override this, and can use it to update
     * for example the physics of a game.  This may be a no-op in some cases.
     * 
     * <p>doDraw() will always be called after this method is called;
     * however, the converse is not true, as we sometimes need to draw
     * just to update the screen.  Hence this method is useful for
     * updates which are dependent on time rather than frames.
     * 
     * @param   now         Current time in ms.
     */
    protected void doUpdate(long now) {
    	
    }

    
    /**
     * Draw the current frame of the application.
     * 
     * <p>Applications must override this, and are expected to draw the
     * entire screen into the provided canvas.
     * 
     * <p>This method will always be called after a call to doUpdate(),
     * and also when the screen needs to be re-drawn.
     * 
     * @param   canvas      The Canvas to draw into.
     * @param   now         Current time in ms.  Will be the same as that
     *                      passed to doUpdate(), if there was a preceeding
     *                      call to doUpdate().
     */
    protected void doDraw(Canvas canvas, long now) {
        canvas.drawColor(0xff000000);
        
        // Draw the touch grid.
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.5f);
        paint.setColor(0xff0060b0);
        for (int x = 0; x < sw; x += GRID_SPACING)
            canvas.drawLine(x, 0, x, sh, paint);
        for (int y = 0; y < sh; y += GRID_SPACING)
            canvas.drawLine(0, y, sw, y, paint);
        
        // Put an outline around the screen.
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(0f);
        paint.setColor(0xffffff00);
        canvas.drawRect(0, 0, sw - 1, sh - 1, paint);
        
        // Draw the edge markers.
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xffe08000);
        canvas.drawPath(edgeMarker, paint);

        // Draw the user's fingers.
        for (int i = 0; i < MAX_POINTERS; ++i) {
            Pointer rec = trackedPointers[i];
            if (!rec.seen)
                continue;
        	int col = POINTER_COLOURS[i % POINTER_COLOURS.length];
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(rec.down ? col : 0x80ffffff);
            canvas.drawCircle(rec.x, rec.y, rec.size * 2 + 24, paint);
            
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(1f);
            canvas.drawLine(rec.x, 0, rec.x, sh, paint);
            canvas.drawLine(0, rec.y, sw, rec.y, paint);
        }
        
        // Draw the display configuration.
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(0f);
        paint.setColor(0xffffa0a0);
        int y = 70;
        canvas.drawText(sizeStr, 50, y, paint);
        y += 22;
        canvas.drawText(confStr, 50, y, paint);
        y += 22;
        canvas.drawText(rotateStr, 50, y, paint);
        y += 22;
        
        // Draw the most recent action codes.
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(0f);
        paint.setColor(0xffffa0a0);
        y = 70;
        for (int i = 0; i < STORED_ACTIONS; ++i) {
            int code = lastActions[(lastActionsIndex + i) % STORED_ACTIONS];
            CharFormatter.formatInt(eventBuf, 0, code, 4, false);
            CharFormatter.formatString(eventBuf, 4, " 0x", 3);
            CharFormatter.formatHex(eventBuf, 7, code, 4);
            canvas.drawText(eventBuf, 0, 11, sw - 180, y, paint);
            y += 22;
        }
    }

    
    private String translateToken(int val, HashMap<Integer, String> map) {
    	String name = map.get(val);
    	if (name != null)
    		return name;
    	return "?<" + val + ">?";
    }
    

	// ******************************************************************** //
	// Class Data.
	// ******************************************************************** //

    // Debugging tag.
	@SuppressWarnings("unused")
	private static final String TAG = "TouchTest";

	private static final HashMap<Integer, String> CONFIG_ORIENTS =
										new HashMap<Integer, String>();
	static {
		CONFIG_ORIENTS.put(Configuration.ORIENTATION_LANDSCAPE, "Landscape");
		CONFIG_ORIENTS.put(Configuration.ORIENTATION_PORTRAIT, "Portrait");
		CONFIG_ORIENTS.put(Configuration.ORIENTATION_SQUARE, "Square");
		CONFIG_ORIENTS.put(Configuration.ORIENTATION_UNDEFINED, "Undefined");
	}

	private static final HashMap<Integer, String> DISP_ORIENTS =
										new HashMap<Integer, String>();
	static {
		DISP_ORIENTS.put(Surface.ROTATION_0, "0 - Upright");
		DISP_ORIENTS.put(Surface.ROTATION_90, "90 - Left");
		DISP_ORIENTS.put(Surface.ROTATION_180, "180 - Inverted");
		DISP_ORIENTS.put(Surface.ROTATION_270, "270 - Right");
	}

    private static final int GRID_SPACING = 50;
    
    private static final int ARROW_SIZE = 40;
    
    private static final int STORED_ACTIONS = 10;
   
    private static final int MAX_POINTERS = 6;
    private class Pointer {
        boolean seen = false;
        boolean down = false;
        float x = 0;
        float y = 0;
        float size = 0;
    }
    
    private static final int[] POINTER_COLOURS = {
    	0xffffa000, 0xff00ffff, 0xff80ff00, 0xff80ff80,
    };

    
	// ******************************************************************** //
	// Private Data.
	// ******************************************************************** //

    // Our app resources, and current configuration.
	private Resources appResources;
	private Configuration appConfig;
	
	// Our window manager and display.
	private WindowManager windowManager;
	private Display appDisplay;

    // Screen width and height.
    private int sw = 0, sh = 0;

    // Current configuration info.
    private String sizeStr;
    private String confStr;
    private String rotateStr;
    
    // Path for drawing the edge markers.
	private Path edgeMarker;

    // Most recently reported touch event action.
    private int[] lastActions = new int[STORED_ACTIONS];
    private int lastActionsIndex = 0;
    
    // Tracked pointer states.
    private Pointer[] trackedPointers = null;
    
    // Paint used for drawing.
    private Paint paint;
    
    // Character buffer for text output.
    private char[] eventBuf = null;

}
