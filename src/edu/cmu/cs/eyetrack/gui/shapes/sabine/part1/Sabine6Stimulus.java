package edu.cmu.cs.eyetrack.gui.shapes.sabine.part1;

import java.awt.Color;
import java.awt.Polygon;

import edu.cmu.cs.eyetrack.gui.shapes.Stimulus;

public class Sabine6Stimulus extends Stimulus {

	private Polygon polygon;
	
	public Sabine6Stimulus(String name, Color color, int width, int height) {

		super(name, color, width, height, color);

		polygon = new Polygon();
		polygon.addPoint(width, height);
		polygon.addPoint((int) (0.3 * width), height);
		polygon.addPoint((int) (0.3 * width), (int) (0.9 * height));
		polygon.addPoint(0, (int) (0.9 * height));
		polygon.addPoint(0, (int) (0.4 * height));
		polygon.addPoint((int) (0.3 * width), (int) (0.4 * height));
		polygon.addPoint((int) (0.3 * width), 0);
		polygon.addPoint(width, 0);
		shape = polygon;
	}

	@Override
	public Stimulus factoryClone(Color color) {
		if(color==null) {
			return new Sabine6Stimulus(name, this.color, width, height);
		} else {
			return new Sabine6Stimulus(name, color, width, height);
		}
	}

	@Override
	public void move(int newX, int newY) {
		//polygon.translate(-oldX + newX - (int) (0.5 * width), -oldY + newY - (int) (0.5 * height));
		polygon.translate(-((int) polygon.getBounds().getX()) + newX - (int) (0.5 * width), 
				-((int) polygon.getBounds().getY()) + newY - (int) (0.5 * height));
	}
}
