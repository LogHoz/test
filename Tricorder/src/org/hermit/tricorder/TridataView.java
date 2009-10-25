
/**
 * Tricorder: turn your phone into a tricorder.
 * 
 * This is an Android implementation of a Star Trek tricorder, based on
 * the phone's own sensors.  It's also a demo project for sensor access.
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License version 2
 *   as published by the Free Software Foundation (see COPYING).
 * 
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 */


package org.hermit.tricorder;

import org.hermit.tricorder.Tricorder.Sound;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.MotionEvent;
import android.view.SurfaceHolder;


/**
 * A view which displays 3-axis sensor data in a variety of ways.
 * 
 * This could be used, for example, to show the accelerometer or
 * compass values.
 */
class TridataView
	extends DataView
	implements SensorEventListener
{

	// ******************************************************************** //
	// Constructor.
	// ******************************************************************** //

	/**
	 * Set up this view.
	 * 
	 * @param	context			Parent application context.
     * @param	sh				SurfaceHolder we're drawing in.
	 * @param	sman			The SensorManager to get data from.
	 * @param	sensor			The ID of the sensor to read:
	 * 							Sensor.TYPE_XXX.
	 * @param	unit			The size of a unit of measure (for example,
	 * 							1g of acceleration).
	 * @param	range			How many units big to make the graph.
	 * @param	gridCol1		Colour for the graph grid in abs mode.
	 * @param	plotCol1		Colour for the graph plot in abs mode.
	 * @param	gridCol2		Colour for the graph grid in rel mode.
	 * @param	plotCol2		Colour for the graph plot in rel mode.
	 */
	public TridataView(Tricorder context, SurfaceHolder sh,
					   SensorManager sman, int sensor,
					   float unit, float range,
					   int gridCol1, int plotCol1,
					   int gridCol2, int plotCol2)
	{
		super(context, sh);
		
		appContext = context;
		surfaceHolder = sh;
		sensorManager = sman;
		sensorId = sensor;
		dataUnit = unit;
		dataRange = range;
		gridColour1 = gridCol1;
	    plotColour1 = plotCol1;
		gridColour2 = gridCol2;
	    plotColour2 = plotCol2;

		processedValues = new float[3];
        
        // See if the sensor is actually equipped.
        sensorEquipped = (sensorManager.getSensors() & sensorId) != 0;

        // Add the gravity 3-axis plot.
        plotView = new AxisElement(context, sh, unit, range,
				 				   gridCol1, plotCol1, new String[] { "" });

        // Add the gravity magnitude chart.
        chartView = new MagnitudeElement(context, sh, unit, range,
        								 gridCol1, plotCol1,
        								 new String[] { "" });

        // Add the numeric display.
        numView = new Num3DElement(context, sh, gridCol1, plotCol1);
        
        xyzView = new MagnitudeElement(context, sh, 3, unit, range,
				 					   gridCol1, XYZ_PLOT_COLS,
				 					   new String[] { "" }, true);

        setRelativeMode(false);
	}


    // ******************************************************************** //
	// Geometry Management.
	// ******************************************************************** //

    /**
     * This is called during layout when the size of this element has
     * changed.  This is where we first discover our size, so set
     * our geometry to match.
     * 
	 * @param	bounds		The bounding rect of this element within
	 * 						its parent View.
     */
	@Override
	protected void setGeometry(Rect bounds) {
		super.setGeometry(bounds);
		
		if (bounds.right - bounds.left < bounds.bottom - bounds.top)
			layoutPortrait(bounds);
		else
			layoutLandscape(bounds);
		
		plotBounds = plotView.getBounds();
		numBounds = numView.getBounds();
	}


    /**
     * Set up the layout of this view in portrait mode.
     * 
	 * @param	bounds		The bounding rect of this element within
	 * 						its parent View.
     */
	private void layoutPortrait(Rect bounds) {
		final int pad = appContext.getInterPadding();
		final int h = bounds.bottom - bounds.top;
		
		final int plotHeight = h / 3;
		int res = h - plotHeight - pad * 2;
		final int numHeight = res / 2;
		final int chartHeight = res / 2;

		int sx = bounds.left + pad;
		int ex = bounds.right;
		int y = bounds.top;
		
		plotView.setGeometry(new Rect(sx, y, ex, y + plotHeight));
		y += plotHeight + pad;
		
		chartView.setGeometry(new Rect(sx, y, ex, y + chartHeight));
		y += chartHeight + pad;
		
		// The numeric and XYZ views are alternatives and go in the
		// same place.
		numView.setGeometry(new Rect(sx, y, ex, y + numHeight));
		xyzView.setGeometry(new Rect(sx, y, ex, y + numHeight));
	}


    /**
     * Set up the layout of this view in landscape mode.
     * 
	 * @param	bounds		The bounding rect of this element within
	 * 						its parent View.
     */
	private void layoutLandscape(Rect bounds) {
		final int pad = appContext.getInterPadding();
		final int w = bounds.right - bounds.left;
		final int h = bounds.bottom - bounds.top;

		final int sx = bounds.left + pad;
		final int ex = bounds.right;
		int x = sx;
		int y = bounds.top;
		
		int plotWidth = (w - pad) / 3;
		plotView.setGeometry(new Rect(x, y, x + plotWidth, y + h));
		x += plotWidth + pad;
		
		int chartHeight = (h - pad) / 2;
		chartView.setGeometry(new Rect(x, y, ex, y + chartHeight));
		y += chartHeight + pad;
		
		// The numeric and XYZ views are alternatives and go in the
		// same place.
		numView.setGeometry(new Rect(x, y, ex, y + chartHeight));
		xyzView.setGeometry(new Rect(x, y, ex, y + chartHeight));
	}


	// ******************************************************************** //
	// Configuration.
	// ******************************************************************** //

	/**
	 * Set whether we should simulate data for missing sensors.
	 * 
	 * @param	fakeIt			If true, sensors that aren't equipped will
	 * 							have simulated data displayed.  If false,
	 * 							they will show "No Data".
	 */
	@Override
	void setSimulateMode(boolean fakeIt) {
		// If we're not faking it, reset all the graphs.
		if (fakeIt && !sensorEquipped) {
			synchronized (surfaceHolder) {
				dataGenerator = new DataGenerator(this, sensorId, 3,
												  dataUnit, dataRange);
				plotView.setIndicator(true, 0xff0000ff);
				chartView.setIndicator(true, 0xff0000ff);
				numView.setIndicator(true, 0xff0000ff);
				xyzView.setIndicator(true, 0xff0000ff);
			}
		} else {
			synchronized (surfaceHolder) {
				dataGenerator = null;
				plotView.setIndicator(false, 0xff0000ff);
				plotView.clearValues();
				chartView.setIndicator(false, 0xff0000ff);
				chartView.clearValue();
				numView.setIndicator(false, 0xff0000ff);
				numView.clearValues();
				xyzView.setIndicator(false, 0xff0000ff);
				xyzView.clearValue();
			}
		}
	}
	
	
	/**
	 * Set or reset relative mode.  In relative mode, we report all
	 * values relative to the values that were in force when we entered
	 * it.  In absolute mode, we always report the absolute values.
	 * 
	 * We set the header fields in each part of the display to reflect
	 * the mode.
	 * 
	 * @param rel
	 */
	void setRelativeMode(boolean rel) {
		relativeMode = rel;
		relativeValues = null;
		
		if (!relativeMode) {
			plotView.setText(0, 0, getRes(R.string.title_vect_abs));
			plotView.setDataColors(gridColour1, plotColour1);
			chartView.setText(0, 0, getRes(R.string.title_mag_abs));
			chartView.setDataColors(gridColour1, plotColour1);
			numView.setText(0, 0, getRes(R.string.title_num_abs));
			numView.setDataColors(gridColour1, plotColour1);
			xyzView.setText(0, 0, getRes(R.string.title_xyz_abs));
			xyzView.setDataColors(gridColour1, XYZ_PLOT_COLS);
		} else {
			plotView.setText(0, 0, getRes(R.string.title_vect_rel));
			plotView.setDataColors(gridColour2, plotColour2);
			chartView.setText(0, 0, getRes(R.string.title_mag_rel));
			chartView.setDataColors(gridColour2, plotColour2);
			numView.setText(0, 0, getRes(R.string.title_num_rel));
			numView.setDataColors(gridColour2, plotColour2);
			xyzView.setText(0, 0, getRes(R.string.title_xyz_rel));
			xyzView.setDataColors(gridColour2, XYZ_PLOT_COLS);
		}
	}
	

	// ******************************************************************** //
	// State Management.
	// ******************************************************************** //
	
	/**
	 * Start this view.  This notifies the view that it should start
	 * receiving and displaying data.  The view will also get tick events
	 * starting here.
	 */
	@Override
	public void start() {
	    Sensor sensor = sensorManager.getDefaultSensor(sensorId);
	    if (sensor != null)
	        sensorManager.registerListener(this, sensor,
				   					       SensorManager.SENSOR_DELAY_GAME);
	}
	

	/**
	 * Stop this view.  This notifies the view that it should stop
	 * receiving and displaying data, and generally stop using
	 * resources.
	 */
	@Override
	public void stop() {
		sensorManager.unregisterListener(this);
	}
	

	// ******************************************************************** //
	// Data Management.
	// ******************************************************************** //

    /**
     * Called when the accuracy of a sensor has changed.
     * 
     * @param   sensor          The sensor being monitored.
     * @param   accuracy        The new accuracy of this sensor.
     */
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Don't need anything here.
    }


    /**
     * Called when sensor values have changed.
     *
	 * @param	event			The sensor event.
     */
	public void onSensorChanged(SensorEvent event) {
	    onSensorData(event.sensor.getType(), event.values);
	}


    /**
     * Called when sensor values have changed.  The length and contents
     * of the values array vary depending on which sensor is being monitored.
     *
     * @param   sensorId        The ID of the sensor being monitored.
     * @param   values          The new values for the sensor.
     */
    @Override
    public void onSensorData(int sensorId, float[] values) {
        if (values.length < 3)
            return;

        synchronized (surfaceHolder) {
            // If we're in relative mode, subtract the baseline values.
            if (relativeMode) {
                // First time through, set the baseline values.
                if (relativeValues == null) {
                    relativeValues = new float[3];
                    relativeValues[0] = values[0];
                    relativeValues[1] = values[1];
                    relativeValues[2] = values[2];
                }

                processedValues[0] = values[0] - relativeValues[0];
                processedValues[1] = values[1] - relativeValues[1];
                processedValues[2] = values[2] - relativeValues[2];
            } else {
                processedValues[0] = values[0];
                processedValues[1] = values[1];
                processedValues[2] = values[2];
            }

            final float x = processedValues[0];
            final float y = processedValues[1];
            final float z = processedValues[2];
            float m = 0.0f;
            float az = 0.0f;
            float alt = 0.0f;

            // Calculate the magnitude.
            m = (float) Math.sqrt(x*x + y*y + z*z);

            // Calculate the azimuth and altitude.
            az = (float) Math.toDegrees(Math.atan2(y, x));
            az = 90 - az;
            if (az < 0)
                az += 360;

            // sin alt = z / mag
            alt = m == 0 ? 0  : (float) Math.toDegrees(Math.asin(z / m));

            plotView.setValues(processedValues, m, az, alt);
            chartView.setValue(m);
            numView.setValues(processedValues, m, az, alt);
            xyzView.setValue(processedValues);
        }
    }


	// ******************************************************************** //
	// Input.
	// ******************************************************************** //

    /**
     * Handle touch screen motion events.
     * 
     * @param	event			The motion event.
     * @return					True if the event was handled, false otherwise.
     */
	@Override
	public boolean handleTouchEvent(MotionEvent event) {
		final int x = (int) event.getX();
		final int y = (int) event.getY();
		final int action = event.getAction();
		boolean done = false;

		synchronized (surfaceHolder) {
			if (action == MotionEvent.ACTION_DOWN) {
				if (plotBounds.contains(x, y)) {
					setRelativeMode(!relativeMode);
					appContext.postSound(Sound.CHIRP_LOW);
					done = true;
				} else if (numBounds.contains(x, y)) {
					// Toggle the X/Y/Z display.
					showXyz = !showXyz;
					appContext.postSound(Sound.CHIRP_LOW);
					done = true;
				}
			}
		}

		event.recycle();
		return done;
	}


	// ******************************************************************** //
	// View Drawing.
	// ******************************************************************** //

	/**
	 * This method is called to ask the view to draw itself.
	 * 
	 * @param	canvas			Canvas to draw into.
	 * @param	now				Current system time in ms.
	 */
	@Override
	protected void draw(Canvas canvas, long now) {
		super.draw(canvas, now);
		
    	// If the sensor is not equipped, fake the data if requested to.
    	if (dataGenerator != null)
    		dataGenerator.generateValues();
    		
		// Draw the elements.
		plotView.draw(canvas, now);
		chartView.draw(canvas, now);
		if (showXyz)
			xyzView.draw(canvas, now);
		else
			numView.draw(canvas, now);
	}


    // ******************************************************************** //
    // Class Data.
    // ******************************************************************** //

    // Debugging tag.
	@SuppressWarnings("unused")
	private static final String TAG = "tricorder";
	
	// Colours for an XYZ plot.
	private static final int[] XYZ_PLOT_COLS =
						new int[] { 0xffff0000, 0xff00ff00, 0xff0000ff };

	
	// ******************************************************************** //
	// Private Data.
	// ******************************************************************** //
	
	// Application handle.
	private Tricorder appContext;
	
	// The surface we're drawing on.
	private SurfaceHolder surfaceHolder;

	// The sensor manager, which we use to interface to all sensors.
    private SensorManager sensorManager;
    
	// The ID of the sensor to read: Sensor.TYPE_XXX.
    private int sensorId;
    
    // The unit and range of the data.  baseDataRange is the specified range
    // for this display; dataRange is the current range under zooming.
	public final float dataUnit;
	public float dataRange;

	// Processed data values.
	private float[] processedValues = null;

	// If relativeMode is true, we're in relative mode.  Display values
	// relative to the values stored in relativeValues -- i.e. subtract
	// relativeValues from future inputs.
	private boolean relativeMode = false;
	private float[] relativeValues = null;

    // Flag whether we have the sensor.
    private final boolean sensorEquipped;

	// Colour of the graph grid and plot, in primary (absolute) and
    // secondary (relative) modes.
    private int gridColour1 = 0xff00ff00;
    private int plotColour1 = 0xffff0000;
    private int gridColour2 = 0xff00ff00;
    private int plotColour2 = 0xffff0000;

    // 3-axis plot, magnitude chart and numeric display for the data.
    // numView and xyzView are two alternative modes for the bottom plot.
    // We also keep the current bounds for each element.
    private AxisElement plotView;
    private Rect plotBounds;
    private MagnitudeElement chartView;
	private Num3DElement numView;
    private Rect numBounds;
    private MagnitudeElement xyzView;
    
    // Do we show the XYZ display?  If not, it's numView.
    private boolean showXyz = false;
	
	// Simulate missing sensors.  If not null, sensors that aren't equipped
	// will have simulated data generated using this generator.
	private DataGenerator dataGenerator = null;

}

