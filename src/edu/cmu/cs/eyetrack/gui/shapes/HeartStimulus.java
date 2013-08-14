package edu.cmu.cs.eyetrack.gui.shapes;

import java.awt.Color;
import java.awt.geom.GeneralPath;

public class HeartStimulus extends Stimulus {

	final static double topCurveFrac = -0.1;
	final static double startCurveFrac = 0.4;
	final static double widerByFrac = 0.15;
	
	public HeartStimulus(String name, Color color, int width, int height) {

		// The heart we're drawing is slightly wider/taller than requested; this
		// is hacky, but doesn't matter for now since we're scaling everything by 0.5 anyways.
		super(name, color, width, height);	
		makeShape(0,0);
	}

	private void makeShape(int x, int y) {

		GeneralPath heart = new GeneralPath();
		heart.moveTo(x + (0.5 * width), y + height);
		heart.lineTo(x, y + (startCurveFrac * height));
		heart.curveTo(x - (widerByFrac * width) , y + (topCurveFrac * height), 
				x + (0.6 * width), y + (topCurveFrac * height), 
				x + (0.5 * width), y + (startCurveFrac * height));

		heart.curveTo(x + (0.4 * width), y + (topCurveFrac * height),
				x + ((1.0 + widerByFrac) * width), y + (topCurveFrac * height),
				x + width, y + (startCurveFrac * height));

		heart.lineTo(x + (0.5 * width), y + height);

		//System.out.println("Width: " + width + ", Actual: " + heart.getBounds().getWidth());
		//System.out.println("Height: " + height + ", Actual: " + heart.getBounds().getHeight());
			
		shape = heart;
	}

	@Override
	public Stimulus factoryClone(Color color) {
		if(color==null) {
			return new HeartStimulus(name, this.color, width, height);
		} else {
			return new HeartStimulus(name, color, width, height);
		}
	}

	@Override
	public void move(int newX, int newY) {
		makeShape(newX - (int) (0.5 * width),
				newY - (int) (0.5 * height));
	}
}
