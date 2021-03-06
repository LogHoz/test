
/**
 * Plughole: a rolling-ball accelerometer game.
 * <br>Copyright 2008-2010 Ian Cameron Smith
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


package org.hermit.plughole;


/**
 * Class representing a line segment.  This is an immutable class.
 */
final class Line {

	// ******************************************************************** //
	// Constructor.
	// ******************************************************************** //
	
	/**
	 * Create a line with given start and end points.
	 * 
	 * @param	start		Start point.
	 * @param	end			End point.
	 */
	public Line(Point start, Point end) {
		this(start.x, start.y, end.x, end.y);
	}
	

    /**
     * Create a line with given start and end points.
     * 
     * @param   sx          Start X.
     * @param   sy          Start Y.
     * @param   ex          End X.
     * @param   ey          End Y.
     */
    public Line(double sx, double sy, double ex, double ey) {
	    this.reflectEnabled = true;
		this.sx = sx;
		this.sy = sy;
		this.ex = ex;
		this.ey = ey;
		this.dx = this.ex - this.sx;
		this.dy = this.ey - this.sy;
		
		// Calculate the line's magnitude.
		this.mag = Math.sqrt(dx * dx + dy * dy);
		
		// Generate the line's unit vector.
		this.ux = mag == 0 ? 0 : this.dx / this.mag;
		this.uy = mag == 0 ? 0 : this.dy / this.mag;
		
		// Set up the minima and maxima.  Pre-computing this now helps a
		// lot with rejecting lines in intersection calculations later.
		minX = sx < ex ? sx : ex;
		minY = sy < ey ? sy : ey;
		maxX = sx > ex ? sx : ex;
		maxY = sy > ey ? sy : ey;
	}
	

    // ******************************************************************** //
    // Accessors.
    // ******************************************************************** //

	/**
	 * Get the start point of this line.
	 * 
	 * @return				The start Point of this line.
	 */
	public Point getStart() {
		return new Point(sx, sy);
	}
	

	/**
	 * Get the end point of this line.
	 * 
	 * @return				The end Point of this line.
	 */
	public Point getEnd() {
		return new Point(ex, ey);
	}
	

    /**
     * Get the angle in which this line goes.
     * 
     * @return              The angle of this line, in degrees.  Zero means
     *                      to +x; angles go anti-clockwise to <360.
     */
    public double getAngle() {
        final double a = Math.toDegrees(Math.atan2(dy, dx));
        return a >= 0 ? a : 360.0 + a;
    }
    

	/**
	 * Get the actions triggered by crossing this line.
	 * 
	 * @return				The actions triggered by crossing this line;
	 * 						null if none.
	 */
	public Action[] getCrossActions() {
		return crossActions;
	}


    /**
     * Set the actions triggered by crossing this line.
     * 
     * @param   acts        The actions triggered by crossing this line;
     *                      null if none.
     */
    public void setCrossActions(Action[] acts) {
        crossActions = acts;
    }


    /**
     * Get the actions triggered by hitting and bouncing off this line.
     * 
     * @return              The actions triggered by hitting this line;
     *                      null if none.
     */
    public Action[] getBounceActions() {
        return bounceActions;
    }


    /**
     * Set the actions triggered by hitting and bouncing off this line.
     * 
     * @param   acts        The actions triggered by hitting this line;
     *                      null if none.
     */
    public void setBounceActions(Action[] acts) {
        bounceActions = acts;
    }


    // ******************************************************************** //
    // Transformations.
    // ******************************************************************** //

	/**
	 * Create a new line which is equal to this one moved left by a set
	 * distance.  Left is defined by the directions of the start and end
	 * points.
	 * 
	 * @param	dist		The offset distance.
	 * @return				A new line which is a shifted version of this one.
	 *                      Actions are not copied.
	 */
	public Line moveLeft(double dist) {
		// Calculate the displacement.  This is the left-hand normal
		// vector (which is the rotated unit vector) times the distance.
		final double dispX = uy * dist;
		final double dispY = -ux * dist;
		return new Line(sx + dispX, sy + dispY, ex + dispX, ey + dispY);
	}


    // ******************************************************************** //
    // Intersection.
    // ******************************************************************** //

	/**
	 * Determine the intersection -- if any -- between two infinite lines.
	 * 
	 * Note that this is relatively inefficient -- we allocate a new Point
	 * for the result.
	 * 
	 * @param	lineA		One line.
	 * @param	lineB		The other line.
	 * @return				The intersection point, if any.
	 *						Otherwise we return null, which could mean that the
	 * 						segments are parallel, coincident, or disjoint.
	 */
	static final Point intersect(Line lineA, Line lineB) {
		/*
		 * See: http://local.wasp.uwa.edu.au/~pbourke/geometry/lineline2d/
		 * Notes:
		 *  + The denominators for the equations for ua and ub are the same.
		 *  + If the denominator for the equations for ua and ub is 0 then the
		 *    two lines are parallel.
		 *  + If the denominator and numerator for the equations for ua and ub
		 *    are 0 then the two lines are coincident.
		 */
		final double mDx = lineB.dx;
		final double bDx = lineA.dx;
		final double mbX = lineB.sx - lineA.sx;
		final double mDy = lineB.dy;
		final double bDy = lineA.dy;
		final double mbY = lineB.sy - lineA.sy;
		
		final double denom =  bDy * mDx - bDx * mDy;
		if (denom == 0)
			return null;
		final double numB = bDx * mbY - bDy * mbX;
		
		final double fracB = numB / denom;
												
		// Calculate the intersect position.
		return new Point(lineB.sx + fracB * mDx, lineB.sy + fracB * mDy);
	}
	

	// ******************************************************************** //
	// Public Data.
	// ******************************************************************** //

	// Whether this line is enabled for reflection.
	public boolean reflectEnabled;
	
	// The start and end points (in that order).
	public final double sx;
	public final double sy;
	public final double ex;
	public final double ey;
	
	// The line's delta X, delta Y and magnitude.
	public final double dx;
	public final double dy;
	public final double mag;
	
	// The line's unit vector.
	public final double ux;
	public final double uy;

	// The line's minimum and maximum positions in X and Y.
	public final double minX;
	public final double minY;
	public final double maxX;
	public final double maxY;

	// The action to take when crossed.  Null if none.
	private Action[] crossActions;

    // The action to take when bounced off.  Null if none.
    private Action[] bounceActions;

}

