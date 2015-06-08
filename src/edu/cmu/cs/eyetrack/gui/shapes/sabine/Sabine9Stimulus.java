package edu.cmu.cs.eyetrack.gui.shapes.sabine;

import java.awt.Color;
import java.awt.Polygon;

import edu.cmu.cs.eyetrack.gui.shapes.Stimulus;

public class Sabine9Stimulus extends Stimulus {

	private Polygon polygon;
	private static double bevelFrac = 0.2;

	public Sabine9Stimulus(String name, Color color, int width, int height) {

		super(name, color, width, height);

		polygon = new Polygon();
		polygon.addPoint(0, (int) (bevelFrac*height));
		polygon.addPoint((int) (bevelFrac*width), 0);
		polygon.addPoint(width, 0);
		polygon.addPoint(width, (int) ((1-bevelFrac)*height));
		polygon.addPoint((int) (width * (1-bevelFrac)), height);
		polygon.addPoint(0, height);
		shape = polygon;
	}

	@Override
	public Stimulus factoryClone(Color color) {
		if(color==null) {
			return new Sabine9Stimulus(name, this.color, width, height);
		} else {
			return new Sabine9Stimulus(name, color, width, height);
		}
	}

	@Override
	public void move(int newX, int newY) {
		//polygon.translate(-oldX + newX - (int) (0.5 * width), -oldY + newY - (int) (0.5 * height));
		polygon.translate(-((int) polygon.getBounds().getX()) + newX - (int) (0.5 * width), 
				-((int) polygon.getBounds().getY()) + newY - (int) (0.5 * height));
	}

}
