package edu.cmu.cs.eyetrack.gui.shapes;

import java.awt.Color;
import java.awt.Polygon;

public class StarStimulus extends Stimulus {

	private Polygon polygon;
	private int numArms;
	private double innerRadius;
	private double outerRadius;

	public StarStimulus(String name, Color color, int numArms, double innerRadius, double outerRadius) {

		super(name, color, (int) (outerRadius * 2), (int) (outerRadius * 2));

		this.numArms = numArms;
		this.innerRadius = innerRadius;
		this.outerRadius = outerRadius;
		this.polygon = new Polygon();

		// General star construction adapted from StackOverflow
		// http://stackoverflow.com/questions/2710065/drawing-star-shapes-with-variable-parameters
		double angle = Math.PI / numArms;

		for(int i=0; i<2*numArms; i++) {
			double r = (i & 1) == 0 ? outerRadius : innerRadius;
			double x = 0 + Math.cos(i * angle) * r;
			double y = 0 + Math.sin(i * angle) * r;
			polygon.addPoint( (int) x, (int) y );
		}

		// Tweak the height/width of the shape to fix our estimates
		height = (int) polygon.getBounds().getHeight();
		width = (int) polygon.getBounds().getWidth();
		shape = polygon;
	}

	@Override
	public Stimulus factoryClone(Color color) {
		if(color==null) {
			return new StarStimulus(name, this.color, numArms, innerRadius, outerRadius);
		} else {
			return new StarStimulus(name, color, numArms, innerRadius, outerRadius);
		}
	}
	
	@Override
	public void move(int newX, int newY) {
		//polygon.translate(-oldX + newX - (int) (0.5 * width), -oldY + newY - (int) (0.5 * height));
		polygon.translate(-((int) polygon.getBounds().getX()) + newX - (int) (0.5 * width), 
				-((int) polygon.getBounds().getY()) + newY - (int) (0.5 * height));
	}

}
