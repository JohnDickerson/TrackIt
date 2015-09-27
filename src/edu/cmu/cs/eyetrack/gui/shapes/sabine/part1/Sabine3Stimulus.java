package edu.cmu.cs.eyetrack.gui.shapes.sabine.part1;

import java.awt.Color;
import java.awt.Polygon;

import edu.cmu.cs.eyetrack.gui.shapes.Stimulus;

public class Sabine3Stimulus extends Stimulus {

	private Polygon polygon;
	private static double squeezeFrac = 0.25;
	private static double indentFrac = 0.40;

	public Sabine3Stimulus(String name, Color color, int width, int height) {

		super(name, color, width, height, color);

		polygon = new Polygon();
		polygon.addPoint(width, height);
		polygon.addPoint(width, 0);
		polygon.addPoint(0, (int) (height * squeezeFrac));
		polygon.addPoint((int) (width*indentFrac), (int) (height * 0.5));
		polygon.addPoint(0, (int) (height * (1-squeezeFrac)));
		shape = polygon;
	}

	@Override
	public Stimulus factoryClone(Color color) {
		if(color==null) {
			return new Sabine3Stimulus(name, this.color, width, height);
		} else {
			return new Sabine3Stimulus(name, color, width, height);
		}
	}

	@Override
	public void move(int newX, int newY) {
		polygon.translate(-((int) polygon.getBounds().getX()) + newX - (int) (0.5 * width), 
				-((int) polygon.getBounds().getY()) + newY - (int) (0.5 * height));
	}
}
