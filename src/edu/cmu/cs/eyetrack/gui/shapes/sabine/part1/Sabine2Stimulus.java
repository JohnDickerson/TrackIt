package edu.cmu.cs.eyetrack.gui.shapes.sabine.part1;

import java.awt.Color;
import java.awt.Polygon;

import edu.cmu.cs.eyetrack.gui.shapes.Stimulus;

public class Sabine2Stimulus extends Stimulus {

	private Polygon polygon;
	private static double squeezeFrac = 0.25;
	
	public Sabine2Stimulus(String name, Color color, int width, int height) {

		super(name, color, width, height, color);

		polygon = new Polygon();
		polygon.addPoint(0,0);
		polygon.addPoint(0, height);
		polygon.addPoint(width, (int) (height * (1-squeezeFrac)) );
		polygon.addPoint(width, (int) (height * squeezeFrac));

		shape = polygon;
	}

	@Override
	public Stimulus factoryClone(Color color) {
		if(color==null) {
			return new Sabine2Stimulus(name, this.color, width, height);
		} else {
			return new Sabine2Stimulus(name, color, width, height);
		}
	}

	@Override
	public void move(int newX, int newY) {
		//polygon.translate(-oldX + newX - (int) (0.5 * width), -oldY + newY - (int) (0.5 * height));
		polygon.translate(-((int) polygon.getBounds().getX()) + newX - (int) (0.5 * width), 
				-((int) polygon.getBounds().getY()) + newY - (int) (0.5 * height));
	}
}
