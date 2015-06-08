package edu.cmu.cs.eyetrack.state;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.jdesktop.core.animation.timing.Interpolator;
import org.jdesktop.core.animation.timing.KeyFrames;
import org.jdesktop.core.animation.timing.KeyFramesBuilder;
import org.jdesktop.core.animation.timing.interpolators.LinearInterpolator;
import org.jdesktop.core.animation.timing.interpolators.SplineInterpolator;

import edu.cmu.cs.eyetrack.helper.Coordinate;
import edu.cmu.cs.eyetrack.helper.Util;
import edu.cmu.cs.eyetrack.state.Settings.Experiment;
import edu.cmu.cs.eyetrack.state.Settings.MotionConstraintType;
import edu.cmu.cs.eyetrack.state.Settings.MotionInterpolationType;

public class RandomGen {

	@SuppressWarnings("serial")
	private static final List<Color> colorList = new ArrayList<Color>() {{ 
		add(Color.RED); 
		add(Color.BLUE);
		add(Color.GREEN);
		add(Color.YELLOW);
		add(new Color(255, 119, 0)); //colorList.add(Color.ORANGE);
		add(Color.PINK);
		add(Color.MAGENTA);
		add(Color.CYAN);
		add(Color.GRAY);
		//add(Color.LIGHT_GRAY);
		//add(Color.DARK_GRAY);
	}};

	private Random random;
	private Experiment exp;

	private List<Integer> gridList;

	private static final Interpolator SPLINE_0_0_0_0 = new SplineInterpolator(0.00, 0.00, 0.00, 0.00);
	private static final Interpolator SPLINE_0_0_0_1 = new SplineInterpolator(0.00, 0.00, 0.00, 1.00);
	private static final Interpolator SPLINE_0_0_1_1 = new SplineInterpolator(0.00, 0.00, 1.00, 1.00);
	private static final Interpolator SPLINE_0_1_1_1 = new SplineInterpolator(0.00, 1.00, 1.00, 1.00);
	private static final Interpolator SPLINE_1_1_1_1 = new SplineInterpolator(1.00, 1.00, 1.00, 1.00);
	private static final Interpolator SPLINE_0_0_1_0 = new SplineInterpolator(0.00, 0.00, 1.00, 0.00);
	private static final Interpolator SPLINE_1_0_1_1 = new SplineInterpolator(1.00, 0.00, 1.00, 1.00);
	private static final Interpolator[] SPLINES = { SPLINE_0_0_0_0, SPLINE_0_0_0_1, SPLINE_0_0_1_1, SPLINE_0_1_1_1, SPLINE_1_1_1_1, SPLINE_0_0_1_0, SPLINE_1_0_1_1 };

	private double blockWidth;
	private double blockHeight;
	
	public RandomGen(Random random, Experiment exp) {
		this.random = random;
		this.exp = exp;

		gridList = new ArrayList<Integer>();
		for(int gridIdx=0; gridIdx < (exp.getGridXSize()*exp.getGridYSize()); gridIdx++) {
			gridList.add(gridIdx);
		}

		blockWidth = (exp.getPixelWidth() / (double) exp.getGridXSize());
		blockHeight = (exp.getPixelHeight() / (double) exp.getGridYSize());
	}

	public List<Color> getRandomColors(int numColors) {

		if(numColors > colorList.size()) {
			Util.dPrintln("WARNING: More unique distractors than there are unique colors; will have repeats.");
		}

		Collections.shuffle(colorList, random);

		List<Color> genList = new ArrayList<Color>();
		for(int idx=0; idx<numColors; idx++) {
			genList.add(colorList.get(idx % colorList.size()));
		}

		return genList;
	}

	public List<Color> getRandomColors() {
		return getRandomColors( colorList.size() );
	}

	public List<Coordinate<Integer>> genRandomGridPositions(int numPositions) {

		if(numPositions > gridList.size()) {
			Util.dPrintln("WARNING: Requesting placement for more objects than there are grid positions.");
			return null;
		}

		// Shuffle positions
		Collections.shuffle(gridList, random);

		// Grab numPositions
		List<Coordinate<Integer>> gridPos = new ArrayList<Coordinate<Integer>>();
		for(int idx=0; idx<numPositions; idx++) {

			int rawGridIdx = gridList.get(idx);		
			gridPos.add(rawToGrid(rawGridIdx));
		}

		return gridPos;
	}

	// Target needs to end in each grid the same (+/- 1) number of times
	public List<Coordinate<Integer>> getRandomEndPositions(int numTrials) {

		if(numTrials > gridList.size()) {
			Util.dPrintln("Requesting more final positions than there are grid squares; some will repeat.");
		}

		List<Coordinate<Integer>> endingList = new ArrayList<Coordinate<Integer>>();
		for(int trialIdx=0; trialIdx<numTrials; trialIdx++) {

			// If we've gone through an entire set of grid blocks, wrap around
			if(trialIdx % gridList.size() == 0) {
				Collections.shuffle(gridList, random);
			}

			endingList.add( rawToGrid( 
					gridList.get(trialIdx % gridList.size()
							)));
		}

		return endingList;
	}

	// Picks a new, random grid coordinate that is different from the current
	// grid coordinate.  
	public Coordinate<Integer> getNextGridPos(Coordinate<Integer> currentGrid) {
		int newX, newY;
		do {
			newX = random.nextInt( exp.getGridXSize() );
			newY = random.nextInt( exp.getGridYSize() );
		} while( newX == currentGrid.getX() && newY == currentGrid.getY() );

		return new Coordinate<Integer>(newX, newY);
	}

	
	// Picks a new pixel coordinate on the screen, different than the current pixel position
	public double getNextPixelPos(Coordinate<Integer> currPos, Coordinate<Integer> nextPos, int xBuffer, int yBuffer, double speed, double maxDist, boolean useCentersOnly) {

		int newX, newY;

		Rectangle2D.Double screen = new Rectangle2D.Double(exp.getInsetX() + xBuffer, exp.getInsetY() + yBuffer, exp.getPixelWidth() - 2*xBuffer, exp.getPixelHeight() - 2*yBuffer);
		Rectangle2D.Double neighborhood = new Rectangle2D.Double(currPos.getX() - maxDist, currPos.getY() - maxDist, 2*maxDist, 2*maxDist);
		Rectangle2D.intersect(screen, neighborhood, neighborhood);

		do {

			int rx, ry;
			boolean nearTheCenter = false;
			do {
				rx = random.nextInt( (int) neighborhood.getWidth() );
				ry = random.nextInt( (int) neighborhood.getHeight() );
				
				// If we choose a point "near" the center, roll a die to see if we want to re-calc a point
				nearTheCenter = (rx > 0.33*neighborhood.getWidth() && rx < 0.67*neighborhood.getWidth() &&
					ry > 0.33*neighborhood.getHeight() && ry < 0.67*neighborhood.getHeight());
				
			} while(nearTheCenter && random.nextInt(3) > 0);

			newX = (int) neighborhood.getX() + rx;
			newY = (int) neighborhood.getY() + ry;

			if(useCentersOnly) {
				Coordinate<Integer> gridCenter = getGridCenter(xToGrid(newX), yToGrid(newY));
				newX = gridCenter.getX();
				newY = gridCenter.getY();
			}
			
		} while(newX == currPos.getX() && newY == currPos.getY() );

		// Tell the object to move to this new random point
		nextPos.setX(newX);
		nextPos.setY(newY);

		// Calculate how long that will take
		double distanceTraveled = nextPos.distance(currPos);
		double timeUsed = 1000.0 * ( distanceTraveled / speed );

		return timeUsed;
	}

	protected int xToGrid(double x) {
		return (int) ( (x - exp.getInsetX() ) / blockWidth );
	}

	protected int yToGrid(double y) {
		return (int) ( (y - exp.getInsetY() ) / blockHeight);
	}
	
	protected Coordinate<Integer> getGridCenter(int gridX, int gridY) {
		return new Coordinate<Integer>( 
				(int) (exp.getInsetX() + (gridX * blockWidth) + (0.5*blockWidth)),
				(int) (exp.getInsetY() + (gridY * blockHeight) + (0.5*blockHeight)));
	}
	
	public double calcKeyFrames(List<KeyFrames<Integer>> framesList,
			double minJumpMS, double maxJumpMS, double trialLength, 
			int xBuffer, int yBuffer, 
			Coordinate<?> startPos, Coordinate<?> endPos ) {

		KeyFramesBuilder<Integer> builderX = new KeyFramesBuilder<Integer>( startPos.getX().intValue() );
		KeyFramesBuilder<Integer> builderY = new KeyFramesBuilder<Integer>( startPos.getY().intValue() );

		// Objects have constant speed for now, measured in pixels/s
		// Convert maxJumpMs to the maximum distance in pixels we can travel without changing
		double speed = exp.getObjectSpeed();
		double maxDistWithoutJump = (maxJumpMS / 1000.0) * speed;

		// From the starting position, add random keyframes for each of the jump times
		double totalTime = 0;
		Coordinate<Integer> currentPos = new Coordinate<Integer>(startPos.getX().intValue(), startPos.getY().intValue() );
		FramePath fp = new FramePath();
		do {

			// Pick a new random location to head toward
			Coordinate<Integer> nextPos = new Coordinate<Integer>(0,0);
			double oldTime = totalTime;

			// Logic for the target only---if we're planned beyond the minimum time and
			// the random nextBoolean fires, work toward the ending position and break out
			if( endPos != null &&
					totalTime >= trialLength && 
					random.nextInt(3) == 0 ) {

				// We definitely want to end at this specific location
				nextPos.setX(endPos.getX().intValue());
				nextPos.setY(endPos.getY().intValue());

				// Make sure we go there at the correct speed
				double distanceTraveled = currentPos.distance(nextPos);
				double timeUsed = 1000.0 * ( distanceTraveled / speed );
				totalTime += timeUsed;

				fp.addFrame(nextPos, totalTime);
				trialLength = totalTime;
				break;
			}

			// Distractors and targets change trajectories only at centers of grids?
			boolean useCentersOnly = exp.getMotionConstraintType().equals(MotionConstraintType.GRID);

			double timeUsed = getNextPixelPos(currentPos, nextPos, xBuffer, yBuffer, speed, maxDistWithoutJump, useCentersOnly);
			totalTime += timeUsed;

			// Logic for the distractors only
			if( endPos == null) {
				// Keyframe's time as a fraction of total trial time
				double timeFrac = (double) totalTime / (double) trialLength;

				// If we won't make it to our end point in time, only go part-way
				if(timeFrac > 1.0) { 

					double diffX = nextPos.getX() - currentPos.getX();
					double diffY = nextPos.getY() - currentPos.getY();
					double diffFrac = 1.0- ((timeFrac - 1.0) / ((totalTime - oldTime) / trialLength));
					nextPos.setX( (int) Math.round(currentPos.getX() + (diffFrac * diffX)) );
					nextPos.setY( (int) Math.round(currentPos.getY() + (diffFrac * diffY)) );
					timeFrac = 1.0;
				}

				Interpolator interp;
				if(exp.getMotionInterpolationType().equals(MotionInterpolationType.LINEAR)) {
					interp = LinearInterpolator.getInstance();
				} else {
					 interp = SPLINES[random.nextInt(SPLINES.length)];
				}
				
				builderX.addFrame(nextPos.getX().intValue(), timeFrac, interp);
				builderY.addFrame(nextPos.getY().intValue(), timeFrac, interp);	
			} else {
				fp.addFrame(nextPos, totalTime);
			}

			currentPos = nextPos;

		} while( endPos != null || totalTime < trialLength );


		if( endPos != null ) {

			for(int idx=0; idx<fp.getPoints().size(); idx++) {
				// For each recorded point, calculate the now-proper fraction of time and
				// add as a new frame
				Coordinate<Integer> nextPos = fp.getPoints().get(idx);
				double timeFrac = fp.getTimes().get(idx) / trialLength;

				Interpolator interp;
				if(exp.getMotionInterpolationType().equals(MotionInterpolationType.LINEAR)) {
					interp = LinearInterpolator.getInstance();
				} else {
					interp = SPLINES[random.nextInt(SPLINES.length)];
				}
				
				builderX.addFrame(nextPos.getX().intValue(), timeFrac, interp);
				builderY.addFrame(nextPos.getY().intValue(), timeFrac, interp);	
			}
		}

		final KeyFrames<Integer> framesX = builderX.build();  
		final KeyFrames<Integer> framesY = builderY.build();   

		// Need to return keyframes for x and y
		framesList.add(framesX);
		framesList.add(framesY);
		return totalTime;
	}


	public static List<Color> getColorList() {
		return colorList;
	}


	@SuppressWarnings("unused")
	private Interpolator genRandomSpline() {
		return SPLINES[ random.nextInt(SPLINES.length) ];
	}

	private Coordinate<Integer> rawToGrid(int rawGridIdx) {
		int gridX = rawGridIdx % exp.getGridXSize();
		int gridY = rawGridIdx / exp.getGridXSize();
		return new Coordinate<Integer>(gridX, gridY);
	}

	private class FramePath {

		private List<Coordinate<Integer>> points;
		private List<Double> times;

		public FramePath() {
			points = new ArrayList<Coordinate<Integer>>();
			times = new ArrayList<Double>();
		}

		public void addFrame(Coordinate<Integer> point, double time) {
			points.add(point);
			times.add(time);
		}

		public List<Coordinate<Integer>> getPoints() {
			return points;
		}

		public List<Double> getTimes() {
			return times;
		}
	}
}
